package com.homosapiens.diagnocareservice.model.entity.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ScheduleSlotUpdateDto {
    private Boolean isActive;

    @Schema(hidden = true)
    // Custom validation to ensure at least one field is provided
    public boolean isValid() {
        return isActive != null;
    }
} 