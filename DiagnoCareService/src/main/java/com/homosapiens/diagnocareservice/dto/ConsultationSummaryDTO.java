package com.homosapiens.diagnocareservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class ConsultationSummaryDTO {
    private String patientName;
    private String symptomsDescription;
    private List<String> symptoms;
    private Boolean hasRedFlags;
    private List<String> redFlags;
    private List<String> potentialPathologies;
    private String recommendedSpecialty;
    private List<String> questionsForDoctor;
    private String pdfUrl;
}
