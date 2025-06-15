package com.homosapiens.diagnocareservice.core.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.homosapiens.diagnocareservice.core.kafka.eventEnums.KafkaEvent;
import com.homosapiens.diagnocareservice.model.entity.availability.Availability;
import com.homosapiens.diagnocareservice.model.entity.dtos.AvailabilityDto;
import com.homosapiens.diagnocareservice.model.entity.dtos.AvailabilityResponseDto;
import com.homosapiens.diagnocareservice.model.mapper.AvailabilityMapper;
import com.homosapiens.diagnocareservice.service.impl.appointment.AvailabilityGenerator;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.logging.Logger;

@Component
public class KafkaConsumer {

    private final Logger logger = Logger.getLogger(KafkaConsumer.class.getName());
    private final ObjectMapper objectMapper;
    private final AvailabilityGenerator availabilityGenerator;
    private final AvailabilityMapper availabilityMapper;
    private final KafkaProducer kafkaProducer;

    public KafkaConsumer(AvailabilityGenerator availabilityGenerator, AvailabilityMapper availabilityMapper, KafkaProducer kafkaProducer) {
        this.availabilityGenerator = availabilityGenerator;
        this.availabilityMapper = availabilityMapper;
        this.kafkaProducer = kafkaProducer;
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        // optionally: configure to not fail on unknown properties
        // objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @KafkaListener(topics = "AVAILABILITY_CREATED", groupId = "availability")
    public void onAvailabilityCreated(ConsumerRecord<String, String> record) {
        String key = record.key();
        String message = record.value();

        logger.info("Received key: " + key);
        logger.info("Received raw message: " + message);

        try {
            Availability availability = objectMapper.readValue(message, Availability.class);

            List<AvailabilityResponseDto>  availabilityResponseDtos = availabilityGenerator.generateAvailability(availability);
            System.out.println(availabilityResponseDtos.size());
            kafkaProducer.sendMessage(KafkaEvent.AVAILABILITIES_GENERATED.toString(), "Availabilities has been generated", availabilityResponseDtos.size());
        } catch (Exception e) {
            logger.severe("Failed to deserialize message: " + e.getMessage());
        }
    }
}
