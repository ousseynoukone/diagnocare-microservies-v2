package com.homosapiens.diagnocareservice.controller;

import com.homosapiens.diagnocareservice.dto.SettingUpdateRequestDTO;
import com.homosapiens.diagnocareservice.dto.AppSettingDTO;
import com.homosapiens.diagnocareservice.dto.SettingDescriptorDTO;
import com.homosapiens.diagnocareservice.service.AppSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("settings")
@Tag(name = "Settings", description = "Manage application settings")
@RequiredArgsConstructor
public class AppSettingController {

    private final AppSettingService appSettingService;

    @GetMapping
    @Operation(summary = "List all existing settings")
    public ResponseEntity<List<AppSettingDTO>> getAll() {
        return ResponseEntity.ok(appSettingService.getAllSettings());
    }

    @GetMapping("/known")
    @Operation(summary = "List supported setting keys")
    public ResponseEntity<List<SettingDescriptorDTO>> getKnownKeys() {
        SettingDescriptorDTO checkInBaseUrl = new SettingDescriptorDTO();
        checkInBaseUrl.setKey("CHECKIN_BASE_URL");
        checkInBaseUrl.setDescription("URL de base utilisée dans les liens des emails de rappel de suivi santé.");
        checkInBaseUrl.setDefaultValue("http://localhost:3000/check-in");

        SettingDescriptorDTO firstReminder = new SettingDescriptorDTO();
        firstReminder.setKey("CHECKIN_FIRST_REMINDER_MINUTES");
        firstReminder.setDescription("Délai en minutes entre la prédiction et le premier email de rappel J+1 (défaut : 1440 = 24h).");
        firstReminder.setDefaultValue("1440");

        SettingDescriptorDTO secondReminder = new SettingDescriptorDTO();
        secondReminder.setKey("CHECKIN_SECOND_REMINDER_MINUTES");
        secondReminder.setDescription("Délai en minutes entre le check-in J+1 et l'email de rappel J+2 (défaut : 2880 = 48h).");
        secondReminder.setDefaultValue("2880");

        return ResponseEntity.ok(List.of(checkInBaseUrl, firstReminder, secondReminder));
    }

    @GetMapping("/{key}")
    @Operation(summary = "Get setting value by key")
    public ResponseEntity<AppSettingDTO> getValue(@PathVariable String key) {
        AppSettingDTO dto = new AppSettingDTO();
        dto.setKey(key);
        dto.setValue(appSettingService.getValue(key, ""));
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{key}")
    @Operation(summary = "Update setting value by key")
    public ResponseEntity<AppSettingDTO> updateValue(@PathVariable String key,
                                                     @Valid @RequestBody SettingUpdateRequestDTO requestDTO) {
        String saved = appSettingService.setValue(key, requestDTO.getValue());
        AppSettingDTO dto = new AppSettingDTO();
        dto.setKey(key);
        dto.setValue(saved);
        return ResponseEntity.ok(dto);
    }
}
