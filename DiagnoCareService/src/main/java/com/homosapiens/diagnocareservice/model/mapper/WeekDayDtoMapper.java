package com.homosapiens.diagnocareservice.model.mapper;

import com.homosapiens.diagnocareservice.core.exception.AppException;
import com.homosapiens.diagnocareservice.model.entity.availability.Availability;
import com.homosapiens.diagnocareservice.model.entity.availability.WeekDay;
import com.homosapiens.diagnocareservice.model.entity.dtos.WeekDayDto;
import com.homosapiens.diagnocareservice.service.AvailabilityService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Component
@AllArgsConstructor
public class WeekDayDtoMapper {

    public WeekDay toWeekDay(WeekDayDto wd) {
        WeekDay dto = new WeekDay();
        dto.setDaysOfWeek(wd.getDaysOfWeek());
        dto.setFromTime(wd.getFromTime());
        dto.setToTime(wd.getToTime());
        dto.setSlotDuration(wd.getSlotDuration());
        return dto;
    }
}
