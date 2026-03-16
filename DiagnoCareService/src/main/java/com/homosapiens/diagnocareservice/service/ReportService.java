package com.homosapiens.diagnocareservice.service;

import com.homosapiens.diagnocareservice.dto.ReportDTO;
import com.homosapiens.diagnocareservice.dto.ReportRequestDTO;
import com.homosapiens.diagnocareservice.model.entity.Report;

import java.util.List;
import java.util.Optional;

public interface ReportService {
    Report createReport(ReportRequestDTO requestDTO);
    Report updateReport(Long id, ReportRequestDTO requestDTO);
    void deleteReport(Long id);
    Optional<Report> getReportById(Long id);
    List<Report> getReportsByUserId(Long userId);
    List<Report> getUncorrectedReports();
    Report markAsCorrected(Long id);
    ReportDTO convertToDTO(Report report);
    List<ReportDTO> convertToDTOList(List<Report> reports);
}
