package com.homosapiens.diagnocareservice.model.mapper;

import com.homosapiens.diagnocareservice.core.exception.AppException;
import com.homosapiens.diagnocareservice.model.entity.User;
import com.homosapiens.diagnocareservice.model.entity.availability.Availability;
import com.homosapiens.diagnocareservice.model.entity.availability.WeekDay;
import com.homosapiens.diagnocareservice.model.entity.dtos.AvailabilityDto;
import com.homosapiens.diagnocareservice.model.entity.dtos.WeekDayDto;
import com.homosapiens.diagnocareservice.service.UserService;
import com.homosapiens.diagnocareservice.service.WeekDayService;
import com.homosapiens.diagnocareservice.service.impl.UserServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class AvailabilityMapper {

    WeekDayDtoMapper weekDayDtoMapper;
    UserService userService;
    WeekDayService weekDayService;

    public Availability toAvailability(AvailabilityDto availabilityDto) {
        Optional<User> userOpt = userService.getUserById(availabilityDto.getUserId());
        if (userOpt.isEmpty()) {
            throw new AppException(HttpStatus.NOT_FOUND, "User not found with ID: " + availabilityDto.getUserId());
        }
        User user = userOpt.get();

        Availability availability = new Availability();

        availability.setRepeatUntil(availabilityDto.getRepeatUntil());
        availability.setUser(user);
        availability.setAvailabilityDate(availabilityDto.getAvailabilityDate() != null ?
            availabilityDto.getAvailabilityDate() : LocalDate.now());

        // Create weekdays and set the availability reference
        Set<WeekDay> weekDays = availabilityDto.getWeekDays().stream()
                .map(weekDayDto -> {
                    WeekDay weekDay = weekDayDtoMapper.toWeekDay(weekDayDto);
                    weekDay.setAvailability(availability);
                    return weekDay;
                })
                .collect(Collectors.toSet());

        availability.setWeekDays(weekDays);
        weekDayService.saveAllWeekDay(weekDays);

        return availability;
    }

    public AvailabilityDto toDto(Availability availability) {
        AvailabilityDto availabilityDto = new AvailabilityDto();


        availabilityDto.setRepeatUntil(availability.getRepeatUntil());
        availabilityDto.setUserId(availability.getUser() != null ? availability.getUser().getId() : null);
        availabilityDto.setAvailabilityDate(availability.getAvailabilityDate());
        Set<WeekDayDto> weekDayDtos = availability.getWeekDays().stream()
                .map(weekDay -> weekDayDtoMapper.toDto(weekDay))
                .collect(Collectors.toSet());

        availabilityDto.setWeekDays(weekDayDtos);

        return availabilityDto;
    }

}
