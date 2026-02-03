package com.homosapiens.diagnocareservice.controller;

import com.homosapiens.diagnocareservice.dto.PatientMedicalProfileDTO;
import com.homosapiens.diagnocareservice.dto.PatientMedicalProfileRequestDTO;
import com.homosapiens.diagnocareservice.service.PatientMedicalProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("patient-profiles")
@Tag(name = "Patient Medical Profile Management", description = "APIs for managing patient medical profiles")
@RequiredArgsConstructor
public class PatientMedicalProfileController {

    private final PatientMedicalProfileService profileService;

    @PostMapping
    @Operation(summary = "Create or update patient medical profile", description = "Creates a new or updates an existing patient medical profile")
    public ResponseEntity<PatientMedicalProfileDTO> createOrUpdateProfile(
            @Valid @RequestBody PatientMedicalProfileRequestDTO requestDTO) {
        var profile = profileService.createOrUpdateProfile(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(profileService.convertToDTO(profile));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete patient medical profile", description = "Deletes a patient medical profile by ID")
    public ResponseEntity<Void> deleteProfile(
            @Parameter(description = "Profile ID") @PathVariable Long id) {
        profileService.deleteProfile(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get patient medical profile by user ID", description = "Retrieves a patient medical profile for a specific user")
    public ResponseEntity<PatientMedicalProfileDTO> getProfileByUserId(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        return profileService.getProfileByUserId(userId)
                .map(profile -> ResponseEntity.ok(profileService.convertToDTO(profile)))
                .orElse(ResponseEntity.notFound().build());
    }
}
