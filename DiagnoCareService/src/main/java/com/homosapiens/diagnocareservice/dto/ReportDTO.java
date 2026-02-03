package com.homosapiens.diagnocareservice.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReportDTO {
    private Long id;
    private String title;
    private String comment;
    private LocalDateTime reportDate;
    private Boolean isCorrected;
    private Long userId;
    private Long predictionId;
}
