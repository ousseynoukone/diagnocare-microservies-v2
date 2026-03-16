package com.homosapiens.diagnocareservice.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PathologySummaryDTO {
    private String pathologyName;
    private BigDecimal diseaseScore;
    private String description;
    private String specialist;
}
