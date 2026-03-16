package com.homosapiens.diagnocareservice.core.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for calculating email hashes.
 * This is a static utility to avoid code duplication in entities and tests.
 * 
 * For service-level usage, prefer EmailHashService which is injectable.
 */
public final class EmailHashUtil {

    private EmailHashUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * Calculates SHA-256 hash of email for uniqueness checks.
     * This allows checking email uniqueness even when email is encrypted.
     * 
     * @param email The email address to hash
     * @return SHA-256 hash as hexadecimal string (64 characters)
     * @throws IllegalArgumentException if email is null or empty
     */
    public static String calculateEmailHash(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(email.toLowerCase().trim().getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
