package com.homosapiens.diagnocareservice.core.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class KafkaProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final Logger logger = Logger.getLogger(KafkaProducer.class.getName());

    public void sendMessage(String topicName,String message, Object data) {
        try {
            String jsonData = objectMapper.writeValueAsString(data);
            kafkaTemplate.send(topicName, message, jsonData);
            logger.info("Message " + message + " has been successfully sent to the topic: " + topicName);
        } catch (Exception e) {
            logger.severe("Failed to serialize message: " + e.getMessage());
        }
    }


}

