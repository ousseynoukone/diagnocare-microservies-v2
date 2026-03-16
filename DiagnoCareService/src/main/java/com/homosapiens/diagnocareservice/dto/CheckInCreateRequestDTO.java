package com.homosapiens.diagnocareservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CheckInCreateRequestDTO {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Previous prediction ID is required")
    private Long previousPredictionId;

    private List<Long> symptomIds;

    private List<String> symptomLabels;
}
