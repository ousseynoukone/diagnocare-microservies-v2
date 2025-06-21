package com.homosapiens.diagnocareservice.service.impl.appointment;

import com.homosapiens.diagnocareservice.core.exception.AppException;
import com.homosapiens.diagnocareservice.model.entity.availability.WeekDay;
import com.homosapiens.diagnocareservice.repository.WeekDayRepository;
import com.homosapiens.diagnocareservice.service.ScheduleSlotService;
import com.homosapiens.diagnocareservice.service.WeekDayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class WeekDayServiceImpl implements WeekDayService {

    private final WeekDayRepository weekDayRepository;
    private final ScheduleSlotService scheduleSlotService;

    @Override
    public WeekDay createWeekDay(WeekDay weekDay) {
        return weekDayRepository.save(weekDay);
    }

    @Override
    public List<WeekDay> saveAllWeekDay(Set<WeekDay> weekDays) {
        return weekDayRepository.saveAll(weekDays);
    }

    @Override
    @Transactional(readOnly = true)
    public WeekDay getWeekDayById(Long id) {
        return weekDayRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "WeekDay not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<WeekDay> getAllWeekDays() {
        return weekDayRepository.findAll();
    }

    @Override
    public WeekDay updateWeekDay(Long id, WeekDay weekDay) {
        WeekDay existingWeekDay = getWeekDayById(id);
        
        existingWeekDay.setFromTime(weekDay.getFromTime());
        existingWeekDay.setToTime(weekDay.getToTime());
        existingWeekDay.setDaysOfWeek(weekDay.getDaysOfWeek());
        existingWeekDay.setSlotDuration(weekDay.getSlotDuration());

        return weekDayRepository.save(existingWeekDay);
    }

    @Override
    public void deleteWeekDay(Long id) {
        if (!weekDayRepository.existsById(id)) {
            throw new AppException(HttpStatus.NOT_FOUND, "WeekDay not found with id: " + id);
        }
        // First, delete all schedule slots that reference this weekday
        scheduleSlotService.deleteByWeekDayId(id);
        // Then delete the weekday
        weekDayRepository.deleteById(id);
    }

    @Override
    public void deleteAllWeekDays(Set<WeekDay> weekDays) {
        if (weekDays != null && !weekDays.isEmpty()) {
            // First, delete all schedule slots that reference these weekdays
            for (WeekDay weekDay : weekDays) {
                scheduleSlotService.deleteByWeekDayId(weekDay.getId());
            }
            // Then delete the weekdays
            weekDayRepository.deleteAll(weekDays);
        }
    }
} 