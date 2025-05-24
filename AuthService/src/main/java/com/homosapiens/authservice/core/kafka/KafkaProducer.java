package com.homosapiens.authservice.core.kafka;
import java.util.logging.Logger;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String TOPIC_NAME= "edgerunners";
    private final Logger logger = Logger.getLogger(KafkaProducer.class.getName());

    public void sendMessage(String message) {
        kafkaTemplate.send(TOPIC_NAME, message);

        logger.info("Message " + message + " has been successfully sent to the topic: " + TOPIC_NAME);

    }
}
