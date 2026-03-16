package com.homosapiens.authservice.core.util;

import org.springframework.stereotype.Service;

/**
 * Centralized service for calculating email hashes.
 * This ensures consistent hash calculation across the entire application.
 * Since emails are encrypted in the database, we use SHA-256 hash for lookups.
 * 
 * This service delegates to EmailHashUtil to avoid code duplication.
 */
@Service
public class EmailHashService {

    /**
     * Calculates SHA-256 hash of email for uniqueness checks and lookups.
     * This allows checking email uniqueness and finding users even when email is encrypted.
     * 
     * @param email The email address to hash
     * @return SHA-256 hash as hexadecimal string (64 characters)
     */
    public String calculateEmailHash(String email) {
        return EmailHashUtil.calculateEmailHash(email);
    }
}
