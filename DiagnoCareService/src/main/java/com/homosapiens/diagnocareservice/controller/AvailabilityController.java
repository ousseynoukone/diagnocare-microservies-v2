package com.homosapiens.diagnocareservice.controller;

import com.homosapiens.diagnocareservice.model.entity.availability.Availability;
import com.homosapiens.diagnocareservice.model.entity.dtos.AvailabilityDto;
import com.homosapiens.diagnocareservice.model.entity.dtos.AvailabilityResponseDto;
import com.homosapiens.diagnocareservice.service.AvailabilityService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@AllArgsConstructor
@RequestMapping("availability")
public class AvailabilityController {
    private AvailabilityService availabilityService;

    @GetMapping
    public ResponseEntity<Page<AvailabilityResponseDto>> getAllAvailability(@PageableDefault(size = 5, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(availabilityService.getAllAvailability(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AvailabilityResponseDto> getAvailabilityById(@PathVariable long id) {
        Optional<AvailabilityResponseDto> availability = availabilityService.getAvailabilityById(id);
        return availability.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AvailabilityResponseDto> createAvailability(@RequestBody AvailabilityDto availability) {
        return ResponseEntity.ok(availabilityService.createAvailability(availability, Optional.empty())) ;
    }

    @PutMapping("/{id}")
    public ResponseEntity<AvailabilityResponseDto> updateAvailability(@RequestBody AvailabilityDto availability, @PathVariable long id) {
        return ResponseEntity.ok(availabilityService.updateAvailability(availability,id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAvailability(@PathVariable long id) {
        availabilityService.deleteAvailability(id);
        return ResponseEntity.noContent().build();
    }
}
