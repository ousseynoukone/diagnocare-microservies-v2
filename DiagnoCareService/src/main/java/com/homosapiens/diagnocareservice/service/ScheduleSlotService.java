package com.homosapiens.diagnocareservice.service;

import com.homosapiens.diagnocareservice.model.entity.availability.Availability;

public interface ScheduleSlotService {
    void createSlots(Availability availability);
} 