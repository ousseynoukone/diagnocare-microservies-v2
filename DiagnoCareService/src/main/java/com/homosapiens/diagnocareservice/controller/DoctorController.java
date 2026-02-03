package com.homosapiens.diagnocareservice.controller;

import com.homosapiens.diagnocareservice.dto.DoctorDTO;
import com.homosapiens.diagnocareservice.model.entity.Doctor;
import com.homosapiens.diagnocareservice.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("doctors")
@Tag(name = "Doctor Management", description = "APIs for managing doctors and specialists")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @PostMapping
    @Operation(summary = "Create a new doctor", description = "Creates a new doctor/specialist in the system")
    public ResponseEntity<DoctorDTO> createDoctor(@RequestBody Doctor doctor) {
        Doctor created = doctorService.createDoctor(doctor);
        return ResponseEntity.status(HttpStatus.CREATED).body(doctorService.convertToDTO(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a doctor", description = "Updates an existing doctor by ID")
    public ResponseEntity<DoctorDTO> updateDoctor(
            @Parameter(description = "Doctor ID") @PathVariable Long id,
            @RequestBody Doctor doctor) {
        try {
            Doctor updated = doctorService.updateDoctor(id, doctor);
            return ResponseEntity.ok(doctorService.convertToDTO(updated));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a doctor", description = "Deletes a doctor by ID")
    public ResponseEntity<Void> deleteDoctor(
            @Parameter(description = "Doctor ID") @PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get doctor by ID", description = "Retrieves a doctor by its ID")
    public ResponseEntity<DoctorDTO> getDoctorById(
            @Parameter(description = "Doctor ID") @PathVariable Long id) {
        return doctorService.getDoctorById(id)
                .map(doctor -> ResponseEntity.ok(doctorService.convertToDTO(doctor)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get all doctors", description = "Retrieves all doctors in the system")
    public ResponseEntity<List<DoctorDTO>> getAllDoctors() {
        List<Doctor> doctors = doctorService.getAllDoctors();
        return ResponseEntity.ok(doctorService.convertToDTOList(doctors));
    }

    @GetMapping("/search")
    @Operation(summary = "Search doctors by specialty", description = "Searches doctors by specialty (case-insensitive)")
    public ResponseEntity<List<DoctorDTO>> searchDoctorsBySpecialty(
            @Parameter(description = "Specialty search term") @RequestParam String specialty) {
        List<Doctor> doctors = doctorService.searchDoctorsBySpecialty(specialty);
        return ResponseEntity.ok(doctorService.convertToDTOList(doctors));
    }
}
