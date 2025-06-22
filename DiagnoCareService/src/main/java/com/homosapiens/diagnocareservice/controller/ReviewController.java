package com.homosapiens.diagnocareservice.controller;

import com.homosapiens.diagnocareservice.model.entity.dtos.ReviewRequestDto;
import com.homosapiens.diagnocareservice.model.entity.dtos.ReviewResponseDto;
import com.homosapiens.diagnocareservice.model.entity.dtos.DoctorReviewSummaryDto;
import com.homosapiens.diagnocareservice.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews")
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(
        summary = "Create a new review",
        description = "Creates a new review for a doctor by a patient."
    )
    @PostMapping
    public ResponseEntity<ReviewResponseDto> createReview(@RequestBody ReviewRequestDto reviewRequestDto) {
        log.info("Creating review: rating={}, doctorId={}, patientId={}, appointmentId={}", 
                reviewRequestDto.getRating(), 
                reviewRequestDto.getDoctorId(), 
                reviewRequestDto.getPatientId(), 
                reviewRequestDto.getAppointmentId());
        
        return new ResponseEntity<>(reviewService.createReview(reviewRequestDto), HttpStatus.CREATED);
    }

    @Operation(summary = "Get a review by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponseDto> getReviewById(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getReviewById(id));
    }

    @Operation(summary = "Update a review")
    @PutMapping("/{id}")
    public ResponseEntity<ReviewResponseDto> updateReview(
            @PathVariable Long id, 
            @RequestBody ReviewRequestDto reviewRequestDto) {
        return ResponseEntity.ok(reviewService.updateReview(id, reviewRequestDto));
    }

    @Operation(summary = "Delete a review")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all reviews with pagination")
    @GetMapping
    public ResponseEntity<Page<ReviewResponseDto>> getAllReviews(
            @PageableDefault(size = 10, sort = "reviewDate") Pageable pageable) {
        return ResponseEntity.ok(reviewService.getAllReviews(pageable));
    }

    @Operation(summary = "Get all reviews for a specific doctor")
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<ReviewResponseDto>> getReviewsByDoctorId(@PathVariable Long doctorId) {
        return ResponseEntity.ok(reviewService.getAllReviewsByDoctorId(doctorId));
    }

    @Operation(summary = "Get reviews for a specific doctor with pagination")
    @GetMapping("/doctor/{doctorId}/page")
    public ResponseEntity<Page<ReviewResponseDto>> getReviewsByDoctorIdPaginated(
            @PathVariable Long doctorId,
            @PageableDefault(size = 10, sort = "reviewDate") Pageable pageable) {
        return ResponseEntity.ok(reviewService.getReviewsByDoctorId(doctorId, pageable));
    }

    @Operation(summary = "Get all reviews by a specific patient")
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<ReviewResponseDto>> getReviewsByPatientId(@PathVariable Long patientId) {
        return ResponseEntity.ok(reviewService.getAllReviewsByPatientId(patientId));
    }

    @Operation(summary = "Get reviews by a specific patient with pagination")
    @GetMapping("/patient/{patientId}/page")
    public ResponseEntity<Page<ReviewResponseDto>> getReviewsByPatientIdPaginated(
            @PathVariable Long patientId,
            @PageableDefault(size = 10, sort = "reviewDate") Pageable pageable) {
        return ResponseEntity.ok(reviewService.getReviewsByPatientId(patientId, pageable));
    }

    @Operation(summary = "Get reviews by rating range")
    @GetMapping("/rating-range")
    public ResponseEntity<List<ReviewResponseDto>> getReviewsByRatingRange(
            @RequestParam Integer minRating,
            @RequestParam Integer maxRating) {
        return ResponseEntity.ok(reviewService.getReviewsByRatingRange(minRating, maxRating));
    }

    @Operation(summary = "Get all reviews with comments")
    @GetMapping("/with-comments")
    public ResponseEntity<List<ReviewResponseDto>> getReviewsWithComments() {
        return ResponseEntity.ok(reviewService.getReviewsWithComments());
    }

    @Operation(summary = "Get reviews with comments for a specific doctor")
    @GetMapping("/doctor/{doctorId}/with-comments")
    public ResponseEntity<List<ReviewResponseDto>> getReviewsWithCommentsByDoctorId(@PathVariable Long doctorId) {
        return ResponseEntity.ok(reviewService.getReviewsWithCommentsByDoctorId(doctorId));
    }

    @Operation(summary = "Get review summary for a doctor")
    @GetMapping("/doctor/{doctorId}/summary")
    public ResponseEntity<DoctorReviewSummaryDto> getDoctorReviewSummary(@PathVariable Long doctorId) {
        return ResponseEntity.ok(reviewService.getDoctorReviewSummary(doctorId));
    }

    @Operation(summary = "Get average rating for a doctor")
    @GetMapping("/doctor/{doctorId}/average-rating")
    public ResponseEntity<Double> getAverageRatingByDoctorId(@PathVariable Long doctorId) {
        return ResponseEntity.ok(reviewService.getAverageRatingByDoctorId(doctorId));
    }

    @Operation(summary = "Get review count for a doctor")
    @GetMapping("/doctor/{doctorId}/count")
    public ResponseEntity<Long> getReviewCountByDoctorId(@PathVariable Long doctorId) {
        return ResponseEntity.ok(reviewService.getReviewCountByDoctorId(doctorId));
    }

    @Operation(summary = "Check if a patient has reviewed a doctor")
    @GetMapping("/check/patient/{patientId}/doctor/{doctorId}")
    public ResponseEntity<Boolean> hasPatientReviewedDoctor(
            @PathVariable Long patientId,
            @PathVariable Long doctorId) {
        return ResponseEntity.ok(reviewService.hasPatientReviewedDoctor(patientId, doctorId));
    }

    @Operation(summary = "Check if a patient has reviewed an appointment")
    @GetMapping("/check/patient/{patientId}/appointment/{appointmentId}")
    public ResponseEntity<Boolean> hasPatientReviewedAppointment(
            @PathVariable Long patientId,
            @PathVariable Long appointmentId) {
        return ResponseEntity.ok(reviewService.hasPatientReviewedAppointment(patientId, appointmentId));
    }

    @Operation(summary = "Get recent reviews for a doctor")
    @GetMapping("/doctor/{doctorId}/recent")
    public ResponseEntity<List<ReviewResponseDto>> getRecentReviewsByDoctorId(
            @PathVariable Long doctorId,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(reviewService.getRecentReviewsByDoctorId(doctorId, limit));
    }

    // Test endpoint for debugging JSON deserialization
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testJsonDeserialization(@RequestBody Map<String, Object> request) {
        log.info("Test endpoint received: {}", request);
        Map<String, Object> response = new HashMap<>();
        response.put("received", request);
        response.put("rating", request.get("rating"));
        response.put("doctorId", request.get("doctorId"));
        response.put("patientId", request.get("patientId"));
        return ResponseEntity.ok(response);
    }
} 