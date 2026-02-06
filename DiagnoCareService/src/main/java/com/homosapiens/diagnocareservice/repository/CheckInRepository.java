package com.homosapiens.diagnocareservice.repository;

import com.homosapiens.diagnocareservice.model.entity.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CheckInRepository extends JpaRepository<CheckIn, Long> {
    List<CheckIn> findByUserId(Long userId);

    Optional<CheckIn> findByPreviousPredictionIdAndUserId(Long previousPredictionId, Long userId);

    @Query("""
            select c from CheckIn c
            where c.status <> 'COMPLETED'
              and (
                   (c.firstSentAt is null and c.firstReminderAt is not null and c.firstReminderAt <= :now)
                or (c.secondSentAt is null and c.secondReminderAt is not null and c.secondReminderAt <= :now)
              )
            """)
    List<CheckIn> findDueReminders(@Param("now") LocalDateTime now);
}
