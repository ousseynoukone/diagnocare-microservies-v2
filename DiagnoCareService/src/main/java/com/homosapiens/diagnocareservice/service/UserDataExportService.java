package com.homosapiens.diagnocareservice.service;

import com.homosapiens.diagnocareservice.core.exception.AppException;
import com.homosapiens.diagnocareservice.dto.*;
import com.homosapiens.diagnocareservice.model.entity.Prediction;
import com.homosapiens.diagnocareservice.model.entity.Report;
import com.homosapiens.diagnocareservice.model.entity.SessionSymptom;
import com.homosapiens.diagnocareservice.model.entity.User;
import com.homosapiens.diagnocareservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserDataExportService {

    private final UserRepository userRepository;
    private final PatientMedicalProfileService patientMedicalProfileService;
    private final SessionSymptomService sessionSymptomService;
    private final PredictionService predictionService;
    private final CheckInService checkInService;
    private final ReportService reportService;

    public UserDataExportDTO exportUserData(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "User not found"));

        // Get medical profile
        PatientMedicalProfileDTO medicalProfile = patientMedicalProfileService.getProfileByUserId(userId)
                .map(patientMedicalProfileService::convertToDTO)
                .orElse(null);

        // Get session symptoms
        List<SessionSymptom> sessionSymptoms = sessionSymptomService.getSessionSymptomsByUserId(userId);
        List<SessionSymptomDTO> sessionSymptomDTOs = sessionSymptomService.convertToDTOList(sessionSymptoms);

        // Get predictions
        List<Prediction> predictions = predictionService.getPredictionsByUserId(userId);
        List<PredictionDTO> predictionDTOs = predictionService.convertToDTOList(predictions);

        // Get check-ins
        List<CheckInResponseDTO> checkIns = checkInService.getCheckInsByUser(userId);

        // Get reports
        List<Report> reports = reportService.getReportsByUserId(userId);
        List<ReportDTO> reportDTOs = reportService.convertToDTOList(reports);

        // Build export DTO
        return UserDataExportDTO.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .birthDate(user.getBirthDate())
                .gender(user.getGender())
                .address(user.getAddress())
                .phoneNumber(user.getPhoneNumber())
                .lang(user.getLang())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedDate())
                .updatedAt(user.getUpdatedDate())
                .medicalProfile(medicalProfile)
                .sessionSymptoms(sessionSymptomDTOs)
                .predictions(predictionDTOs)
                .checkIns(checkIns)
                .reports(reportDTOs)
                .exportDate(LocalDateTime.now())
                .exportVersion("v1.0")
                .build();
    }
}
