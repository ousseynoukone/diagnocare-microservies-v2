package com.homosapiens.diagnocareservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CheckInCreateRequestDTO {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Previous prediction ID is required")
    private Long previousPredictionId;

    @NotEmpty(message = "At least one symptom label is required")
    private List<String> symptomLabels;
}
