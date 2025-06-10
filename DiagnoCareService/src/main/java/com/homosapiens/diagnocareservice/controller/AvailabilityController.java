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

@RestController
@AllArgsConstructor
@RequestMapping("availability")
public class AvailabilityController {
    private AvailabilityService availabilityService;

    @GetMapping
    public ResponseEntity<Page<AvailabilityResponseDto>> getAllAvailability(@PageableDefault(size = 5, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(availabilityService.getAllAvailability(pageable));
    }

    @PostMapping
    public ResponseEntity<AvailabilityResponseDto> createAvailability(@RequestBody AvailabilityDto availability) {
        return ResponseEntity.ok(availabilityService.createAvailability(availability)) ;
    }
}
