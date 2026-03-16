package com.homosapiens.diagnocareservice.repository;

import com.homosapiens.diagnocareservice.model.entity.PathologyResult;
import com.homosapiens.diagnocareservice.model.entity.Pathology;
import com.homosapiens.diagnocareservice.model.entity.Doctor;
import com.homosapiens.diagnocareservice.model.entity.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PathologyResultRepository extends JpaRepository<PathologyResult, Long> {
    List<PathologyResult> findByPathology(Pathology pathology);
    List<PathologyResult> findByDoctor(Doctor doctor);
    List<PathologyResult> findByPrediction(Prediction prediction);
    List<PathologyResult> findByPredictionId(Long predictionId);
}
