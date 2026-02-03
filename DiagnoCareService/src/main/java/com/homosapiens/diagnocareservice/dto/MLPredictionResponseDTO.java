package com.homosapiens.diagnocareservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MLPredictionResponseDTO {
    private List<PredictionResult> predictions;
    private String language; // Language used for translations ("fr" or "en")
    private Metadata metadata;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PredictionResult {
        private Integer rank;
        private String disease; // EN (original, always present)
        private Double probability;
        private String specialist; // EN (original, always present)
        private Double specialist_probability;
        private String description; // Explanation in requested language
        // Translated names (conditional based on language)
        private String disease_fr; // French translation (if language="fr")
        private String specialist_fr; // French translation (if language="fr")
        private String disease_en; // English (if language="en", same as disease)
        private String specialist_en; // English (if language="en", same as specialist)
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Metadata {
        private Integer symptoms_count;
        private Map<String, String> profile_used;
    }
}
