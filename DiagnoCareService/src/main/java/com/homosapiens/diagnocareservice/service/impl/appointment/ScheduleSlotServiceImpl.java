package com.homosapiens.diagnocareservice.service.impl.appointment;

import com.homosapiens.diagnocareservice.core.exception.AppException;
import com.homosapiens.diagnocareservice.model.entity.availability.Availability;
import com.homosapiens.diagnocareservice.model.entity.availability.WeekDay;
import com.homosapiens.diagnocareservice.model.entity.appointment.ScheduleSlot;
import com.homosapiens.diagnocareservice.model.entity.dtos.ScheduleSlotResponseDto;
import com.homosapiens.diagnocareservice.model.entity.dtos.ScheduleSlotUpdateDto;
import com.homosapiens.diagnocareservice.model.entity.dtos.ScheduleSlotStatusDto;
import com.homosapiens.diagnocareservice.repository.ScheduleSlotRepository;
import com.homosapiens.diagnocareservice.service.ScheduleSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ScheduleSlotServiceImpl implements ScheduleSlotService {
    private final ScheduleSlotRepository scheduleSlotRepository;

    @Override
    public void createSlots(Availability availability) {
        try {
            List<ScheduleSlot> slots = generateSlots(availability);

            System.out.println("Generated slots: " + slots.size());
            scheduleSlotRepository.saveAll(slots);
        } catch (Exception e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    public void bulkDelete(Availability savedAvailability) {
        scheduleSlotRepository.deleteAllByAvailability(savedAvailability);
    }

    @Override
    public void deleteByWeekDayId(Long weekDayId) {
        scheduleSlotRepository.deleteAllByWeekDayId(weekDayId);
    }

    @Override
    @Transactional
    public ScheduleSlotResponseDto updateSlot(Long slotId, ScheduleSlotUpdateDto updateDto) {
        // Validate that at least one field is provided
        if (!updateDto.isValid()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "isActive field must be provided for update");
        }
        
        ScheduleSlot slot = scheduleSlotRepository.findById(slotId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Slot not found with ID: " + slotId));
        
        // Check if slot is booked
        if (slot.isIsBooked()) {
            throw new AppException(HttpStatus.CONFLICT, "Cannot update a booked slot");
        }
        
        // Update isActive field
        if (updateDto.getIsActive() != null) {
            slot.setIsActive(updateDto.getIsActive());
        }
        
        ScheduleSlot savedSlot = scheduleSlotRepository.save(slot);
        return ScheduleSlotResponseDto.fromEntity(savedSlot);
    }

    @Override
    @Transactional
    public void deleteSlot(Long slotId) {
        ScheduleSlot slot = scheduleSlotRepository.findById(slotId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Slot not found with ID: " + slotId));
        
        // Check if slot is booked
        if (slot.isIsBooked()) {
            throw new AppException(HttpStatus.CONFLICT, "Cannot delete a booked slot");
        }
        
        // Instead of physical deletion, deactivate the slot
        slot.setIsActive(false);
        scheduleSlotRepository.save(slot);
    }

    @Override
    @Transactional
    public ScheduleSlotResponseDto updateSlotStatus(Long slotId, ScheduleSlotStatusDto statusDto) {
        // Validate that at least one field is provided
        if (!statusDto.isValid()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "isActive field must be provided for status update");
        }
        
        ScheduleSlot slot = scheduleSlotRepository.findById(slotId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Slot not found with ID: " + slotId));
        
        // Update isActive field
        if (statusDto.getIsActive() != null) {
            slot.setIsActive(statusDto.getIsActive());
        }
        
        ScheduleSlot savedSlot = scheduleSlotRepository.save(slot);
        return ScheduleSlotResponseDto.fromEntity(savedSlot);
    }

    private List<ScheduleSlot> generateSlots(Availability availability) {
        List<ScheduleSlot> scheduleSlots = new ArrayList<>();
        Set<WeekDay> weekDays = availability.getWeekDays();


        for (WeekDay weekDay : weekDays) {
            LocalTime startTime = weekDay.getFromTime();
            LocalTime endTime = weekDay.getToTime();
            int duration = weekDay.getSlotDuration();

            LocalTime slotStart = startTime;
            while (!slotStart.plusMinutes(duration).isAfter(endTime)) {
                LocalTime slotEnd = slotStart.plusMinutes(duration);

                ScheduleSlot slot = new ScheduleSlot();
                slot.setAvailability(availability);
                slot.setFromTime(slotStart);
                slot.setToTime(slotEnd);
                slot.setIsActive(true);
                slot.setIsBooked(false);
                slot.setWeekDay(weekDay);

                scheduleSlots.add(slot);
                slotStart = slotEnd;
            }
        }
        System.out.println("sscheduleSlots: " + scheduleSlots.size());

        return scheduleSlots;
    }
} 