package com.homosapiens.diagnocareservice.core.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.homosapiens.diagnocareservice.core.exception.AppException;
import com.homosapiens.diagnocareservice.core.kafka.eventEnums.KafkaEvent;
import com.homosapiens.diagnocareservice.model.entity.availability.Availability;
import com.homosapiens.diagnocareservice.model.entity.availability.WeekDay;
import com.homosapiens.diagnocareservice.model.entity.dtos.AvailabilityDto;
import com.homosapiens.diagnocareservice.model.entity.dtos.AvailabilityResponseDto;
import com.homosapiens.diagnocareservice.model.mapper.AvailabilityMapper;
import com.homosapiens.diagnocareservice.repository.AvailabilityRepository;
import com.homosapiens.diagnocareservice.repository.ScheduleSlotRepository;
import com.homosapiens.diagnocareservice.service.AvailabilityService;
import com.homosapiens.diagnocareservice.service.ScheduleSlotService;
import com.homosapiens.diagnocareservice.service.WeekDayService;
import com.homosapiens.diagnocareservice.service.impl.appointment.AvailabilityGenerator;
import com.homosapiens.diagnocareservice.service.impl.appointment.ScheduleSlotServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

@Component
@RequiredArgsConstructor
public class KafkaConsumer {

    private final Logger logger = Logger.getLogger(KafkaConsumer.class.getName());
    private final ObjectMapper objectMapper = new ObjectMapper() {{
        registerModule(new JavaTimeModule());
    }};
    private final AvailabilityGenerator availabilityGenerator;
    private final AvailabilityMapper availabilityMapper;
    private final KafkaProducer kafkaProducer;
    private final ScheduleSlotService scheduleSlotService;
    private final WeekDayService weekDayService;
    private final AvailabilityRepository availabilityRepository;

    @KafkaListener(topics = "AVAILABILITY_CREATED", groupId = "availability")
    @Transactional
    public void onAvailabilityCreated(ConsumerRecord<String, String> record) {
        String key = record.key();
        String message = record.value();

        logger.info("Received key: " + key);
        logger.info("Received raw message: " + message);

        try {
            Availability availability = objectMapper.readValue(message, Availability.class);

            // Ensure slots are created for the availability
            scheduleSlotService.createSlots(availability);

            // Generate additional availabilities if needed
            List<AvailabilityResponseDto> availabilityResponseDtos = availabilityGenerator.generateAvailability(availability);
            System.out.println(availabilityResponseDtos.size());
            kafkaProducer.sendMessage(KafkaEvent.AVAILABILITIES_GENERATED.toString(), "Availabilities has been generated", availabilityResponseDtos.size());
        } catch (Exception e) {
            logger.severe("Failed to deserialize message: " + e.getMessage());
        }
    }

    @KafkaListener(topics = "AVAILABILITY_DELETED", groupId = "availability")
    @Transactional
    public void onAvailabilityDeleted(ConsumerRecord<String, String> record) {
        String key = record.key();
        String message = record.value();

        logger.info("Received key: " + key);
        logger.info("Received raw message: " + message);

        try {
            Long availabilityId = objectMapper.readValue(message, Long.class);
            logger.info("Received availability ID for deletion: " + availabilityId);
            
            Optional<Availability> availabilityOpt = availabilityRepository.findByIdWithWeekDays(availabilityId);

            if (availabilityOpt.isEmpty()) {
                logger.warning("Availability with id " + availabilityId + " not found - it may have already been deleted");
                return; // Exit gracefully if availability not found
            }

            Availability availability = availabilityOpt.get();
            logger.info("Found availability for deletion - User ID: " + availability.getUser().getId() + ", Generated: " + availability.isGenerated());
            Long userId = availability.getUser().getId();

            // Step 1: Delete all generated availabilities and their schedule slots
            List<Availability> generatedAvailabilities = availabilityRepository.findByUserId(userId).stream()
                    .filter(av -> av.isGenerated())
                    .collect(java.util.stream.Collectors.toList());
            
            for (Availability generatedAvailability : generatedAvailabilities) {
                // Delete schedule slots for each generated availability
                scheduleSlotService.bulkDelete(generatedAvailability);
                
                // Delete weekdays for each generated availability
                Set<WeekDay> weekDays = generatedAvailability.getWeekDays();
                if (!weekDays.isEmpty()) {
                    weekDayService.deleteAllWeekDays(weekDays);
                }
            }
            
            // Delete all generated availabilities
            availabilityRepository.deleteByisGeneratedAndUserId(true, userId);

            // Step 2: Delete schedule slots for the main availability
            scheduleSlotService.bulkDelete(availability);

            // Step 3: Delete all weekdays for the main availability
            Set<WeekDay> weekDays = availability.getWeekDays();
            if (!weekDays.isEmpty()) {
                weekDayService.deleteAllWeekDays(weekDays);
            }

            // Step 4: Now delete the main availability
            availabilityRepository.deleteById(availability.getId());

            logger.info("Successfully deleted availability ID " + availabilityId + " and all generated availabilities");
        } catch (Exception e) {
            logger.severe("Failed to deserialize message: " + e.getMessage());
        }
    }

}
