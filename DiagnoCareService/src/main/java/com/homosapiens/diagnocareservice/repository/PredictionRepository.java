package com.homosapiens.diagnocareservice.repository;

import com.homosapiens.diagnocareservice.model.entity.Prediction;
import com.homosapiens.diagnocareservice.model.entity.SessionSymptom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PredictionRepository extends JpaRepository<Prediction, Long> {
    Optional<Prediction> findByIdAndDeletedFalse(Long id);
    List<Prediction> findBySessionSymptomAndDeletedFalse(SessionSymptom sessionSymptom);
    List<Prediction> findBySessionSymptomIdAndDeletedFalse(Long sessionSymptomId);
    List<Prediction> findByIsRedAlertAndDeletedFalse(Boolean isRedAlert);
    List<Prediction> findByPreviousPredictionIdAndDeletedFalse(Long previousPredictionId);
    List<Prediction> findBySessionSymptomUserIdAndDeletedFalse(Long userId);
    List<Prediction> findByDeletedFalse();

    /** Soft-delete a single prediction by ID — avoids CascadeType.ALL cascade on save() */
    @Modifying
    @Query("UPDATE Prediction p SET p.deleted = true WHERE p.id = :id AND p.deleted = false")
    int softDeleteById(@Param("id") Long id);

    /** Soft-delete all predictions for a user — avoids CascadeType.ALL cascade on save() */
    @Modifying
    @Query("UPDATE Prediction p SET p.deleted = true WHERE p.sessionSymptom.user.id = :userId AND p.deleted = false")
    int softDeleteByUserId(@Param("userId") Long userId);
}
