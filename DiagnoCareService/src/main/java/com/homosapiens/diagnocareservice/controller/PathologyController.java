package com.homosapiens.diagnocareservice.controller;

import com.homosapiens.diagnocareservice.dto.PathologyDTO;
import com.homosapiens.diagnocareservice.model.entity.Pathology;
import com.homosapiens.diagnocareservice.service.PathologyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("pathologies")
@Tag(name = "Pathology Management", description = "APIs for managing pathologies")
@RequiredArgsConstructor
public class PathologyController {

    private final PathologyService pathologyService;
    private final com.homosapiens.diagnocareservice.service.MLPredictionClient mlPredictionClient;

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a pathology", description = "Deletes a pathology by ID")
    public ResponseEntity<Void> deletePathology(
            @Parameter(description = "Pathology ID") @PathVariable Long id) {
        pathologyService.deletePathology(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get pathology by ID", description = "Retrieves a pathology by its ID")
    public ResponseEntity<PathologyDTO> getPathologyById(
            @Parameter(description = "Pathology ID") @PathVariable Long id) {
        return pathologyService.getPathologyById(id)
                .map(pathology -> ResponseEntity.ok(pathologyService.convertToDTO(pathology)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get all pathologies", description = "Retrieves all pathologies in the system")
    public ResponseEntity<List<PathologyDTO>> getAllPathologies() {
        List<Pathology> pathologies = pathologyService.getAllPathologies();
        return ResponseEntity.ok(pathologyService.convertToDTOList(pathologies));
    }

    @GetMapping("/search")
    @Operation(summary = "Search pathology by name", description = "Searches for a pathology by name")
    public ResponseEntity<PathologyDTO> searchPathologyByName(
            @Parameter(description = "Pathology name") @RequestParam String name) {
        return pathologyService.getPathologyByName(name)
                .map(pathology -> ResponseEntity.ok(pathologyService.convertToDTO(pathology)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/ml-metadata")
    @Operation(summary = "Get ML diseases metadata", description = "Retrieves all diseases/pathologies from ML service with translations in French and English")
    public ResponseEntity<com.homosapiens.diagnocareservice.dto.MLDiseasesMetadataDTO> getMLDiseasesMetadata() {
        com.homosapiens.diagnocareservice.dto.MLDiseasesMetadataDTO metadata = mlPredictionClient.getDiseasesMetadata();
        return ResponseEntity.ok(metadata);
    }
}
