package com.homosapiens.diagnocareservice.repository;

import com.homosapiens.diagnocareservice.model.entity.UrgentDisease;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UrgentDiseaseRepository extends JpaRepository<UrgentDisease, Long> {
    boolean existsByDiseaseNameIgnoreCase(String diseaseName);
    Optional<UrgentDisease> findByDiseaseNameIgnoreCase(String diseaseName);
}
