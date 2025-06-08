package com.homosapiens.diagnocareservice.service;

import com.homosapiens.diagnocareservice.model.entity.availability.WeekDay;
import java.util.List;
import java.util.Set;

public interface WeekDayService {
    WeekDay createWeekDay(WeekDay weekDay);
    List<WeekDay> saveAllWeekDay(Set<WeekDay> weekDay);
    WeekDay getWeekDayById(Long id);
    List<WeekDay> getAllWeekDays();
    WeekDay updateWeekDay(Long id, WeekDay weekDay);
    void deleteWeekDay(Long id);
} 