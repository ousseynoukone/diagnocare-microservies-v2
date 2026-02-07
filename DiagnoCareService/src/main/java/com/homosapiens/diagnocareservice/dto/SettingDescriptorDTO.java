package com.homosapiens.diagnocareservice.dto;

import lombok.Data;

@Data
public class SettingDescriptorDTO {
    private String key;
    private String description;
    private String defaultValue;
}
