package com.homosapiens.diagnocareservice.repository;

import com.homosapiens.diagnocareservice.model.entity.SessionSymptom;
import com.homosapiens.diagnocareservice.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionSymptomRepository extends JpaRepository<SessionSymptom, Long> {
    List<SessionSymptom> findByUser(User user);
    List<SessionSymptom> findByUserId(Long userId);
}
