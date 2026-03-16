package com.homosapiens.diagnocareservice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MLTranslationResponseDTO {
    private String language;
    private List<String> symptoms;
    private List<String> diseases;
    private List<String> specialists;
}
