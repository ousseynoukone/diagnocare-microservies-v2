package com.homosapiens.diagnocareservice.model.entity.dtos;

import com.homosapiens.diagnocareservice.model.entity.availability.WeekDay;
import lombok.Data;

import java.time.LocalTime;

@Data
public class WeekDayResponseDto {
    private Long id;
    private String daysOfWeek;
    private LocalTime fromTime;
    private LocalTime toTime;
    private Integer slotDuration;

    public static WeekDayResponseDto fromEntity(WeekDay weekDay) {
        WeekDayResponseDto dto = new WeekDayResponseDto();
        dto.setId(weekDay.getId());
        dto.setDaysOfWeek(weekDay.getDaysOfWeek().name());
        dto.setFromTime(weekDay.getFromTime());
        dto.setToTime(weekDay.getToTime());
        dto.setSlotDuration(weekDay.getSlotDuration());
        return dto;
    }
} 