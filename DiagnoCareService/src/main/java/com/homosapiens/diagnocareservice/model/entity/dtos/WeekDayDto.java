package com.homosapiens.diagnocareservice.model.entity.dtos;

import com.homosapiens.diagnocareservice.model.entity.availability.DaysOfWeek;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class WeekDayDto {

    @NotNull(message = "Start time is required")
    private LocalTime fromTime;

    @NotNull(message = "End time is required")
    private LocalTime toTime;

    @NotNull(message = "Week day is required")
    @Enumerated(EnumType.STRING)
    private DaysOfWeek daysOfWeek;

    @NotNull(message = "Slot duration is required")
    @Min(value = 10, message = "Slot duration must be at least 10 minutes")
    private Integer slotDuration;

    // Optional field - not required when updating availability
    private Long availabilityId;
}


