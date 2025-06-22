package com.homosapiens.diagnocareservice.repository;

import com.homosapiens.diagnocareservice.model.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    // Find all reviews for a specific doctor
    List<Review> findByDoctorId(Long doctorId);
    
    // Find all reviews by a specific patient
    List<Review> findByPatientId(Long patientId);
    
    // Find reviews for a specific appointment
    List<Review> findByAppointmentId(Long appointmentId);
    
    // Find reviews by rating range
    List<Review> findByRatingBetween(Integer minRating, Integer maxRating);
    
    // Find reviews by doctor with pagination
    Page<Review> findByDoctorId(Long doctorId, Pageable pageable);
    
    // Find reviews by patient with pagination
    Page<Review> findByPatientId(Long patientId, Pageable pageable);
    
    // Check if a patient has already reviewed a specific doctor
    Optional<Review> findByPatientIdAndDoctorId(Long patientId, Long doctorId);
    
    // Check if a patient has already reviewed a specific appointment
    Optional<Review> findByPatientIdAndAppointmentId(Long patientId, Long appointmentId);
    
    // Calculate average rating for a doctor
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.doctor.id = :doctorId")
    Double getAverageRatingByDoctorId(@Param("doctorId") Long doctorId);
    
    // Count reviews for a doctor
    @Query("SELECT COUNT(r) FROM Review r WHERE r.doctor.id = :doctorId")
    Long countReviewsByDoctorId(@Param("doctorId") Long doctorId);
    
    // Find reviews with comments (not null or empty)
    @Query("SELECT r FROM Review r WHERE r.comment IS NOT NULL AND r.comment != ''")
    List<Review> findReviewsWithComments();
    
    // Find reviews for a doctor with comments
    @Query("SELECT r FROM Review r WHERE r.doctor.id = :doctorId AND r.comment IS NOT NULL AND r.comment != ''")
    List<Review> findReviewsWithCommentsByDoctorId(@Param("doctorId") Long doctorId);
} 