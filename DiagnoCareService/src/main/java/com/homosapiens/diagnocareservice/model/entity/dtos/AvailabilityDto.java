package com.homosapiens.diagnocareservice.model.entity.dtos;
import com.homosapiens.diagnocareservice.model.entity.availability.WeekDay;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
public class AvailabilityDto {

    @Schema(example = "2025-09-21")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate repeatUntil;

    @Schema(example = "2025-07-21")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate availabilityDate;

    private Set<WeekDayDto> weekDays = new HashSet<>();

    @Schema(example = "152")
    private Long userId;

}

