package com.homosapiens.diagnocareservice.model.entity.dtos;

import com.homosapiens.diagnocareservice.model.entity.appointment.ScheduleSlot;
import lombok.Data;

import java.time.LocalTime;

@Data
public class ScheduleSlotResponseDto {
    private Long id;
    private LocalTime fromTime;
    private LocalTime toTime;
    private Boolean isActive;
    private boolean IsBooked;
    private Long availabilityId;
    private WeekDayResponseDto weekDay;

    public static ScheduleSlotResponseDto fromEntity(ScheduleSlot slot) {
        ScheduleSlotResponseDto dto = new ScheduleSlotResponseDto();
        dto.setId(slot.getId());
        dto.setFromTime(slot.getFromTime());
        dto.setToTime(slot.getToTime());
        dto.setIsActive(slot.getIsActive());
        dto.setIsBooked(slot.isIsBooked());
        dto.setWeekDay(WeekDayResponseDto.fromEntity(slot.getWeekDay()));
        dto.setAvailabilityId(slot.getAvailability() != null ? slot.getAvailability().getId() : null);
        return dto;
    }
} 