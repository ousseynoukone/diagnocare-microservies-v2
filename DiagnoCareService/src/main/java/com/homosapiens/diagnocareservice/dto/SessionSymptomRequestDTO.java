package com.homosapiens.diagnocareservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SessionSymptomRequestDTO {
    @NotNull(message = "User ID is required")
    private Long userId;
    
    private String rawDescription;

    private List<Long> symptomIds;

    private List<String> symptomLabels;
}
