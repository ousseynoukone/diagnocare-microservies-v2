package com.homosapiens.diagnocareservice.service;

import com.homosapiens.diagnocareservice.model.entity.dtos.ReviewRequestDto;
import com.homosapiens.diagnocareservice.model.entity.dtos.ReviewResponseDto;
import com.homosapiens.diagnocareservice.model.entity.dtos.DoctorReviewSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReviewService {
    
    // Basic CRUD operations
    ReviewResponseDto createReview(ReviewRequestDto reviewRequestDto);
    ReviewResponseDto getReviewById(Long id);
    ReviewResponseDto updateReview(Long id, ReviewRequestDto reviewRequestDto);
    void deleteReview(Long id);
    
    // Pagination
    Page<ReviewResponseDto> getAllReviews(Pageable pageable);
    Page<ReviewResponseDto> getReviewsByDoctorId(Long doctorId, Pageable pageable);
    Page<ReviewResponseDto> getReviewsByPatientId(Long patientId, Pageable pageable);
    
    // List operations
    List<ReviewResponseDto> getAllReviewsByDoctorId(Long doctorId);
    List<ReviewResponseDto> getAllReviewsByPatientId(Long patientId);
    List<ReviewResponseDto> getReviewsByRatingRange(Integer minRating, Integer maxRating);
    List<ReviewResponseDto> getReviewsWithComments();
    List<ReviewResponseDto> getReviewsWithCommentsByDoctorId(Long doctorId);
    
    // Statistics and summaries
    DoctorReviewSummaryDto getDoctorReviewSummary(Long doctorId);
    Double getAverageRatingByDoctorId(Long doctorId);
    Long getReviewCountByDoctorId(Long doctorId);
    
    // Validation methods
    boolean hasPatientReviewedDoctor(Long patientId, Long doctorId);
    boolean hasPatientReviewedAppointment(Long patientId, Long appointmentId);
    
    // Recent reviews
    List<ReviewResponseDto> getRecentReviewsByDoctorId(Long doctorId, int limit);
} 