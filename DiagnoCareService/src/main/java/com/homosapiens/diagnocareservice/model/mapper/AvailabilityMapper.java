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
        Set<WeekDay> weekDays = availabilityDto.getWeekDays().stream()
                .map(weekDayDto -> weekDayDtoMapper
                        .toWeekDay(weekDayDto)).collect(Collectors.toSet());

        weekDayService.saveAllWeekDay(weekDays);

        availability.setRepeating(availabilityDto.isRepeating());
        availability.setWeekDays(weekDays);
        availability.setRepeatUntil(availabilityDto.getRepeatUntil());
        availability.setUser(user);
        availability.setAvailabilityDate(availabilityDto.getAvailabilityDate() != null ? 
            availabilityDto.getAvailabilityDate() : LocalDate.now());

        return availability;
    }

    public AvailabilityDto toDto(Availability availability) {
        AvailabilityDto availabilityDto = new AvailabilityDto();

        availabilityDto.setRepeating(availability.isRepeating());
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
