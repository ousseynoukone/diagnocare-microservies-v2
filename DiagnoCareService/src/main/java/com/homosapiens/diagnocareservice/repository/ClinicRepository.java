package com.homosapiens.diagnocareservice.repository;


import com.homosapiens.diagnocareservice.model.entity.Clinic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClinicRepository extends JpaRepository<Clinic,Long> {
    List<Clinic> findByNameContainingIgnoreCase(String name); // Search clinics by name (case-insensitive)
}
