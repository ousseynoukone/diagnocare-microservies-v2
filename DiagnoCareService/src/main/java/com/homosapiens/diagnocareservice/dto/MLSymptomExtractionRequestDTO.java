package com.homosapiens.diagnocareservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MLSymptomExtractionRequestDTO {
    private String raw_description;
    private String language; // "fr" or "en", default: "fr"
}
