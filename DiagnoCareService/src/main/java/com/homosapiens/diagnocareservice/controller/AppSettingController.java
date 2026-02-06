package com.homosapiens.diagnocareservice.controller;

import com.homosapiens.diagnocareservice.dto.SettingUpdateRequestDTO;
import com.homosapiens.diagnocareservice.service.AppSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("settings")
@Tag(name = "Settings", description = "Manage application settings")
@RequiredArgsConstructor
public class AppSettingController {

    private final AppSettingService appSettingService;

    @GetMapping("/{key}")
    @Operation(summary = "Get setting value by key")
    public ResponseEntity<String> getValue(@PathVariable String key) {
        return ResponseEntity.ok(appSettingService.getValue(key, ""));
    }

    @PutMapping("/{key}")
    @Operation(summary = "Update setting value by key")
    public ResponseEntity<String> updateValue(@PathVariable String key,
                                              @Valid @RequestBody SettingUpdateRequestDTO requestDTO) {
        return ResponseEntity.ok(appSettingService.setValue(key, requestDTO.getValue()));
    }
}
