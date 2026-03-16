package com.homosapiens.diagnocareservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MLSymptomExtractionResponseDTO {
    private List<String> symptoms;
    private String language;
}
