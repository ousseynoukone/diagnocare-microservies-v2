package com.homosapiens.diagnocareservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckInActivateRequestDTO {
    @NotNull
    private Long predictionId;
    @NotNull
    private Long userId;
}
