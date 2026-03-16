package com.homosapiens.diagnocareservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionWithResultsResponse {
    private PredictionDTO prediction;
    private MLPredictionResponseDTO mlResults;
}
