package com.homosapiens.diagnocareservice.dto;

import com.homosapiens.diagnocareservice.model.entity.enums.GenderEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Data
public class PatientMedicalProfileRequestDTO {
    @NotNull(message = "User ID is required")
    private Long userId;
    
    private Boolean isSmoking;
    private int age;
    private GenderEnum gender;
    private Float weight;
    private Float meanBloodPressure;
    private Float meanCholesterol;
    private Boolean sedentary;
    private int bmi;
    private Boolean alcohol;
    private Set<String> familyAntecedents;
}
