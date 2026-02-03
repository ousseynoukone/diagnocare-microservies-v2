package com.homosapiens.diagnocareservice.model.entity;

import com.homosapiens.diagnocareservice.model.entity.enums.GenderEnum;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Data
@Entity
@Table(name = "patient_medical_profiles")
public class PatientMedicalProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Boolean isSmoking;

    @Column
    private int age;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private GenderEnum gender;

    @Column
    private Float weight;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = true , name = "mean_bp")
    private Float meanBloodPressure;

    @Column(nullable = true , name = "mean_chol")
    private Float meanCholesterol;

    @Column
    private Boolean sedentary;

    @Column
    private int bmi;

    @Column
    private Boolean alcohol;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> familyAntecedents;






}

