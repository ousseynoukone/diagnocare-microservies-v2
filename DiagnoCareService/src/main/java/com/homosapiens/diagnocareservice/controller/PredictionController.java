package com.homosapiens.diagnocareservice.controller;

import com.homosapiens.diagnocareservice.dto.*;
import com.homosapiens.diagnocareservice.model.entity.*;
import com.homosapiens.diagnocareservice.model.entity.enums.GenderEnum;
import com.homosapiens.diagnocareservice.service.*;
import com.homosapiens.diagnocareservice.core.exception.AppException;
import com.homosapiens.diagnocareservice.repository.SymptomRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("predictions")
@Tag(name = "Prediction Management", description = "APIs for managing AI predictions")
@RequiredArgsConstructor
@Slf4j
public class PredictionController {

    private final PredictionService predictionService;
    private final PredictionWorkflowService predictionWorkflowService;
    private final CheckInService checkInService;

    @PostMapping
    @Operation(summary = "Create a new prediction", description = "Creates a new AI prediction based on symptom session")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<PredictionWithResultsResponse> makePrediction(
            @Valid @RequestBody SessionSymptomRequestDTO sessionSymptomRequestDTO) {
        PredictionCreationResult result = predictionWorkflowService.createPrediction(sessionSymptomRequestDTO, null);

        checkInService.scheduleCheckIn(result.getPrediction());

        PredictionDTO predictionDTO = predictionService.convertToDTO(result.getPrediction());
        PredictionWithResultsResponse response = PredictionWithResultsResponse.builder()
                .prediction(predictionDTO)
                .mlResults(result.getMlResponse())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a prediction", description = "Updates an existing prediction by ID")
    public ResponseEntity<PredictionDTO> updatePrediction(
            @Parameter(description = "Prediction ID") @PathVariable Long id,
            @Valid @RequestBody PredictionRequestDTO requestDTO) {
        try {
            Prediction updated = predictionService.updatePrediction(id, requestDTO);
            return ResponseEntity.ok(predictionService.convertToDTO(updated));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a prediction", description = "Deletes a prediction by ID")
    public ResponseEntity<Void> deletePrediction(
            @Parameter(description = "Prediction ID") @PathVariable Long id) {
        predictionService.deletePrediction(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get prediction by ID", description = "Retrieves a prediction by its ID")
    public ResponseEntity<PredictionDTO> getPredictionById(
            @Parameter(description = "Prediction ID") @PathVariable Long id) {
        return predictionService.getPredictionById(id)
                .map(prediction -> ResponseEntity.ok(predictionService.convertToDTO(prediction)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/session-symptom/{sessionSymptomId}")
    @Operation(summary = "Get predictions by session symptom ID", description = "Retrieves all predictions for a specific session symptom")
    public ResponseEntity<List<PredictionDTO>> getPredictionsBySessionSymptomId(
            @Parameter(description = "Session Symptom ID") @PathVariable Long sessionSymptomId) {
        List<Prediction> predictions = predictionService.getPredictionsBySessionSymptomId(sessionSymptomId);
        return ResponseEntity.ok(predictionService.convertToDTOList(predictions));
    }

    @GetMapping
    @Operation(summary = "Get all predictions", description = "Retrieves all predictions in the system")
    public ResponseEntity<List<PredictionDTO>> getAllPredictions() {
        List<Prediction> predictions = predictionService.getAllPredictions();
        return ResponseEntity.ok(predictionService.convertToDTOList(predictions));
    }

    @GetMapping("/red-alerts")
    @Operation(summary = "Get red alert predictions", description = "Retrieves all predictions marked as red alerts")
    public ResponseEntity<List<PredictionDTO>> getRedAlertPredictions() {
        List<Prediction> predictions = predictionService.getRedAlertPredictions();
        return ResponseEntity.ok(predictionService.convertToDTOList(predictions));
    }
}
