package com.homosapiens.diagnocareservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SettingUpdateRequestDTO {
    @NotBlank(message = "Value is required")
    private String value;
}
