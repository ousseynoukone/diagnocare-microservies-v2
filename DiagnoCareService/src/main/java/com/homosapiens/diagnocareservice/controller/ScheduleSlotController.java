package com.homosapiens.diagnocareservice.controller;

import com.homosapiens.diagnocareservice.model.entity.appointment.ScheduleSlot;
import com.homosapiens.diagnocareservice.model.entity.dtos.ScheduleSlotResponseDto;
import com.homosapiens.diagnocareservice.repository.ScheduleSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/diagnocare/slots")
@RequiredArgsConstructor
public class ScheduleSlotController {

    private final ScheduleSlotRepository scheduleSlotRepository;

    @GetMapping("/availability/{availabilityId}")
    public ResponseEntity<List<ScheduleSlotResponseDto>> getSlotsByAvailabilityId(
            @PathVariable Long availabilityId) {
        List<ScheduleSlot> slots = scheduleSlotRepository.findByAvailabilityId(availabilityId);
        List<ScheduleSlotResponseDto> response = slots.stream()
                .map(ScheduleSlotResponseDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/availability/{availabilityId}/page")
    public ResponseEntity<Page<ScheduleSlotResponseDto>> getSlotsByAvailabilityIdPaginated(
            @PathVariable Long availabilityId,
            Pageable pageable) {
        Page<ScheduleSlot> slots = scheduleSlotRepository.findByAvailabilityId(availabilityId, pageable);
        Page<ScheduleSlotResponseDto> response = slots.map(ScheduleSlotResponseDto::fromEntity);
        return ResponseEntity.ok(response);
    }
} 