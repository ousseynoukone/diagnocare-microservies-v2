package com.homosapiens.diagnocareservice.controller;

import com.homosapiens.diagnocareservice.dto.DoctorDTO;
import com.homosapiens.diagnocareservice.model.entity.Doctor;
import com.homosapiens.diagnocareservice.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("doctors")
@Tag(name = "Doctor Management", description = "Read-only APIs for consulting doctors/specialists. Doctors are automatically created by the system based on ML predictions.")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping("/{id}")
    @Operation(summary = "Get doctor by ID", description = "Retrieves a doctor/specialist by its ID. Doctors are automatically created by the system.")
    public ResponseEntity<DoctorDTO> getDoctorById(
            @Parameter(description = "Doctor ID") @PathVariable Long id) {
        return doctorService.getDoctorById(id)
                .map(doctor -> ResponseEntity.ok(doctorService.convertToDTO(doctor)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get all doctors", description = "Retrieves all doctors/specialists in the system. These are automatically created based on ML prediction results.")
    public ResponseEntity<List<DoctorDTO>> getAllDoctors() {
        List<Doctor> doctors = doctorService.getAllDoctors();
        return ResponseEntity.ok(doctorService.convertToDTOList(doctors));
    }

    @GetMapping("/search")
    @Operation(summary = "Search doctors by specialty", description = "Searches doctors/specialists by specialty (case-insensitive). These are automatically created by the system.")
    public ResponseEntity<List<DoctorDTO>> searchDoctorsBySpecialty(
            @Parameter(description = "Specialty search term") @RequestParam String specialty) {
        List<Doctor> doctors = doctorService.searchDoctorsBySpecialty(specialty);
        return ResponseEntity.ok(doctorService.convertToDTOList(doctors));
    }
}
