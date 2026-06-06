package com.homosapiens.diagnocareservice.dto;

import com.homosapiens.diagnocareservice.model.entity.enums.GenderEnum;
import lombok.Data;

import java.util.Set;

@Data
public class PatientMedicalProfileDTO {
    private Long id;
    private Boolean isSmoking;
    private Integer age;
    private GenderEnum gender;
    private Float weight;
    private Float meanBloodPressure;
    private Float meanCholesterol;
    private Boolean sedentary;
    private Integer bmi;
    private Boolean alcohol;
    private Set<String> familyAntecedents;
    private Long userId;
}
