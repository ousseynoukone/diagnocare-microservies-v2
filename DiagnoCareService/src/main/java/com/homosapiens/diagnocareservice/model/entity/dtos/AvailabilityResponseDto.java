package com.homosapiens.diagnocareservice.model.entity.dtos;

import com.homosapiens.diagnocareservice.model.entity.availability.Availability;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
public class AvailabilityResponseDto {
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isRepeating;
    private LocalDate repeatUntil;
    private LocalDate availabilityDate;
    private Long userId;
    private Set<WeekDayResponseDto> weekDays = new HashSet<>();

    @Data
    public static class WeekDayResponseDto {
        private Long id;
        private String daysOfWeek;
        private String fromTime;
        private String toTime;
        private Integer slotDuration;
    }

    public static AvailabilityResponseDto fromEntity(Availability availability) {
        AvailabilityResponseDto dto = new AvailabilityResponseDto();
        dto.setId(availability.getId());
        dto.setCreatedAt(availability.getCreatedAt());
        dto.setUpdatedAt(availability.getUpdatedAt());
        dto.setRepeating(availability.isRepeating());
        dto.setRepeatUntil(availability.getRepeatUntil());
        dto.setAvailabilityDate(availability.getAvailabilityDate());
        dto.setUserId(availability.getUser().getId());

        if (availability.getWeekDays() != null) {
            availability.getWeekDays().forEach(weekDay -> {
                WeekDayResponseDto weekDayDto = new WeekDayResponseDto();
                weekDayDto.setId(weekDay.getId());
                weekDayDto.setDaysOfWeek(weekDay.getDaysOfWeek().name());
                weekDayDto.setFromTime(weekDay.getFromTime().toString());
                weekDayDto.setToTime(weekDay.getToTime().toString());
                weekDayDto.setSlotDuration(weekDay.getSlotDuration());
                dto.getWeekDays().add(weekDayDto);
            });
        }

        return dto;
    }
} 