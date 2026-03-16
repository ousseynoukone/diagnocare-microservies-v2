package com.homosapiens.diagnocareservice.model.entity;

import com.homosapiens.diagnocareservice.core.security.*;
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
    @Convert(converter = EncryptedBooleanConverter.class)
    private Boolean isSmoking;

    @Column
    @Convert(converter = EncryptedIntegerConverter.class)
    private Integer age;

    @Column(length = 255)
    @Convert(converter = EncryptedGenderEnumConverter.class)
    private GenderEnum gender;

    @Column
    @Convert(converter = EncryptedFloatConverter.class)
    private Float weight;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = true , name = "mean_bp")
    @Convert(converter = EncryptedFloatConverter.class)
    private Float meanBloodPressure;

    @Column(nullable = true , name = "mean_chol")
    @Convert(converter = EncryptedFloatConverter.class)
    private Float meanCholesterol;

    @Column
    @Convert(converter = EncryptedBooleanConverter.class)
    private Boolean sedentary;

    @Column
    @Convert(converter = EncryptedIntegerConverter.class)
    private Integer bmi;

    @Column
    @Convert(converter = EncryptedBooleanConverter.class)
    private Boolean alcohol;

    @Column(columnDefinition = "TEXT")
    @Convert(converter = EncryptedSetStringConverter.class)
    private Set<String> familyAntecedents;






}

