package com.homosapiens.diagnocareservice.service;

import com.homosapiens.diagnocareservice.model.entity.availability.Availability;
import com.homosapiens.diagnocareservice.model.entity.dtos.AvailabilityDto;
import com.homosapiens.diagnocareservice.model.entity.dtos.AvailabilityResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface AvailabilityService {
    AvailabilityResponseDto createAvailability(AvailabilityDto availabilityDto);
    Page<AvailabilityResponseDto> getAllAvailability(Pageable pageable);
    Optional<AvailabilityResponseDto> getAvailabilityById(long id);
    AvailabilityResponseDto updateAvailability(Availability availability);
    void deleteAvailability(long id);
} 