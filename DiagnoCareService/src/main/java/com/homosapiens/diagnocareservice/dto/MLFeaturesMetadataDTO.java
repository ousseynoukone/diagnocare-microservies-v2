package com.homosapiens.diagnocareservice.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class MLFeaturesMetadataDTO {
    private SymptomsMetadata symptoms;
    private FeaturesMetadata features;
    private List<String> languages;

    @Data
    public static class SymptomsMetadata {
        private Integer count;
        private List<SymptomItem> en;
        private List<SymptomItem> fr;
    }

    @Data
    public static class SymptomItem {
        private String id;
        private String label;
    }

    @Data
    public static class FeaturesMetadata {
        private List<Map<String, Object>> numeric;
        private List<Map<String, Object>> categorical;
    }
}
