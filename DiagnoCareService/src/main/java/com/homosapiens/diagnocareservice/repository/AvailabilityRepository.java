package com.homosapiens.diagnocareservice.repository;

import com.homosapiens.diagnocareservice.model.entity.availability.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
    List<Availability> findByUserId(Long userId);
    Optional<Availability> findByIdAndUserId(Long id, Long userId);
    boolean existsByAvailabilityDateAndUserId(LocalDate availabilityDate, Long userId);
}
