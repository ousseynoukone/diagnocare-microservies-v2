package com.homosapiens.diagnocareservice.controller;

import com.homosapiens.diagnocareservice.dto.PathologyResultDTO;
import com.homosapiens.diagnocareservice.dto.PathologyResultRequestDTO;
import com.homosapiens.diagnocareservice.model.entity.PathologyResult;
import com.homosapiens.diagnocareservice.service.PathologyResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("pathology-results")
@Tag(name = "Pathology Result Management", description = "APIs for managing pathology results")
@RequiredArgsConstructor
public class PathologyResultController {

    private final PathologyResultService pathologyResultService;

    @PostMapping
    @Operation(summary = "Create a new pathology result", description = "Creates a new pathology result for a prediction")
    public ResponseEntity<PathologyResultDTO> createPathologyResult(
            @Valid @RequestBody PathologyResultRequestDTO requestDTO) {
        PathologyResult created = pathologyResultService.createPathologyResult(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(pathologyResultService.convertToDTO(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a pathology result", description = "Updates an existing pathology result by ID")
    public ResponseEntity<PathologyResultDTO> updatePathologyResult(
            @Parameter(description = "Pathology Result ID") @PathVariable Long id,
            @Valid @RequestBody PathologyResultRequestDTO requestDTO) {
        try {
            PathologyResult updated = pathologyResultService.updatePathologyResult(id, requestDTO);
            return ResponseEntity.ok(pathologyResultService.convertToDTO(updated));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a pathology result", description = "Deletes a pathology result by ID")
    public ResponseEntity<Void> deletePathologyResult(
            @Parameter(description = "Pathology Result ID") @PathVariable Long id) {
        pathologyResultService.deletePathologyResult(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get pathology result by ID", description = "Retrieves a pathology result by its ID")
    public ResponseEntity<PathologyResultDTO> getPathologyResultById(
            @Parameter(description = "Pathology Result ID") @PathVariable Long id) {
        return pathologyResultService.getPathologyResultById(id)
                .map(result -> ResponseEntity.ok(pathologyResultService.convertToDTO(result)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/prediction/{predictionId}")
    @Operation(summary = "Get pathology results by prediction ID", description = "Retrieves all pathology results for a specific prediction")
    public ResponseEntity<List<PathologyResultDTO>> getPathologyResultsByPredictionId(
            @Parameter(description = "Prediction ID") @PathVariable Long predictionId) {
        List<PathologyResult> results = pathologyResultService.getPathologyResultsByPredictionId(predictionId);
        return ResponseEntity.ok(pathologyResultService.convertToDTOList(results));
    }
}
