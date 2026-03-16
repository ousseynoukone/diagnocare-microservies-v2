package com.homosapiens.diagnocareservice.controller;

import com.homosapiens.diagnocareservice.dto.CheckInCreateRequestDTO;
import com.homosapiens.diagnocareservice.dto.CheckInResponseDTO;
import com.homosapiens.diagnocareservice.service.CheckInService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("check-ins")
@Tag(name = "Check-ins", description = "APIs for symptom check-ins")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;

    @PostMapping
    @Operation(summary = "Submit a check-in and create a follow-up prediction")
    public ResponseEntity<CheckInResponseDTO> submitCheckIn(@Valid @RequestBody CheckInCreateRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(checkInService.submitCheckIn(requestDTO));
    }

    @GetMapping
    @Operation(summary = "List check-ins by user")
    public ResponseEntity<List<CheckInResponseDTO>> getCheckIns(@RequestParam Long userId) {
        return ResponseEntity.ok(checkInService.getCheckInsByUser(userId));
    }
}
