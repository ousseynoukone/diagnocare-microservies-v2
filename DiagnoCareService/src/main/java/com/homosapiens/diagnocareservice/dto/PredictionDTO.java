package com.homosapiens.diagnocareservice.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PredictionDTO {
    private Long id;
    private BigDecimal bestScore;
    private String pdfReportUrl;
    private Boolean isRedAlert;
    private String comment;
    private Long sessionSymptomId;
    private Long previousPredictionId;
    private LocalDateTime createdAt;
}
