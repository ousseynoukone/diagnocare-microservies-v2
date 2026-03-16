package com.homosapiens.diagnocareservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDataExportDTO {
    // User Profile
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private Boolean gender;
    private String address;
    private String phoneNumber;
    private String lang;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Medical Profile
    private PatientMedicalProfileDTO medicalProfile;

    // Health Data
    private List<SessionSymptomDTO> sessionSymptoms;
    private List<PredictionDTO> predictions;
    private List<CheckInResponseDTO> checkIns;
    private List<ReportDTO> reports;

    // Metadata
    private LocalDateTime exportDate;
    private String exportVersion;
}
