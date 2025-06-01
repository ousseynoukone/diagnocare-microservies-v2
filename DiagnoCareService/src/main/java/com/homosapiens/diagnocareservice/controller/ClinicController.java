package com.homosapiens.diagnocareservice.controller;

import com.homosapiens.diagnocareservice.model.entity.Clinic;
import com.homosapiens.diagnocareservice.service.ClinicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/clinics")
public class ClinicController {
    private final ClinicService clinicService;

    @PostMapping
    public ResponseEntity<Clinic> createClinic(@Valid @RequestBody Clinic clinic) {
        return new ResponseEntity<>(clinicService.createClinic(clinic), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Clinic>> getAllClinics() {
        return ResponseEntity.ok(clinicService.findAllClinics());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Clinic> getClinicById(@PathVariable Long id) {
        return ResponseEntity.ok(clinicService.findClinicById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Clinic> updateClinic(@PathVariable Long id, @Valid @RequestBody Clinic clinic) {
        return ResponseEntity.ok(clinicService.updateClinic(id, clinic));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClinic(@PathVariable Long id) {
        clinicService.deleteClinic(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<Clinic>> getClinicsByCity(@PathVariable String city) {
        return ResponseEntity.ok(clinicService.findClinicsByCity(city));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Clinic>> searchClinicsByName(@RequestParam String name) {
        return ResponseEntity.ok(clinicService.searchClinicsByName(name));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Clinic>> getClinicsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(clinicService.findClinicsByUserId(userId));
    }
}
