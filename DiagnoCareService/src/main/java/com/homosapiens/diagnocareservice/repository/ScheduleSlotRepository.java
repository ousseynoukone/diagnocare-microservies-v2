package com.homosapiens.diagnocareservice.repository;

import com.homosapiens.diagnocareservice.model.entity.appointment.ScheduleSlot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleSlotRepository extends JpaRepository<ScheduleSlot, Long> {
    // Find slots by weekDay's availability
    @Query("SELECT ss FROM ScheduleSlot ss JOIN ss.weekDay wd WHERE wd.availability.id = :availabilityId")
    List<ScheduleSlot> findByWeekDayAvailabilityId(@Param("availabilityId") Long availabilityId);
    
    @Query("SELECT ss FROM ScheduleSlot ss JOIN ss.weekDay wd WHERE wd.availability.id = :availabilityId")
    Page<ScheduleSlot> findByWeekDayAvailabilityId(@Param("availabilityId") Long availabilityId, Pageable pageable);

    // Delete slots by weekDay's availability using method name
    void deleteByWeekDay_Availability_Id(Long availabilityId);
    
    void deleteAllByWeekDayId(Long weekDayId);
}
