package com.homosapiens.diagnocareservice.service;

import com.homosapiens.diagnocareservice.dto.ConsultationSummaryDTO;

import java.io.ByteArrayOutputStream;

public interface ConsultationSummaryService {
    ByteArrayOutputStream generatePdfSummary(Long predictionId);
    ConsultationSummaryDTO generateSummaryData(Long predictionId);
    String savePdfAndGetUrl(ByteArrayOutputStream pdfStream, Long predictionId);
}
