package com.homosapiens.diagnocareservice.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PathologyResultDTO {
    private Long id;
    private BigDecimal diseaseScore;
    private String description;
    private Long pathologyId;
    private String pathologyName;
    private Long doctorId;
    private String doctorSpecialistLabel;
    private String predictionId;
}
