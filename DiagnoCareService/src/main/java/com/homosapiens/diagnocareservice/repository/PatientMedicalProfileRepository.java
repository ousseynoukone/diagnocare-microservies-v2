package com.homosapiens.diagnocareservice.repository;

import com.homosapiens.diagnocareservice.model.entity.PatientMedicalProfile;
import com.homosapiens.diagnocareservice.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientMedicalProfileRepository extends JpaRepository<PatientMedicalProfile, Long> {
    Optional<PatientMedicalProfile> findByUser(User user);
    Optional<PatientMedicalProfile> findByUserId(Long userId);
}
