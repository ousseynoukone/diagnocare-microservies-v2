package com.homosapiens.diagnocareservice.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Entity
@Table(name = "clinics")
public class Clinic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255)
    private String name;

    @Column(length = 2552)
    private String address;

    @Column(length = 255)
    private String city;

    @Column(length = 255)
    private String postalCode;

    @Column(length = 13)
    private String phoneNumber;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @PrePersist
    @PreUpdate
    private void validateUserRole() {
        if (user == null || user.getRole() != Role.DOCTOR) {
            throw new IllegalStateException("Clinic must be associated with a doctor");
        }
    }
}