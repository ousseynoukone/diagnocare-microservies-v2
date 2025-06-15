package com.homosapiens.diagnocareservice.controller;

import com.homosapiens.diagnocareservice.dto.request.ClinicRequestDTO;
import com.homosapiens.diagnocareservice.dto.response.ClinicResponseDTO;
import com.homosapiens.diagnocareservice.service.ClinicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clinics")
@RequiredArgsConstructor
public class ClinicController {
    private final ClinicService clinicService;

    @PostMapping
    public ResponseEntity<ClinicResponseDTO> createClinic(@Valid @RequestBody ClinicRequestDTO clinicDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clinicService.createClinic(clinicDTO));
    }

    @GetMapping
    public ResponseEntity<List<ClinicResponseDTO>> getAllClinics() {
        return ResponseEntity.ok(clinicService.findAllClinics());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClinicResponseDTO> getClinicById(@PathVariable Long id) {
        return ResponseEntity.ok(clinicService.findClinicById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClinicResponseDTO> updateClinic(
            @PathVariable Long id,
            @Valid @RequestBody ClinicRequestDTO clinicDTO) {
        return ResponseEntity.ok(clinicService.updateClinic(id, clinicDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClinic(@PathVariable Long id) {
        clinicService.deleteClinic(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<ClinicResponseDTO>> getClinicsByCity(@PathVariable String city) {
        return ResponseEntity.ok(clinicService.findClinicsByCity(city));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ClinicResponseDTO>> searchClinicsByName(@RequestParam String name) {
        return ResponseEntity.ok(clinicService.searchClinicsByName(name));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ClinicResponseDTO>> getClinicsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(clinicService.findClinicsByUserId(userId));
    }
}
