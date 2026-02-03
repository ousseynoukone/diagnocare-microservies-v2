package com.homosapiens.diagnocareservice.controller;

import com.homosapiens.diagnocareservice.dto.ReportDTO;
import com.homosapiens.diagnocareservice.dto.ReportRequestDTO;
import com.homosapiens.diagnocareservice.model.entity.Report;
import com.homosapiens.diagnocareservice.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("reports")
@Tag(name = "Report Management", description = "APIs for managing user reports/feedback")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    @Operation(summary = "Create a new report", description = "Creates a new report/feedback from a user")
    public ResponseEntity<ReportDTO> createReport(
            @Valid @RequestBody ReportRequestDTO requestDTO) {
        Report created = reportService.createReport(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(reportService.convertToDTO(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a report", description = "Updates an existing report by ID")
    public ResponseEntity<ReportDTO> updateReport(
            @Parameter(description = "Report ID") @PathVariable Long id,
            @Valid @RequestBody ReportRequestDTO requestDTO) {
        try {
            Report updated = reportService.updateReport(id, requestDTO);
            return ResponseEntity.ok(reportService.convertToDTO(updated));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a report", description = "Deletes a report by ID")
    public ResponseEntity<Void> deleteReport(
            @Parameter(description = "Report ID") @PathVariable Long id) {
        reportService.deleteReport(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get report by ID", description = "Retrieves a report by its ID")
    public ResponseEntity<ReportDTO> getReportById(
            @Parameter(description = "Report ID") @PathVariable Long id) {
        return reportService.getReportById(id)
                .map(report -> ResponseEntity.ok(reportService.convertToDTO(report)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get reports by user ID", description = "Retrieves all reports for a specific user")
    public ResponseEntity<List<ReportDTO>> getReportsByUserId(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        List<Report> reports = reportService.getReportsByUserId(userId);
        return ResponseEntity.ok(reportService.convertToDTOList(reports));
    }

    @GetMapping("/uncorrected")
    @Operation(summary = "Get uncorrected reports", description = "Retrieves all reports that haven't been corrected yet")
    public ResponseEntity<List<ReportDTO>> getUncorrectedReports() {
        List<Report> reports = reportService.getUncorrectedReports();
        return ResponseEntity.ok(reportService.convertToDTOList(reports));
    }

    @PutMapping("/{id}/mark-corrected")
    @Operation(summary = "Mark report as corrected", description = "Marks a report as corrected")
    public ResponseEntity<ReportDTO> markReportAsCorrected(
            @Parameter(description = "Report ID") @PathVariable Long id) {
        Report report = reportService.markAsCorrected(id);
        return ResponseEntity.ok(reportService.convertToDTO(report));
    }
}
