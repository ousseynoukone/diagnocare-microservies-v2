package com.homosapiens.diagnocareservice.controller;

import com.homosapiens.diagnocareservice.dto.ConsultationSummaryDTO;
import com.homosapiens.diagnocareservice.service.ConsultationSummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;

@RestController
@RequestMapping("consultation-summaries")
@Tag(name = "Consultation Summary Management", description = "APIs for generating consultation summaries and PDFs")
@RequiredArgsConstructor
public class ConsultationSummaryController {

    private final ConsultationSummaryService consultationSummaryService;

    @GetMapping("/{predictionId}")
    @Operation(summary = "Get consultation summary data", description = "Retrieves consultation summary data for a prediction")
    public ResponseEntity<ConsultationSummaryDTO> getSummary(
            @Parameter(description = "Prediction ID") @PathVariable Long predictionId) {
        ConsultationSummaryDTO summary = consultationSummaryService.generateSummaryData(predictionId);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/{predictionId}/pdf")
    @Operation(summary = "Generate PDF consultation summary", description = "Generates and downloads a PDF consultation summary")
    public ResponseEntity<byte[]> generatePdfSummary(
            @Parameter(description = "Prediction ID") @PathVariable Long predictionId) {
        ByteArrayOutputStream pdfStream = consultationSummaryService.generatePdfSummary(predictionId);
        
        // Save PDF and get URL
        String pdfUrl = consultationSummaryService.savePdfAndGetUrl(pdfStream, predictionId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "consultation-summary-" + predictionId + ".pdf");
        headers.setContentLength(pdfStream.size());
        
        return new ResponseEntity<>(pdfStream.toByteArray(), headers, HttpStatus.OK);
    }
}
