package com.homosapiens.diagnocareservice.repository;

import com.homosapiens.diagnocareservice.model.entity.availability.WeekDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeekDayRepository extends JpaRepository<WeekDay, Long> {
} 