package com.homosapiens.diagnocareservice.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String firstName;

    @Column(length = 100)
    private String lastName;

    private LocalDate birthDate;

    private Boolean gender;

    @Column(length = 255, unique = true)
    private String email;

    @Column(length = 255)
    private String address;

    @Column(length = 13)
    private String phoneNumber;

    private Boolean isActive;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Role role;

    // Doctor specific fields
    @Column(length = 255)
    private String stripeCustomerId;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Clinic clinic;

    @Column(length = 11)
    private String npi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialization_id")
    private Specialization specialization;

    // Patient specific fields
    @Column(length = 13)
    private String healthAssuranceNumber;
    
    @Column(length = 50)
    private String credit;
} 