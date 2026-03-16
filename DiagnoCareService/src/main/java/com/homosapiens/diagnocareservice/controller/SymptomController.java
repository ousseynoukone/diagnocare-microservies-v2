package com.homosapiens.diagnocareservice.controller;

import com.homosapiens.diagnocareservice.dto.SymptomDTO;
import com.homosapiens.diagnocareservice.model.entity.Symptom;
import com.homosapiens.diagnocareservice.service.SymptomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("symptoms")
@Tag(name = "Symptom Management", description = "APIs for managing symptoms")
@RequiredArgsConstructor
public class SymptomController {

    private final SymptomService symptomService;

    @PostMapping
    @Operation(summary = "Create a new symptom", description = "Creates a new symptom in the system")
    public ResponseEntity<SymptomDTO> createSymptom(@RequestBody Symptom symptom) {
        Symptom created = symptomService.createSymptom(symptom);
        return ResponseEntity.status(HttpStatus.CREATED).body(symptomService.convertToDTO(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a symptom", description = "Updates an existing symptom by ID")
    public ResponseEntity<SymptomDTO> updateSymptom(
            @Parameter(description = "Symptom ID") @PathVariable Long id,
            @RequestBody Symptom symptom) {
        try {
            Symptom updated = symptomService.updateSymptom(id, symptom);
            return ResponseEntity.ok(symptomService.convertToDTO(updated));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a symptom", description = "Deletes a symptom by ID")
    public ResponseEntity<Void> deleteSymptom(
            @Parameter(description = "Symptom ID") @PathVariable Long id) {
        symptomService.deleteSymptom(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get symptom by ID", description = "Retrieves a symptom by its ID")
    public ResponseEntity<SymptomDTO> getSymptomById(
            @Parameter(description = "Symptom ID") @PathVariable Long id) {
        return symptomService.getSymptomById(id)
                .map(symptom -> ResponseEntity.ok(symptomService.convertToDTO(symptom)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get all symptoms", description = "Retrieves all symptoms in the system")
    public ResponseEntity<List<SymptomDTO>> getAllSymptoms() {
        List<Symptom> symptoms = symptomService.getAllSymptoms();
        return ResponseEntity.ok(symptomService.convertToDTOList(symptoms));
    }

    @GetMapping("/search")
    @Operation(summary = "Search symptoms by label", description = "Searches symptoms by label (case-insensitive)")
    public ResponseEntity<List<SymptomDTO>> searchSymptoms(
            @Parameter(description = "Search term") @RequestParam String label) {
        List<Symptom> symptoms = symptomService.searchSymptomsByLabel(label);
        return ResponseEntity.ok(symptomService.convertToDTOList(symptoms));
    }
}
