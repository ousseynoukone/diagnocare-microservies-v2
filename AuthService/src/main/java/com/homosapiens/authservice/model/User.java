package com.homosapiens.authservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    private String email;

    @NotNull(message = "First name must not be null")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotNull(message = "Last name must not be null")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @NotNull(message = "Phone number must not be null")
    @Pattern(regexp = "\\+?[0-9]{7,15}", message = "Phone number must be valid")
    private String phoneNumber;

    @Column(length = 5)
    private String lang = "fr";

    @NotNull(message = "Password must not be null")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

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
    private void normalizeLang() {
        if (lang == null || lang.trim().isEmpty()) {
            lang = "fr";
            return;
        }
        lang = lang.trim().toLowerCase();
        if (!lang.equals("fr") && !lang.equals("en")) {
            lang = "fr";
        }
    }
}
