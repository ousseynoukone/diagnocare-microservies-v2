package com.homosapiens.diagnocareservice.repository;

import com.homosapiens.diagnocareservice.model.entity.appointment.ScheduleSlot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleSlotRepository extends JpaRepository<ScheduleSlot, Long> {
}
