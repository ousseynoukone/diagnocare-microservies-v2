package com.homosapiens.diagnocareservice.repository;

import com.homosapiens.diagnocareservice.model.entity.Clinic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClinicRepository extends JpaRepository<Clinic, Long> {
    List<Clinic> findByCity(String city);
    List<Clinic> findByNameContainingIgnoreCase(String name);
    Optional<Clinic> findByPhoneNumber(String phoneNumber);
    List<Clinic> findByUserId(Long userId);
    boolean existsByPhoneNumber(String phoneNumber);
}
