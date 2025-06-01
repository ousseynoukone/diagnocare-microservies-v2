package com.homosapiens.diagnocareservice.service;

import com.homosapiens.diagnocareservice.core.exception.AppException;
import com.homosapiens.diagnocareservice.model.entity.availability.Availability;
import com.homosapiens.diagnocareservice.model.entity.availability.WeekDay;
import com.homosapiens.diagnocareservice.model.entity.appointment.ScheduleSlot;
import com.homosapiens.diagnocareservice.repository.ScheduleSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ScheduleSlotService {

    private final ScheduleSlotRepository scheduleSlotRepository;

    public void createSlots(Availability availability) {
        try {
            List<ScheduleSlot> slots = generateSlots(availability);
            scheduleSlotRepository.saveAll(slots);
        } catch (Exception e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private List<ScheduleSlot> generateSlots(Availability availability) {
        List<ScheduleSlot> scheduleSlots = new ArrayList<>();
        Set<WeekDay> weekDays = availability.getWeekDays();

        for (WeekDay weekDay : weekDays) {
            LocalTime startTime = weekDay.getFromTime();
            LocalTime endTime = weekDay.getToTime();
            int duration = weekDay.getSlotDuration();

            LocalTime slotStart = startTime;
            while (slotStart.plusMinutes(duration).compareTo(endTime) <= 0) {
                LocalTime slotEnd = slotStart.plusMinutes(duration);

                ScheduleSlot slot = new ScheduleSlot();
                slot.setAvailability(availability);
                slot.setFromTime(slotStart);
                slot.setToTime(slotEnd);
                slot.setIsActive(true);
                slot.setIsBooked(false);
                slot.setAvailable(true);

                scheduleSlots.add(slot);
                slotStart = slotEnd;
            }
        }

        return scheduleSlots;
    }
}
