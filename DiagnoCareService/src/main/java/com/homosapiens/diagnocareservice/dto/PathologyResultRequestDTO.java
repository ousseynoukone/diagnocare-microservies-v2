package com.homosapiens.diagnocareservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PathologyResultRequestDTO {
    @NotNull(message = "Pathology ID is required")
    private Long pathologyId;
    
    @NotNull(message = "Doctor ID is required")
    private Long doctorId;
    
    private BigDecimal diseaseScore;
    private String description;
    private String predictionId; // Optional, can be set when creating from prediction service
    private String localizedDiseaseName;
    private String localizedSpecialistLabel;
}
