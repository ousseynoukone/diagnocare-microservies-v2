package com.homosapiens.diagnocareservice.dto;

import com.homosapiens.diagnocareservice.model.entity.Prediction;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PredictionCreationResult {
    private Prediction prediction;
    private MLPredictionResponseDTO mlResponse;
}
