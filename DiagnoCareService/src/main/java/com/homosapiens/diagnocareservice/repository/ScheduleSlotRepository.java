package com.homosapiens.diagnocareservice.repository;

import com.homosapiens.diagnocareservice.model.entity.appointment.ScheduleSlot;
import com.homosapiens.diagnocareservice.model.entity.availability.Availability;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleSlotRepository extends JpaRepository<ScheduleSlot, Long> {
    List<ScheduleSlot> findByAvailabilityId(Long availabilityId);
    Page<ScheduleSlot> findByAvailabilityId(Long availabilityId, Pageable pageable);

    List<ScheduleSlot> getScheduleSlotByAvailabilityId(long id);

    void deleteAllByAvailability(Availability availability);
    
    void deleteAllByWeekDayId(Long weekDayId);
}
