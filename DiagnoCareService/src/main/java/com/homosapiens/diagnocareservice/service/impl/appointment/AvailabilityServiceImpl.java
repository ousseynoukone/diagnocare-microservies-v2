package com.homosapiens.diagnocareservice.service.impl.appointment;

import com.homosapiens.diagnocareservice.core.exception.AppException;
import com.homosapiens.diagnocareservice.core.kafka.KafkaProducer;
import com.homosapiens.diagnocareservice.core.kafka.eventEnums.KafkaEvent;
import com.homosapiens.diagnocareservice.model.entity.availability.Availability;
import com.homosapiens.diagnocareservice.model.entity.availability.WeekDay;
import com.homosapiens.diagnocareservice.model.entity.appointment.ScheduleSlot;
import com.homosapiens.diagnocareservice.model.entity.dtos.AvailabilityDto;
import com.homosapiens.diagnocareservice.model.entity.dtos.AvailabilityResponseDto;
import com.homosapiens.diagnocareservice.model.entity.dtos.AvailabilityEventDto;
import com.homosapiens.diagnocareservice.model.entity.dtos.WeekDayDto;
import com.homosapiens.diagnocareservice.model.mapper.AvailabilityMapper;
import com.homosapiens.diagnocareservice.repository.AvailabilityRepository;
import com.homosapiens.diagnocareservice.repository.ScheduleSlotRepository;
import com.homosapiens.diagnocareservice.service.AvailabilityService;
import com.homosapiens.diagnocareservice.service.ScheduleSlotService;
import com.homosapiens.diagnocareservice.service.UserService;
import com.homosapiens.diagnocareservice.service.WeekDayService;
import com.homosapiens.diagnocareservice.service.impl.appointment.helper.AvailabilityHelper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvailabilityServiceImpl implements AvailabilityService {
    private final AvailabilityRepository availabilityRepository;
    private final ScheduleSlotService scheduleSlotService;
    private final UserService userService;
    private final AvailabilityMapper availabilityMapper;
    private final KafkaProducer kafkaProducer;
    private final AvailabilityHelper availabilityHelper;
    private final WeekDayService weekDayService;
    private final ScheduleSlotRepository scheduleSlotRepository;
    private static final Logger logger = LoggerFactory.getLogger(AvailabilityServiceImpl.class);
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public AvailabilityResponseDto createAvailability(AvailabilityDto availabilityDto, Optional<Boolean> ignoreAvailabilityCheck ) {
        boolean ignoreCheck = ignoreAvailabilityCheck.orElse(false);

        // Validate weekday times
        validateWeekDayTimes(availabilityDto.getWeekDays());

        if(!ignoreCheck){
            // Can doctor create new availabilities
            Optional<Availability> lastAvailability = availabilityRepository.findFirstByUserId_OrderByAvailabilityDateDesc(availabilityDto.getUserId());

            if (lastAvailability.isPresent()) {
                Availability availability = lastAvailability.get();
                if(!availability.getRepeatUntil().minusWeeks(1).isBefore(LocalDate.now())){

                    throw new AppException(HttpStatus.NOT_ACCEPTABLE, "You already have a running availability , you must either edit your current one or wait  at least until "+availabilityDto.getRepeatUntil().minusWeeks(1));

                }
            }
        }


        Availability availability = availabilityMapper.toAvailability(availabilityDto);
        Availability savedAvailability = availabilityRepository.save(availability);

        AvailabilityEventDto eventDto = new AvailabilityEventDto(
            savedAvailability.getId(),
            savedAvailability.getUser().getId(),
            "AVAILABILITY_CREATED",
            "Doctor Created Availability"
        );
        kafkaProducer.sendMessage(KafkaEvent.AVAILABILITY_CREATED.toString(), "Doctor Created Availability", eventDto);
        return AvailabilityResponseDto.fromEntity(savedAvailability);
    }

    @Override
    public Page<AvailabilityResponseDto> getAllAvailability(Pageable pageable) {
        return availabilityRepository.findAllWithWeekDays(pageable)
            .map(AvailabilityResponseDto::fromEntity);
    }

    @Override
    public Optional<AvailabilityResponseDto> getAvailabilityById(long id) {
        return availabilityRepository.findByIdWithWeekDays(id)
            .map(AvailabilityResponseDto::fromEntity);
    }

    @Override
    @Transactional
    public AvailabilityResponseDto updateAvailability(AvailabilityDto availabilityDto, long id) {
        Optional<Availability> lastSavedAvailability = availabilityRepository.findById(id);

        if (lastSavedAvailability.isEmpty()) {
            throw new AppException(HttpStatus.NOT_FOUND, "No such availability found");
        }

        // Validate weekday times
        validateWeekDayTimes(availabilityDto.getWeekDays());

        Availability oldAvailability = lastSavedAvailability.get();
        boolean isUpdatable = availabilityHelper.checkAvailability(oldAvailability, oldAvailability);

        if (isUpdatable) {

            // Re-fetch the managed Availability instance
            Availability managedAvailability = availabilityRepository.findById(id)
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "No such availability found after update"));

            // Update the existing availability with new data

            managedAvailability.setRepeatUntil(availabilityDto.getRepeatUntil());
            managedAvailability.setAvailabilityDate(availabilityDto.getAvailabilityDate() != null ?
                    availabilityDto.getAvailabilityDate() : LocalDate.now());

            // Delete all weekday
           Set<WeekDay> weekDays = managedAvailability.getWeekDays();
           weekDayService.deleteAllWeekDays(weekDays);

           managedAvailability.getWeekDays().clear();


           availabilityRepository.saveAndFlush(managedAvailability);

            // Now create and save new weekDays with proper availability reference
            weekDays = availabilityDto.getWeekDays().stream()
                    .map(weekDayDto -> {
                        WeekDay weekDay = new WeekDay();
                        weekDay.setFromTime(weekDayDto.getFromTime());
                        weekDay.setToTime(weekDayDto.getToTime());
                        weekDay.setDaysOfWeek(weekDayDto.getDaysOfWeek());
                        weekDay.setSlotDuration(weekDayDto.getSlotDuration());
                        weekDay.setAvailability(managedAvailability);
                        return weekDay;
                    })
                    .collect(Collectors.toSet());

            // Save weekDays
            List<WeekDay> savedWeekDays = weekDayService.saveAllWeekDay(weekDays);

            // Add weekDays to availability
            managedAvailability.getWeekDays().addAll(savedWeekDays);
            availabilityRepository.save(managedAvailability);

            AvailabilityEventDto eventDto = new AvailabilityEventDto(
                managedAvailability.getId(),
                managedAvailability.getUser().getId(),
                "AVAILABILITY_UPDATED",
                "Doctor Updated Availability"
            );
            kafkaProducer.sendMessage(KafkaEvent.AVAILABILITY_UPDATED.toString(), "Doctor Updated Availability", eventDto);

            return AvailabilityResponseDto.fromEntity(managedAvailability);
        }
        throw new AppException(HttpStatus.NOT_ACCEPTABLE, "This availability is not updatable");
    }

    @Override
    @Transactional
    public void deleteAvailability(long id) {
        // Check if availability exists
        Optional<Availability> availabilityOpt = availabilityRepository.findById(id);
        if (availabilityOpt.isEmpty()) {
            throw new AppException(HttpStatus.NOT_FOUND, "Availability not found with ID: " + id);
        }

        Availability availability = availabilityOpt.get();

        // Check if there are any booked slots that would prevent deletion
        try {
            availabilityHelper.checkAvailability(availability, availability);

            // Send Kafka event for availability deletion - the consumer will handle the complete deletion
            logger.info("Sending availability deletion event for ID: " + availability.getId());
            AvailabilityEventDto eventDto = new AvailabilityEventDto(
                availability.getId(),
                availability.getUser().getId(),
                "AVAILABILITY_DELETED",
                "Doctor Deleted Availability"
            );
            kafkaProducer.sendMessage(KafkaEvent.AVAILABILITY_DELETED.toString(), "Doctor Deleted Availability", eventDto);

        } catch (AppException e) {
            // Re-throw the exception with a more specific message for deletion context
            throw new AppException(HttpStatus.CONFLICT, "Cannot delete availability: " + e.getMessage());
        }
    }


    private void validateWeekDayTimes(Set<WeekDayDto> weekDays) {
        if (weekDays == null || weekDays.isEmpty()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "At least one weekday must be provided");
        }

        for (WeekDayDto weekDay : weekDays) {
            if (weekDay.getFromTime() == null || weekDay.getToTime() == null) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Start time and end time are required for all weekdays");
            }

            if (weekDay.getFromTime().equals(weekDay.getToTime())) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Start time and end time cannot be the same for weekday: " + weekDay.getDaysOfWeek());
            }

            if (weekDay.getFromTime().isAfter(weekDay.getToTime())) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Start time must be before end time for weekday: " + weekDay.getDaysOfWeek());
            }
        }
    }
} 