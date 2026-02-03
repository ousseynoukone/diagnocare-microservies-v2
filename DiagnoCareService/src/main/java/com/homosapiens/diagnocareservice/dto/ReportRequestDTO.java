package com.homosapiens.diagnocareservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportRequestDTO {
    @NotNull(message = "User ID is required")
    private Long userId;
    
    private String title;
    private String comment;
    private Long predictionId;
}
