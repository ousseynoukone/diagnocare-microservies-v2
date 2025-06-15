package com.homosapiens.diagnocareservice.model.mapper;

import com.homosapiens.diagnocareservice.core.exception.AppException;
import com.homosapiens.diagnocareservice.model.entity.availability.Availability;
import com.homosapiens.diagnocareservice.model.entity.availability.WeekDay;
import com.homosapiens.diagnocareservice.model.entity.dtos.WeekDayDto;
import com.homosapiens.diagnocareservice.repository.AvailabilityRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class WeekDayDtoMapper {

    private final AvailabilityRepository availabilityRepository;

    public WeekDay toWeekDay(WeekDayDto wd) {
        WeekDay dto = new WeekDay();
        dto.setDaysOfWeek(wd.getDaysOfWeek());
        dto.setFromTime(wd.getFromTime());
        dto.setToTime(wd.getToTime());
        dto.setSlotDuration(wd.getSlotDuration());
        
        if (wd.getAvailabilityId() != null) {
            Optional<Availability> availability = availabilityRepository.findById(wd.getAvailabilityId());
            if (availability.isPresent()) {
                dto.setAvailability(availability.get());
            } else {
                throw new AppException(HttpStatus.NOT_FOUND, "Availability not found with ID: " + wd.getAvailabilityId());
            }
        }
        
        return dto;
    }

    public WeekDayDto toDto(WeekDay weekDay) {
        WeekDayDto dto = new WeekDayDto();
        dto.setDaysOfWeek(weekDay.getDaysOfWeek());
        dto.setFromTime(weekDay.getFromTime());
        dto.setToTime(weekDay.getToTime());
        dto.setSlotDuration(weekDay.getSlotDuration());
        if (weekDay.getAvailability() != null) {
            dto.setAvailabilityId(weekDay.getAvailability().getId());
        }
        return dto;
    }
}
