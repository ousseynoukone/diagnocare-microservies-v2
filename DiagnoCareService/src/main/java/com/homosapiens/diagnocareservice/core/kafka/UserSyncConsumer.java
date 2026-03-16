package com.homosapiens.diagnocareservice.core.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homosapiens.diagnocareservice.dto.UserSyncEventDTO;
import com.homosapiens.diagnocareservice.model.entity.User;
import com.homosapiens.diagnocareservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class UserSyncConsumer {
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final Logger logger = Logger.getLogger(UserSyncConsumer.class.getName());

    @KafkaListener(topics = {"USER_REGISTERED", "USER_UPDATE", "USER_DELETED"}, groupId = "diagnocare-user-sync")
    public void handleUserSync(ConsumerRecord<String, String> record) {
        try {
            UserSyncEventDTO event = objectMapper.readValue(record.value(), UserSyncEventDTO.class);
            if (event.getId() == null) {
                logger.warning("User sync event missing id. Topic: " + record.topic());
                return;
            }

            if ("USER_DELETED".equals(record.topic())) {
                userRepository.findById(event.getId()).ifPresent(user -> {
                    user.setIsActive(false);
                    userRepository.save(user);
                });
                return;
            }

            User user = userRepository.findById(event.getId()).orElseGet(User::new);
            user.setId(event.getId());
            user.setEmail(event.getEmail());
            user.setFirstName(event.getFirstName());
            user.setLastName(event.getLastName());
            user.setPhoneNumber(event.getPhoneNumber());
            user.setLang(event.getLang());
            user.setIsActive(event.getActive() != null ? event.getActive() : true);
            userRepository.save(user);
        } catch (Exception e) {
            logger.severe("Failed to process user sync event: " + e.getMessage());
        }
    }
}
