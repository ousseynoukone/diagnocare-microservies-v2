package com.homosapiens.authservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.homosapiens.authservice.core.security.EncryptedStringConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;

@Data
@Entity
@Table(name = "users")

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull(message = "Email must not be null")
    @Email(message = "Email should be valid")
    @Column(unique = true)
    @Convert(converter = EncryptedStringConverter.class)
    private String email;

    @Column(name = "email_hash", unique = true, nullable = false, length = 64)
    private String emailHash;

    @NotNull(message = "First name must not be null")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotNull(message = "Last name must not be null")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @Pattern(regexp = "^$|\\+?[0-9]{7,15}", message = "Phone number must be valid")
    @Convert(converter = EncryptedStringConverter.class)
    private String phoneNumber;

    @Column(length = 5)
    private String lang = "fr";

    @NotNull(message = "Password must not be null")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Column(name = "privacy_policy_accepted", nullable = false)
    private Boolean privacyPolicyAccepted = false;

    @Column(name = "terms_accepted", nullable = false)
    private Boolean termsAccepted = false;

    @Column(name = "consent_date")
    private Date consentDate;

    @Column(name = "consent_version", length = 50)
    private String consentVersion;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;

    @LastModifiedDate
    @Column(updatable = false, name = "updated_at")
    private Date updatedAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "userId", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    private List<Role> roles = new ArrayList<>();

    @PrePersist
    @PreUpdate
    private void normalizeFields() {
        // Normalize lang
        if (lang == null || lang.trim().isEmpty()) {
            lang = "fr";
        } else {
            lang = lang.trim().toLowerCase();
            if (!lang.equals("fr") && !lang.equals("en")) {
                lang = "fr";
            }
        }

        // Normalize phone number
        if (phoneNumber != null) {
            // Trim and remove common separators so validation is stable across clients (e.g. "04 20 37 13 30")
            phoneNumber = phoneNumber.trim()
                    .replace(" ", "")
                    .replace("-", "")
                    .replace("(", "")
                    .replace(")", "");
            // Convert empty string to null (phone number is optional)
            if (phoneNumber.isEmpty()) {
                phoneNumber = null;
            }
        }

        // Calculate email hash for uniqueness checks (before encryption)
        // Note: This is done in the entity to ensure hash is always set before persistence
        // For lookups, always use UserLookupService.findUserByEmail()
        if (email != null && emailHash == null) {
            emailHash = com.homosapiens.authservice.core.util.EmailHashUtil.calculateEmailHash(email);
        }
    }
}
