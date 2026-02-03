package com.homosapiens.diagnocareservice.service;

import com.homosapiens.diagnocareservice.dto.PatientMedicalProfileDTO;
import com.homosapiens.diagnocareservice.dto.PatientMedicalProfileRequestDTO;
import com.homosapiens.diagnocareservice.model.entity.PatientMedicalProfile;

import java.util.Optional;

public interface PatientMedicalProfileService {
    PatientMedicalProfile createOrUpdateProfile(PatientMedicalProfileRequestDTO requestDTO);
    void deleteProfile(Long id);
    Optional<PatientMedicalProfile> getProfileByUserId(Long userId);
    PatientMedicalProfileDTO convertToDTO(PatientMedicalProfile profile);
}
