package com.homosapiens.diagnocareservice.core.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.homosapiens.diagnocareservice.core.exception.AppException;
import com.homosapiens.diagnocareservice.core.kafka.eventEnums.KafkaEvent;
import com.homosapiens.diagnocareservice.model.entity.availability.Availability;
import com.homosapiens.diagnocareservice.model.entity.availability.WeekDay;
import com.homosapiens.diagnocareservice.model.entity.dtos.AvailabilityDto;
import com.homosapiens.diagnocareservice.model.entity.dtos.AvailabilityResponseDto;
import com.homosapiens.diagnocareservice.model.entity.dtos.AvailabilityEventDto;
import com.homosapiens.diagnocareservice.model.mapper.AvailabilityMapper;
import com.homosapiens.diagnocareservice.repository.AvailabilityRepository;
import com.homosapiens.diagnocareservice.repository.ScheduleSlotRepository;
import com.homosapiens.diagnocareservice.service.AvailabilityService;
import com.homosapiens.diagnocareservice.service.ScheduleSlotService;
import com.homosapiens.diagnocareservice.service.WeekDayService;
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
            AvailabilityEventDto eventDto = objectMapper.readValue(message, AvailabilityEventDto.class);
            logger.info("Received availability event: " + eventDto.getEventType() + " for ID: " + eventDto.getAvailabilityId());

            Optional<Availability> availabilityOpt = availabilityRepository.findByIdWithWeekDays(eventDto.getAvailabilityId());

            if (availabilityOpt.isEmpty()) {
                logger.warning("Availability with id " + eventDto.getAvailabilityId() + " not found");
                return;
            }

            Availability availability = availabilityOpt.get();

            // Ensure slots are created for the availability
            scheduleSlotService.createSlots(availability);

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
            AvailabilityEventDto eventDto = objectMapper.readValue(message, AvailabilityEventDto.class);
            logger.info("Received availability deletion event for ID: " + eventDto.getAvailabilityId());
            
            Optional<Availability> availabilityOpt = availabilityRepository.findByIdWithWeekDays(eventDto.getAvailabilityId());

            if (availabilityOpt.isEmpty()) {
                logger.warning("Availability with id " + eventDto.getAvailabilityId() + " not found - it may have already been deleted");
                return; // Exit gracefully if availability not found
            }

            Availability availability = availabilityOpt.get();

            // Step 2: Delete schedule slots for the main availability
            scheduleSlotService.bulkDelete(availability);

            // Step 3: Delete all weekdays for the main availability
            Set<WeekDay> weekDays = availability.getWeekDays();
            if (!weekDays.isEmpty()) {
                weekDayService.deleteAllWeekDays(weekDays);
            }

            // Step 4: Now delete the main availability
            availabilityRepository.deleteById(availability.getId());

            logger.info("Successfully deleted availability ID " + eventDto.getAvailabilityId() + " and all generated availabilities");
        } catch (Exception e) {
            logger.severe("Failed to deserialize message: " + e.getMessage());
        }
    }

    @KafkaListener(topics = "AVAILABILITY_UPDATED", groupId = "availability")
    @Transactional
    public void onAvailabilityUpdated(ConsumerRecord<String, String> record) {
        String key = record.key();
        String message = record.value();

        logger.info("Received key: " + key);
        logger.info("Received raw message: " + message);

        try {
            AvailabilityEventDto eventDto = objectMapper.readValue(message, AvailabilityEventDto.class);
            logger.info("Received availability update event: " + eventDto.getEventType() + " for ID: " + eventDto.getAvailabilityId());

            Optional<Availability> availabilityOpt = availabilityRepository.findByIdWithWeekDays(eventDto.getAvailabilityId());

            if (availabilityOpt.isEmpty()) {
                logger.warning("Availability with id " + eventDto.getAvailabilityId() + " not found");
                return;
            }

            Availability availability = availabilityOpt.get();


            scheduleSlotService.createSlots(availability);


        } catch (Exception e) {
            logger.severe("Failed to deserialize message: " + e.getMessage());
        }
    }

}
