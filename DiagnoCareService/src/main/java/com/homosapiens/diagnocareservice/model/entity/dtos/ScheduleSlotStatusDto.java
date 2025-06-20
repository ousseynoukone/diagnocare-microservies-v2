package com.homosapiens.diagnocareservice.model.entity.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ScheduleSlotStatusDto {
    private Boolean isActive;
    
    // Custom validation to ensure at least one field is provided
    public boolean isValid() {
        return isActive != null;
    }
} 