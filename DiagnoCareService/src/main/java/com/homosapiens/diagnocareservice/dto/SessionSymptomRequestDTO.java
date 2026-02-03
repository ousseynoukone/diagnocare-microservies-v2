package com.homosapiens.diagnocareservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;

import java.util.List;

@Data
public class SessionSymptomRequestDTO {
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotBlank(message = "Description is required")
    private String rawDescription;

    private List<Long> symptomIds;
    
    private String language; // "fr" or "en", optional, default: "fr"
}
