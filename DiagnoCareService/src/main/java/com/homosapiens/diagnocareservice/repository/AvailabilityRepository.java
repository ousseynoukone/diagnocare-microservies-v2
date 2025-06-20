package com.homosapiens.diagnocareservice.repository;

import com.homosapiens.diagnocareservice.model.entity.availability.Availability;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
    List<Availability> findByUserId(Long userId);
    Optional<Availability> findByIdAndUserId(Long id, Long userId);
    boolean existsByAvailabilityDateAndUserId(LocalDate availabilityDate, Long userId);

    Optional<Availability> findFirstByUserId_OrderByAvailabilityDateDesc(Long userId);

    Optional<Availability> findFirstByUserIdAndAvailabilityDateGreaterThanEqualOrderByAvailabilityDateAsc(Long userId, LocalDate today);

    Optional<Availability> findFirstByUserIdAndAvailabilityDateGreaterThanOrderByAvailabilityDateAsc(Long userId, LocalDate date);
    void deleteByisGeneratedAndUserId(boolean isGenerated, Long id);

    @Query("SELECT a FROM Availability a LEFT JOIN FETCH a.weekDays WHERE a.id = :id")
    Optional<Availability> findByIdWithWeekDays(@Param("id") Long id);

    @Query("SELECT a FROM Availability a LEFT JOIN FETCH a.weekDays")
    Page<Availability> findAllWithWeekDays(Pageable pageable);

}
