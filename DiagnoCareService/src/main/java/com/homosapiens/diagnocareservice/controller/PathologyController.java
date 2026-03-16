package com.homosapiens.diagnocareservice.controller;

import com.homosapiens.diagnocareservice.dto.PathologyDTO;
import com.homosapiens.diagnocareservice.model.entity.Pathology;
import com.homosapiens.diagnocareservice.service.PathologyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("pathologies")
@Tag(name = "Pathology Management", description = "APIs for managing pathologies")
@RequiredArgsConstructor
public class PathologyController {

    private final PathologyService pathologyService;

    @PostMapping
    @Operation(summary = "Create a new pathology", description = "Creates a new pathology in the system")
    public ResponseEntity<PathologyDTO> createPathology(@RequestBody Pathology pathology) {
        Pathology created = pathologyService.createPathology(pathology);
        return ResponseEntity.status(HttpStatus.CREATED).body(pathologyService.convertToDTO(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a pathology", description = "Updates an existing pathology by ID")
    public ResponseEntity<PathologyDTO> updatePathology(
            @Parameter(description = "Pathology ID") @PathVariable Long id,
            @RequestBody Pathology pathology) {
        try {
            Pathology updated = pathologyService.updatePathology(id, pathology);
            return ResponseEntity.ok(pathologyService.convertToDTO(updated));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

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
}
