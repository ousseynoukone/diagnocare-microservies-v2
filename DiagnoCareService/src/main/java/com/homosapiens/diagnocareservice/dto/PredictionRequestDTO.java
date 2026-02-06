package com.homosapiens.diagnocareservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PredictionRequestDTO {
    @NotNull(message = "Session symptom ID is required")
    private Long sessionSymptomId;
    
    private BigDecimal bestScore;
    private Boolean isRedAlert;
    private String comment;
    private Long previousPredictionId;
    private List<PathologyResultRequestDTO> pathologyResults;
}
