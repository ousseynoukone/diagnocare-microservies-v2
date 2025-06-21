package com.homosapiens.diagnocareservice.model.entity.dtos;

import com.homosapiens.diagnocareservice.model.entity.availability.DaysOfWeek;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalTime;

@Data
public class WeekDayDto {

    @Schema(example = "08:00")
    @JsonFormat(pattern = "HH:mm")
    @NotNull(message = "Start time is required")
    private LocalTime fromTime;

    @Schema(example = "16:00")
    @JsonFormat(pattern = "HH:mm")
    @NotNull(message = "End time is required")
    private LocalTime toTime;

    @Schema(example = "MONDAY")
    @NotNull(message = "Week day is required")
    @Enumerated(EnumType.STRING)
    private DaysOfWeek daysOfWeek;

    @Schema(example = "15")
    @NotNull(message = "Slot duration is required")
    @Min(value = 10, message = "Slot duration must be at least 10 minutes")
    private Integer slotDuration;

    @Schema(hidden = true)
    // Optional field - not required when updating availability
    private Long availabilityId;
}


