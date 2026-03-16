package com.homosapiens.diagnocareservice.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ConsultationSummaryDTO {
    private String patientName;
    private String symptomsDescription;
    private List<String> symptoms;
    private Integer symptomsCount;
    private Boolean hasRedFlags;
    private List<String> redFlags;
    private List<String> potentialPathologies;
    private List<PathologySummaryDTO> pathologyDetails;
    private String recommendedSpecialty;
    private List<String> questionsForDoctor;
    private String pdfUrl;
    private String language;
    private String generatedAt;
    private Boolean checkIn;
    private Long previousPredictionId;
    private String checkInStatus;
    private String checkInOutcome;
    private String worseReason;
    private BigDecimal previousBestScore;
    private BigDecimal currentBestScore;
    private BigDecimal bestScoreDelta;
    private Integer checkInCount;
    private List<TimelineEntryDTO> timeline;
}
