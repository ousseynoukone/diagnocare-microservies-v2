package com.homosapiens.diagnocareservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UrgentDiseaseRequestDTO {
    @NotBlank(message = "Disease name is required")
    private String diseaseName;
}
