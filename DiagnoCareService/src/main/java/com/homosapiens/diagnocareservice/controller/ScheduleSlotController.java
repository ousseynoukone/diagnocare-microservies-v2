package com.homosapiens.diagnocareservice.controller;

import com.homosapiens.diagnocareservice.model.entity.appointment.ScheduleSlot;
import com.homosapiens.diagnocareservice.model.entity.dtos.ScheduleSlotResponseDto;
import com.homosapiens.diagnocareservice.model.entity.dtos.ScheduleSlotUpdateDto;
import com.homosapiens.diagnocareservice.model.entity.dtos.ScheduleSlotStatusDto;
import com.homosapiens.diagnocareservice.repository.ScheduleSlotRepository;
import com.homosapiens.diagnocareservice.service.ScheduleSlotService;
import jakarta.validation.Valid;
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
    private final ScheduleSlotService scheduleSlotService;

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

    /**
     * Update an existing slot
     * 
     * Constraints:
     * - Cannot update booked slots
     * - Only allows status updates (isActive), not structural changes
     * 
     * @param slotId The ID of the slot to update
     * @param updateDto The update data containing isActive
     * @return Updated slot information
     */
    @PutMapping("/availability/{slotId}")
    public ResponseEntity<ScheduleSlotResponseDto> updateSlot(
            @PathVariable Long slotId,
            @Valid @RequestBody ScheduleSlotUpdateDto updateDto) {
        ScheduleSlotResponseDto updatedSlot = scheduleSlotService.updateSlot(slotId, updateDto);
        return ResponseEntity.ok(updatedSlot);
    }

    /**
     * Remove or block a slot
     * 
     * Constraints:
     * - Cannot delete booked slots
     * - Performs logical deletion by setting isActive=false
     * 
     * @param slotId The ID of the slot to delete/block
     * @return No content on success
     */
    @DeleteMapping("/availability/{slotId}")
    public ResponseEntity<Void> deleteSlot(@PathVariable Long slotId) {
        scheduleSlotService.deleteSlot(slotId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Update slot status (enable/disable/activate)
     * 
     * Constraints:
     * - Allows status changes for non-booked slots
     * 
     * @param slotId The ID of the slot to update status
     * @param statusDto The status update data containing isActive
     * @return Updated slot information
     */
    @PatchMapping("/availability/{slotId}/status")
    public ResponseEntity<ScheduleSlotResponseDto> updateSlotStatus(
            @PathVariable Long slotId,
            @Valid @RequestBody ScheduleSlotStatusDto statusDto) {
        ScheduleSlotResponseDto updatedSlot = scheduleSlotService.updateSlotStatus(slotId, statusDto);
        return ResponseEntity.ok(updatedSlot);
    }
} 