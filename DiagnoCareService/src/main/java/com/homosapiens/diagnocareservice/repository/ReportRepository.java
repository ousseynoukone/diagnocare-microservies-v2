package com.homosapiens.diagnocareservice.repository;

import com.homosapiens.diagnocareservice.model.entity.Report;
import com.homosapiens.diagnocareservice.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByUser(User user);
    List<Report> findByUserId(Long userId);
    List<Report> findByIsCorrected(Boolean isCorrected);
    List<Report> findByPredictionId(Long predictionId);
}
