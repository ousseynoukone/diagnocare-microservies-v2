package com.homosapiens.diagnocareservice.model.entity.dtos;
import com.homosapiens.diagnocareservice.model.entity.availability.WeekDay;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Data
public class AvailabilityDto {

    private boolean isRepeating;

    private LocalDate repeatUntil;

    private LocalDate availabilityDate;

    private Set<WeekDayDto> weekDays = new HashSet<>();

    private Long userId;
}

