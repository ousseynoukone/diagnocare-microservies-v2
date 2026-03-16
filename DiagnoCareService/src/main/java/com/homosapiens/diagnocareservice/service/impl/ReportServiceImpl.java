package com.homosapiens.diagnocareservice.service.impl;

import com.homosapiens.diagnocareservice.core.exception.AppException;
import com.homosapiens.diagnocareservice.dto.ReportDTO;
import com.homosapiens.diagnocareservice.dto.ReportRequestDTO;
import com.homosapiens.diagnocareservice.model.entity.Prediction;
import com.homosapiens.diagnocareservice.model.entity.Report;
import com.homosapiens.diagnocareservice.model.entity.User;
import com.homosapiens.diagnocareservice.repository.ReportRepository;
import com.homosapiens.diagnocareservice.repository.UserRepository;
import com.homosapiens.diagnocareservice.repository.PredictionRepository;
import com.homosapiens.diagnocareservice.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final PredictionRepository predictionRepository;

    @Override
    public Report createReport(ReportRequestDTO requestDTO) {
        User user = userRepository.findById(requestDTO.getUserId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, 
                        "User not found with id: " + requestDTO.getUserId()));

        Report report = new Report();
        report.setUser(user);
        report.setTitle(requestDTO.getTitle());
        report.setComment(requestDTO.getComment());
        
        if (requestDTO.getPredictionId() != null) {
            Prediction prediction = predictionRepository.findById(requestDTO.getPredictionId())
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, 
                            "Prediction not found with id: " + requestDTO.getPredictionId()));
            report.setPrediction(prediction);
        }
        
        report.setReportDate(LocalDateTime.now());
        report.setIsCorrected(false);

        return reportRepository.save(report);
    }

    @Override
    public Report updateReport(Long id, ReportRequestDTO requestDTO) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, 
                        "Report not found with id: " + id));

        if (requestDTO.getTitle() != null) {
            report.setTitle(requestDTO.getTitle());
        }
        if (requestDTO.getComment() != null) {
            report.setComment(requestDTO.getComment());
        }
        if (requestDTO.getPredictionId() != null) {
            Prediction prediction = predictionRepository.findById(requestDTO.getPredictionId())
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, 
                            "Prediction not found with id: " + requestDTO.getPredictionId()));
            report.setPrediction(prediction);
        }

        return reportRepository.save(report);
    }

    @Override
    public void deleteReport(Long id) {
        reportRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Report> getReportById(Long id) {
        return reportRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Report> getReportsByUserId(Long userId) {
        return reportRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Report> getUncorrectedReports() {
        return reportRepository.findByIsCorrected(false);
    }

    @Override
    public Report markAsCorrected(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, 
                        "Report not found with id: " + id));
        report.setIsCorrected(true);
        return reportRepository.save(report);
    }

    @Override
    public ReportDTO convertToDTO(Report report) {
        ReportDTO dto = new ReportDTO();
        dto.setId(report.getId());
        dto.setTitle(report.getTitle());
        dto.setComment(report.getComment());
        dto.setReportDate(report.getReportDate());
        dto.setIsCorrected(report.getIsCorrected());
        dto.setUserId(report.getUser().getId());
        dto.setPredictionId(report.getPrediction() != null ? report.getPrediction().getId() : null);
        return dto;
    }

    @Override
    public List<ReportDTO> convertToDTOList(List<Report> reports) {
        return reports.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}
