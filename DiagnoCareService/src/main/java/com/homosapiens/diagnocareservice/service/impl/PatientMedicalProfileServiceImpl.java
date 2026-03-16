package com.homosapiens.diagnocareservice.service.impl;

import com.homosapiens.diagnocareservice.core.exception.AppException;
import com.homosapiens.diagnocareservice.dto.PatientMedicalProfileDTO;
import com.homosapiens.diagnocareservice.dto.PatientMedicalProfileRequestDTO;
import com.homosapiens.diagnocareservice.model.entity.PatientMedicalProfile;
import com.homosapiens.diagnocareservice.model.entity.User;
import com.homosapiens.diagnocareservice.repository.PatientMedicalProfileRepository;
import com.homosapiens.diagnocareservice.repository.UserRepository;
import com.homosapiens.diagnocareservice.service.PatientMedicalProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class PatientMedicalProfileServiceImpl implements PatientMedicalProfileService {

    private final PatientMedicalProfileRepository profileRepository;
    private final UserRepository userRepository;

    @Override
    public PatientMedicalProfile createOrUpdateProfile(PatientMedicalProfileRequestDTO requestDTO) {
        User user = userRepository.findById(requestDTO.getUserId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, 
                        "User not found with id: " + requestDTO.getUserId()));

        Optional<PatientMedicalProfile> existingProfile = profileRepository.findByUserId(requestDTO.getUserId());
        
        PatientMedicalProfile profile;
        if (existingProfile.isPresent()) {
            profile = existingProfile.get();
        } else {
            profile = new PatientMedicalProfile();
            profile.setUser(user);
        }

        profile.setIsSmoking(requestDTO.getIsSmoking());
        profile.setAge(requestDTO.getAge());
        profile.setGender(requestDTO.getGender());
        profile.setWeight(requestDTO.getWeight());
        profile.setMeanBloodPressure(requestDTO.getMeanBloodPressure());
        profile.setMeanCholesterol(requestDTO.getMeanCholesterol());
        profile.setSedentary(requestDTO.getSedentary());
        profile.setBmi(requestDTO.getBmi());
        profile.setAlcohol(requestDTO.getAlcohol());
        profile.setFamilyAntecedents(requestDTO.getFamilyAntecedents());

        return profileRepository.save(profile);
    }

    @Override
    public void deleteProfile(Long id) {
        profileRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PatientMedicalProfile> getProfileByUserId(Long userId) {
        return profileRepository.findByUserId(userId);
    }

    @Override
    public PatientMedicalProfileDTO convertToDTO(PatientMedicalProfile profile) {
        PatientMedicalProfileDTO dto = new PatientMedicalProfileDTO();
        dto.setId(profile.getId());
        dto.setIsSmoking(profile.getIsSmoking());
        dto.setAge(profile.getAge());
        dto.setGender(profile.getGender());
        dto.setWeight(profile.getWeight());
        dto.setMeanBloodPressure(profile.getMeanBloodPressure());
        dto.setMeanCholesterol(profile.getMeanCholesterol());
        dto.setSedentary(profile.getSedentary());
        dto.setBmi(profile.getBmi());
        dto.setAlcohol(profile.getAlcohol());
        dto.setFamilyAntecedents(profile.getFamilyAntecedents());
        dto.setUserId(profile.getUser().getId());
        return dto;
    }
}
