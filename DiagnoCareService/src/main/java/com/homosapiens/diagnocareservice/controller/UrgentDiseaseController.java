package com.homosapiens.diagnocareservice.controller;

import com.homosapiens.diagnocareservice.dto.UrgentDiseaseDTO;
import com.homosapiens.diagnocareservice.dto.UrgentDiseaseRequestDTO;
import com.homosapiens.diagnocareservice.service.UrgentDiseaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("urgent-diseases")
@Tag(name = "Urgent Diseases", description = "APIs for managing urgent disease list")
@RequiredArgsConstructor
public class UrgentDiseaseController {

    private final UrgentDiseaseService urgentDiseaseService;

    @GetMapping
    @Operation(summary = "List urgent diseases")
    public ResponseEntity<List<UrgentDiseaseDTO>> getAll() {
        return ResponseEntity.ok(urgentDiseaseService.getAll());
    }

    @PostMapping
    @Operation(summary = "Add urgent disease")
    public ResponseEntity<UrgentDiseaseDTO> create(@Valid @RequestBody UrgentDiseaseRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(urgentDiseaseService.create(requestDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete urgent disease")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        urgentDiseaseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
