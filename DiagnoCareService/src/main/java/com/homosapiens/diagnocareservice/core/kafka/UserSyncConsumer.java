package com.homosapiens.diagnocareservice.core.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homosapiens.diagnocareservice.dto.UserSyncEventDTO;
import com.homosapiens.diagnocareservice.model.entity.User;
import com.homosapiens.diagnocareservice.repository.UserRepository;
import com.homosapiens.diagnocareservice.service.UserDataAnonymizationService;
import com.homosapiens.diagnocareservice.service.UserLookupService;
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
    private final UserLookupService userLookupService;
    private final UserDataAnonymizationService userDataAnonymizationService;
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
                try {
                    userDataAnonymizationService.anonymizeUserData(event.getId());
                    logger.info("User " + event.getId() + " anonymized successfully");
                } catch (Exception e) {
                    logger.severe("Failed to anonymize user " + event.getId() + ": " + e.getMessage());
                }
                return;
            }

            // Try to find user by ID first
            User user = userRepository.findById(event.getId()).orElse(null);
            
            // If not found by ID, try to find by email using UserLookupService (handles encrypted email)
            if (user == null && event.getEmail() != null) {
                user = userLookupService.findUserByEmail(event.getEmail()).orElse(null);
            }
            
            // If still not found, create a new user
            if (user == null) {
                user = new User();
            }
            
            // Update user fields
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
