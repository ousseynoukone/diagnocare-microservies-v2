package com.homosapiens.diagnocareservice.repository;

import com.homosapiens.diagnocareservice.model.entity.Prediction;
import com.homosapiens.diagnocareservice.model.entity.SessionSymptom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PredictionRepository extends JpaRepository<Prediction, Long> {
    List<Prediction> findBySessionSymptom(SessionSymptom sessionSymptom);
    List<Prediction> findBySessionSymptomId(Long sessionSymptomId);
    List<Prediction> findByIsRedAlert(Boolean isRedAlert);
    List<Prediction> findByPreviousPredictionId(Long previousPredictionId);
}
