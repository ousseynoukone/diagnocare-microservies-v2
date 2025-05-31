package com.homosapiens.diagnocareservice.model.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "patient_medical_profiles")
public class PatientMedicalProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Boolean isSmoking;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    private Float weight;
    private Float height;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String identifiant_1;
}

enum Gender {
    MALE,
    FEMALE
}