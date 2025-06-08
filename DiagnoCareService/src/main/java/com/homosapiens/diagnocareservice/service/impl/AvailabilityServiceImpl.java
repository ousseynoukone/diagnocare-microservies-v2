package com.homosapiens.diagnocareservice.service.impl;

import com.homosapiens.diagnocareservice.model.entity.availability.Availability;
import com.homosapiens.diagnocareservice.model.entity.dtos.AvailabilityDto;
import com.homosapiens.diagnocareservice.model.entity.dtos.AvailabilityResponseDto;
import com.homosapiens.diagnocareservice.model.mapper.AvailabilityMapper;
import com.homosapiens.diagnocareservice.repository.AvailabilityRepository;
import com.homosapiens.diagnocareservice.service.AvailabilityService;
import com.homosapiens.diagnocareservice.service.ScheduleSlotService;
import com.homosapiens.diagnocareservice.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AvailabilityServiceImpl implements AvailabilityService {
    private final AvailabilityRepository availabilityRepository;
    private final ScheduleSlotService scheduleSlotService;
    private final UserService userService;
    private final AvailabilityMapper availabilityMapper;

    @Override
    @Transactional
    public AvailabilityResponseDto createAvailability(AvailabilityDto availabilityDto) {
        Availability availability = availabilityMapper.toAvailability(availabilityDto);
        Availability savedAvailability = availabilityRepository.save(availability);
        scheduleSlotService.createSlots(savedAvailability);
        return AvailabilityResponseDto.fromEntity(savedAvailability);
    }

    @Override
    public Page<AvailabilityResponseDto> getAllAvailability(Pageable pageable) {
        return availabilityRepository.findAll(pageable)
            .map(AvailabilityResponseDto::fromEntity);
    }

    @Override
    public Optional<AvailabilityResponseDto> getAvailabilityById(long id) {
        return availabilityRepository.findById(id)
            .map(AvailabilityResponseDto::fromEntity);
    }

    @Override
    public AvailabilityResponseDto updateAvailability(Availability availability) {
        Availability savedAvailability = availabilityRepository.save(availability);
        return AvailabilityResponseDto.fromEntity(savedAvailability);
    }

    @Override
    public void deleteAvailability(long id) {
        availabilityRepository.deleteById(id);
    }
} 