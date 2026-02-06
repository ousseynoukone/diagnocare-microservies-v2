package com.homosapiens.diagnocareservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MLPredictionRequestDTO {
    private List<String> symptoms;
    private Integer age;
    private Float weight;
    private Float bmi;
    private Float tension_moyenne;
    private Float cholesterole_moyen;
    private String gender;
    private String blood_pressure;
    private String cholesterol_level;
    private String outcome_variable;
    private String smoking;
    private String alcohol;
    private String sedentarite;
    private String family_history;
    private String language; // "fr" or "en", default: "fr"
}
