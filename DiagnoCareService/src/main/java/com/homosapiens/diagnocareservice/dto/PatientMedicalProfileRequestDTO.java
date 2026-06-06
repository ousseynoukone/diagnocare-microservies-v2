package com.homosapiens.diagnocareservice.dto;

import com.homosapiens.diagnocareservice.model.entity.enums.GenderEnum;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import lombok.Data;

import java.util.Set;

@Data
public class PatientMedicalProfileRequestDTO {
    @NotNull(message = "User ID is required")
    private Long userId;
    
    private Boolean isSmoking;

    @Min(value = 1, message = "L'âge doit être d'au moins 1 an")
    @Max(value = 120, message = "L'âge ne peut pas dépasser 120 ans")
    private Integer age;

    private GenderEnum gender;

    @DecimalMin(value = "2.0", message = "Le poids doit être d'au moins 2 kg")
    @DecimalMax(value = "300.0", message = "Le poids ne peut pas dépasser 300 kg")
    private Float weight;

    @DecimalMin(value = "50.0", message = "La tension artérielle moyenne doit être d'au moins 50 mmHg")
    @DecimalMax(value = "250.0", message = "La tension artérielle moyenne ne peut pas dépasser 250 mmHg")
    private Float meanBloodPressure;

    @DecimalMin(value = "50.0", message = "Le cholestérol moyen doit être d'au moins 50 mg/dL")
    @DecimalMax(value = "500.0", message = "Le cholestérol moyen ne peut pas dépasser 500 mg/dL")
    private Float meanCholesterol;

    private Boolean sedentary;
    private Integer bmi;
    private Boolean alcohol;
    private Set<String> familyAntecedents;
}
