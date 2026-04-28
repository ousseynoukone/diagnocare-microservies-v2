package com.homosapiens.diagnocareservice.dto;

import lombok.Data;
import java.util.List;

@Data
public class MLDiseasesMetadataDTO {
    private DiseasesMetadata diseases;
    private List<String> languages;

    @Data
    public static class DiseasesMetadata {
        private Integer count;
        private List<DiseaseItem> en;
        private List<DiseaseItem> fr;
    }

    @Data
    public static class DiseaseItem {
        private String key;
        private String label;
    }
}
