package com.homosapiens.diagnocareservice.service;

import com.homosapiens.diagnocareservice.core.exception.AppException;
import com.homosapiens.diagnocareservice.model.entity.User;
import com.homosapiens.diagnocareservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserDataAnonymizationService {

    private final UserRepository userRepository;

    /**
     * Anonymizes user PII while preserving health data for research purposes.
     * This method removes personally identifiable information (email, name, phone, address)
     * but keeps all health-related data (symptoms, predictions, check-ins, reports, medical profile).
     *
     * @param userId The ID of the user to anonymize
     * @throws AppException if user is not found
     */
    public void anonymizeUserData(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "User not found"));

        // Generate unique anonymized identifier
        String anonymizedId = UUID.randomUUID().toString();

        // Anonymize PII fields
        user.setEmail("deleted_" + anonymizedId + "@anonymized.local");
        user.setFirstName("ANONYMOUS_USER");
        user.setLastName("ANONYMOUS_USER");
        user.setPhoneNumber(null);
        user.setAddress(null);
        
        // Set user as inactive (cannot login)
        user.setIsActive(false);

        // Note: Health data (Predictions, SessionSymptoms, CheckIns, Reports, PatientMedicalProfile)
        // are NOT modified - they remain linked to the anonymized user for research purposes

        // Save anonymized user
        userRepository.save(user);
    }
}
