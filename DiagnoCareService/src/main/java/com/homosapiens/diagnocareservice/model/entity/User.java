package com.homosapiens.diagnocareservice.model.entity;

import com.homosapiens.diagnocareservice.core.security.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(name = "users")
public class User extends BaseEntity{

    @Column(length = 100)
    private String firstName;

    @Column(length = 100)
    private String lastName;

    private LocalDate birthDate;

    private Boolean gender;

    @Column(length = 255, unique = true)
    @Convert(converter = EncryptedStringConverter.class)
    private String email;

    @Column(name = "email_hash", unique = true, length = 64)
    private String emailHash;

    @Column(length = 255)
    @Convert(converter = EncryptedStringConverter.class)
    private String address;

    @Column(length = 13)
    @Convert(converter = EncryptedStringConverter.class)
    private String phoneNumber;

    @Column(length = 5)
    private String lang = "fr";

    private Boolean isActive;

    @PrePersist
    @PreUpdate
    private void normalizeFields() {
        // Normalize lang
        if (lang == null || lang.trim().isEmpty()) {
            lang = "fr";
            return;
        }
        lang = lang.trim().toLowerCase();
        if (!lang.equals("fr") && !lang.equals("en")) {
            lang = "fr";
        }

        // Calculate email hash for uniqueness checks (before encryption)
        // Note: This is done in the entity to ensure hash is always set before persistence
        // For lookups, always use UserLookupService.findUserByEmail()
        if (email != null && emailHash == null) {
            emailHash = com.homosapiens.diagnocareservice.core.util.EmailHashUtil.calculateEmailHash(email);
        }
    }

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    private List<Role> roles = new ArrayList<>();


    @OneToMany(cascade = CascadeType.ALL,fetch = FetchType.EAGER)
    private List<SessionSymptom> sessionSymptom;
} 