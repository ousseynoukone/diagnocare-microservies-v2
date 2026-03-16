package com.homosapiens.authservice.service;

import com.homosapiens.authservice.core.util.EmailHashService;
import com.homosapiens.authservice.model.User;
import com.homosapiens.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service for looking up users by email.
 * This service handles the complexity of finding users when emails are encrypted in the database.
 * 
 * IMPORTANT: Always use this service instead of directly calling UserRepository.findUserByEmail()
 * to ensure proper handling of encrypted emails.
 */
@Service
@RequiredArgsConstructor
public class UserLookupService {

    private final UserRepository userRepository;
    private final EmailHashService emailHashService;

    /**
     * Finds a user by email address.
     * Since emails are encrypted in the database, this method:
     * 1. Calculates the SHA-256 hash of the email
     * 2. Looks up the user by email hash
     * 
     * @param email The plain text email address
     * @return Optional containing the user if found
     */
    public Optional<User> findUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        String emailHash = emailHashService.calculateEmailHash(email);
        return userRepository.findByEmailHash(emailHash);
    }

    /**
     * Checks if a user exists with the given email.
     * 
     * @param email The plain text email address
     * @return true if user exists, false otherwise
     */
    public boolean existsByEmail(String email) {
        return findUserByEmail(email).isPresent();
    }
}
