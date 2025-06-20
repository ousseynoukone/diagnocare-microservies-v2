package com.homosapiens.diagnocareservice.service;

import com.homosapiens.diagnocareservice.model.entity.availability.Availability;
import com.homosapiens.diagnocareservice.model.entity.appointment.ScheduleSlot;
import com.homosapiens.diagnocareservice.model.entity.dtos.ScheduleSlotResponseDto;
import com.homosapiens.diagnocareservice.model.entity.dtos.ScheduleSlotUpdateDto;
import com.homosapiens.diagnocareservice.model.entity.dtos.ScheduleSlotStatusDto;

public interface ScheduleSlotService {
    void createSlots(Availability availability);

    void bulkDelete(Availability savedAvailability);

    void deleteByWeekDayId(Long weekDayId);
    
    // New methods for individual slot operations
    ScheduleSlotResponseDto updateSlot(Long slotId, ScheduleSlotUpdateDto updateDto);
    
    void deleteSlot(Long slotId);
    
    ScheduleSlotResponseDto updateSlotStatus(Long slotId, ScheduleSlotStatusDto statusDto);
}