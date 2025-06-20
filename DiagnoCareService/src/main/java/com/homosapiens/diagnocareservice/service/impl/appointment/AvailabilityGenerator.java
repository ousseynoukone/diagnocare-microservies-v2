package com.homosapiens.diagnocareservice.service.impl.appointment;

import com.homosapiens.diagnocareservice.model.entity.availability.Availability;
import com.homosapiens.diagnocareservice.model.entity.dtos.AvailabilityResponseDto;
import com.homosapiens.diagnocareservice.model.mapper.AvailabilityMapper;
import com.homosapiens.diagnocareservice.repository.AvailabilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AvailabilityGenerator {

    private final AvailabilityServiceImpl availabilityService;
    private final AvailabilityMapper availabilityMapper;
    private final AvailabilityRepository availabilityRepository;

    public List<AvailabilityResponseDto> generateAvailability(Availability availability) {
        if (availability.getRepeatUntil() == null || !availability.isRepeating()) {
            return List.of(AvailabilityResponseDto.fromEntity(availability));
        }

        LocalDate endDate = availability.getRepeatUntil();
        if (endDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Repeat until date must be in the future");
        }

        // Set initial availability date if not set
        if (availability.getAvailabilityDate() == null) {
            availability.setAvailabilityDate(LocalDate.now());
        }

        Availability currentAvailability = availability;
        List<AvailabilityResponseDto> availabilities = new ArrayList<>();
        availabilities.add(AvailabilityResponseDto.fromEntity(availability));

        while (currentAvailability.getAvailabilityDate().plusWeeks(1).isBefore(endDate)) {
            LocalDate nextDate = currentAvailability.getAvailabilityDate().plusWeeks(1);

            // Check if availability already exists for this date and user
            boolean exists = availabilityRepository.existsByAvailabilityDateAndUserId(nextDate, availability.getUser().getId());
            if (!exists) {
                Availability newAvailability = getAvailability(currentAvailability);
                newAvailability.setGenerated(true);
                AvailabilityResponseDto createdAvailability = availabilityService
                        .createAvailability(availabilityMapper.toDto(newAvailability), Optional.of(true));
                availabilities.add(createdAvailability);
                currentAvailability = newAvailability;
            } else {
                // Skip to next week if availability already exists
                currentAvailability.setAvailabilityDate(nextDate);
            }
        }

        return availabilities;
    }

    private Availability getAvailability(Availability previousAvailability) {
        Availability newAvailability = new Availability();
        newAvailability.setAvailabilityDate(previousAvailability.getAvailabilityDate().plusWeeks(1));
        newAvailability.setUser(previousAvailability.getUser());
        newAvailability.setRepeatUntil(previousAvailability.getRepeatUntil());
        newAvailability.setRepeating(previousAvailability.isRepeating());
        newAvailability.setWeekDays(previousAvailability.getWeekDays());
        return newAvailability;
    }
}
