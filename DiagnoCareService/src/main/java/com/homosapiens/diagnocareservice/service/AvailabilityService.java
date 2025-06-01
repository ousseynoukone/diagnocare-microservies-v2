package com.homosapiens.diagnocareservice.service;

import com.homosapiens.diagnocareservice.model.entity.availability.Availability;
import com.homosapiens.diagnocareservice.repository.AvailabilityRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AvailabilityService {
    private final AvailabilityRepository availabilityRepository;
    private final ScheduleSlotService scheduleSlotService;


    @Transactional
    public Availability createAvailability(Availability availability) {
        Availability savedAvailability = availabilityRepository.save(availability);
        scheduleSlotService.createSlots(savedAvailability);

        return savedAvailability;
    }

    public List<Availability> getAllAvailability() {
        return availabilityRepository.findAll();
    }

    public Optional<Availability> getAvailabilityById(long id) {
        return availabilityRepository.findById(id);
    }



    public Availability updateAvailability(Availability availability) {
        return availabilityRepository.save(availability);
    }
    public void deleteAvailability(long id) {
        availabilityRepository.deleteById(id);
    }





}
