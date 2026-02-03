package com.homosapiens.diagnocareservice.controller;

import com.homosapiens.diagnocareservice.dto.SessionSymptomDTO;
import com.homosapiens.diagnocareservice.dto.SessionSymptomRequestDTO;
import com.homosapiens.diagnocareservice.model.entity.SessionSymptom;
import com.homosapiens.diagnocareservice.service.SessionSymptomService;
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
@RequestMapping("session-symptoms")
@Tag(name = "Symptom Session Management", description = "APIs for managing symptom sessions")
@RequiredArgsConstructor
public class SessionSymptomController {

    private final SessionSymptomService sessionSymptomService;

    @PostMapping
    @Operation(summary = "Create a new symptom session", description = "Creates a new symptom session for a user")
    public ResponseEntity<SessionSymptomDTO> createSessionSymptom(
            @Valid @RequestBody SessionSymptomRequestDTO requestDTO) {
        SessionSymptom created = sessionSymptomService.createSessionSymptom(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(sessionSymptomService.convertToDTO(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a symptom session", description = "Updates an existing symptom session by ID")
    public ResponseEntity<SessionSymptomDTO> updateSessionSymptom(
            @Parameter(description = "Session Symptom ID") @PathVariable Long id,
            @Valid @RequestBody SessionSymptomRequestDTO requestDTO) {
        try {
            SessionSymptom updated = sessionSymptomService.updateSessionSymptom(id, requestDTO);
            return ResponseEntity.ok(sessionSymptomService.convertToDTO(updated));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a symptom session", description = "Deletes a symptom session by ID")
    public ResponseEntity<Void> deleteSessionSymptom(
            @Parameter(description = "Session Symptom ID") @PathVariable Long id) {
        sessionSymptomService.deleteSessionSymptom(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get symptom session by ID", description = "Retrieves a symptom session by its ID")
    public ResponseEntity<SessionSymptomDTO> getSessionSymptomById(
            @Parameter(description = "Session Symptom ID") @PathVariable Long id) {
        return sessionSymptomService.getSessionSymptomById(id)
                .map(session -> ResponseEntity.ok(sessionSymptomService.convertToDTO(session)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get symptom sessions by user ID", description = "Retrieves all symptom sessions for a specific user")
    public ResponseEntity<List<SessionSymptomDTO>> getSessionSymptomsByUserId(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        List<SessionSymptom> sessions = sessionSymptomService.getSessionSymptomsByUserId(userId);
        return ResponseEntity.ok(sessionSymptomService.convertToDTOList(sessions));
    }
}
