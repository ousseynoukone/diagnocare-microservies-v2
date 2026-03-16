package com.homosapiens.authservice.repository;

import com.homosapiens.authservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * User repository.
 * 
 * IMPORTANT: Do NOT use findUserByEmail() directly!
 * Since emails are encrypted in the database, always use UserLookupService.findUserByEmail()
 * which handles the hash calculation automatically.
 * 
 * This repository only exposes findByEmailHash() for internal use.
 */
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Finds a user by email hash (SHA-256).
     * This is the actual database query method.
     * 
     * For looking up users by plain email, use UserLookupService.findUserByEmail() instead.
     * 
     * @param emailHash The SHA-256 hash of the email
     * @return Optional containing the user if found
     */
    Optional<User> findByEmailHash(String emailHash);
}
