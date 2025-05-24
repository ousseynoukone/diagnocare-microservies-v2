package com.diagnocare.gateway.core.kafka;

import com.diagnocare.gateway.data.dto.UserRegisterDto;
import com.diagnocare.gateway.core.kafka.eventEnums.KafkaEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class KafkaConsumer {
    private final Logger logger = Logger.getLogger(KafkaConsumer.class.getName());
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "USER_REGISTERED", groupId = "test")
    public void onUserRegisterConsumer(ConsumerRecord<String, String> record) {
        String key = record.key();
        String message = record.value();

        logger.info("Received key: " + key);
        logger.info("Received raw message: " + message);

        try {
            UserRegisterDto dto = objectMapper.readValue(message, UserRegisterDto.class);
            logger.info("Parsed DTO: " + dto.toString());
        } catch (Exception e) {
            logger.severe("Failed to deserialize message: " + e.getMessage());
        }
    }
}
