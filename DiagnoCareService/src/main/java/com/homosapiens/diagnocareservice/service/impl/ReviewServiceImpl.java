package com.homosapiens.diagnocareservice.service.impl;

import com.homosapiens.diagnocareservice.core.exception.AppException;
import com.homosapiens.diagnocareservice.model.entity.Review;
import com.homosapiens.diagnocareservice.model.entity.User;
import com.homosapiens.diagnocareservice.model.entity.appointment.Appointment;
import com.homosapiens.diagnocareservice.model.entity.dtos.ReviewRequestDto;
import com.homosapiens.diagnocareservice.model.entity.dtos.ReviewResponseDto;
import com.homosapiens.diagnocareservice.model.entity.dtos.DoctorReviewSummaryDto;
import com.homosapiens.diagnocareservice.model.entity.enums.RoleEnum;
import com.homosapiens.diagnocareservice.model.mapper.ReviewMapper;
import com.homosapiens.diagnocareservice.repository.ReviewRepository;
import com.homosapiens.diagnocareservice.repository.AppointmentRepository;
import com.homosapiens.diagnocareservice.service.AppointmentService;
import com.homosapiens.diagnocareservice.service.ReviewService;
import com.homosapiens.diagnocareservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final UserService userService;
    private final AppointmentService appointmentService;
    private final AppointmentRepository appointmentRepository;

    @Override
    public ReviewResponseDto createReview(ReviewRequestDto reviewRequestDto) {
        // Validate that patient hasn't already reviewed this doctor
        if (hasPatientReviewedDoctor(reviewRequestDto.getPatientId(), reviewRequestDto.getDoctorId())) {
            throw new AppException(HttpStatus.CONFLICT, "Patient has already reviewed this doctor");
        }

        // Validate that patient hasn't already reviewed this appointment (if appointmentId is provided)
        if (reviewRequestDto.getAppointmentId() != null && 
            hasPatientReviewedAppointment(reviewRequestDto.getPatientId(), reviewRequestDto.getAppointmentId())) {
            throw new AppException(HttpStatus.CONFLICT, "Patient has already reviewed this appointment");
        }

        // Validate users exist and have correct roles
        User doctor = validateAndGetDoctor(reviewRequestDto.getDoctorId());
        User patient = validateAndGetPatient(reviewRequestDto.getPatientId());

        // Validate appointment exists and belongs to the correct patient/doctor if appointmentId is provided
        Appointment appointment = null;
        if (reviewRequestDto.getAppointmentId() != null) {
            appointment = validateAndGetAppointment(reviewRequestDto.getAppointmentId());
            
            // Validate that the appointment belongs to the correct patient and doctor
            if (!appointment.getPatient().getId().equals(reviewRequestDto.getPatientId())) {
                throw new AppException(HttpStatus.BAD_REQUEST, 
                    "Appointment does not belong to the specified patient");
            }
            
            if (!appointment.getDoctor().getId().equals(reviewRequestDto.getDoctorId())) {
                throw new AppException(HttpStatus.BAD_REQUEST, 
                    "Appointment does not belong to the specified doctor");
            }
        }

        Review review = new Review();
        review.setRating(reviewRequestDto.getRating());
        review.setComment(reviewRequestDto.getComment());
        review.setDoctor(doctor);
        review.setPatient(patient);
        review.setAppointment(appointment);
        review.setReviewDate(java.time.LocalDateTime.now());

        Review savedReview = reviewRepository.save(review);
        return reviewMapper.toDto(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponseDto getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Review not found with id: " + id));
        return reviewMapper.toDto(review);
    }

    @Override
    public ReviewResponseDto updateReview(Long id, ReviewRequestDto reviewRequestDto) {
        Review existingReview = reviewRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Review not found with id: " + id));

        // Validate that the patient is updating their own review
        if (!existingReview.getPatient().getId().equals(reviewRequestDto.getPatientId())) {
            throw new AppException(HttpStatus.FORBIDDEN, "You can only update your own reviews");
        }

        existingReview.setRating(reviewRequestDto.getRating());
        existingReview.setComment(reviewRequestDto.getComment());

        Review updatedReview = reviewRepository.save(existingReview);
        return reviewMapper.toDto(updatedReview);
    }

    @Override
    public void deleteReview(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new AppException(HttpStatus.NOT_FOUND, "Review not found with id: " + id);
        }
        reviewRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponseDto> getAllReviews(Pageable pageable) {
        return reviewRepository.findAll(pageable)
                .map(reviewMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponseDto> getReviewsByDoctorId(Long doctorId, Pageable pageable) {
        return reviewRepository.findByDoctorId(doctorId, pageable)
                .map(reviewMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponseDto> getReviewsByPatientId(Long patientId, Pageable pageable) {
        return reviewRepository.findByPatientId(patientId, pageable)
                .map(reviewMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getAllReviewsByDoctorId(Long doctorId) {
        return reviewRepository.findByDoctorId(doctorId).stream()
                .map(reviewMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getAllReviewsByPatientId(Long patientId) {
        return reviewRepository.findByPatientId(patientId).stream()
                .map(reviewMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getReviewsByRatingRange(Integer minRating, Integer maxRating) {
        return reviewRepository.findByRatingBetween(minRating, maxRating).stream()
                .map(reviewMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getReviewsWithComments() {
        return reviewRepository.findReviewsWithComments().stream()
                .map(reviewMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getReviewsWithCommentsByDoctorId(Long doctorId) {
        return reviewRepository.findReviewsWithCommentsByDoctorId(doctorId).stream()
                .map(reviewMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorReviewSummaryDto getDoctorReviewSummary(Long doctorId) {
        // Validate doctor exists
        User doctor = validateAndGetDoctor(doctorId);
        
        Double averageRating = reviewRepository.getAverageRatingByDoctorId(doctorId);
        Long totalReviews = reviewRepository.countReviewsByDoctorId(doctorId);
        List<ReviewResponseDto> recentReviews = getRecentReviewsByDoctorId(doctorId, 5);
        
        return DoctorReviewSummaryDto.create(
            doctorId, 
            doctor.getFirstName() + " " + doctor.getLastName(), 
            averageRating, 
            totalReviews, 
            recentReviews
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageRatingByDoctorId(Long doctorId) {
        return reviewRepository.getAverageRatingByDoctorId(doctorId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getReviewCountByDoctorId(Long doctorId) {
        return reviewRepository.countReviewsByDoctorId(doctorId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPatientReviewedDoctor(Long patientId, Long doctorId) {
        return reviewRepository.findByPatientIdAndDoctorId(patientId, doctorId).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPatientReviewedAppointment(Long patientId, Long appointmentId) {
        return reviewRepository.findByPatientIdAndAppointmentId(patientId, appointmentId).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getRecentReviewsByDoctorId(Long doctorId, int limit) {
        return reviewRepository.findByDoctorId(doctorId).stream()
                .sorted((r1, r2) -> r2.getReviewDate().compareTo(r1.getReviewDate()))
                .limit(limit)
                .map(reviewMapper::toDto)
                .collect(Collectors.toList());
    }

    // Helper methods
    private User validateAndGetDoctor(Long doctorId) {
        Optional<User> doctorOpt = userService.getUserById(doctorId);
        if (doctorOpt.isEmpty()) {
            throw new AppException(HttpStatus.NOT_FOUND, "Doctor not found with id: " + doctorId);
        }
        
        User doctor = doctorOpt.get();
        if (doctor.getRoles().stream().noneMatch(role -> role.getName() == RoleEnum.DOCTOR)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "User with id " + doctorId + " is not a doctor");
        }
        
        return doctor;
    }

    private User validateAndGetPatient(Long patientId) {
        Optional<User> patientOpt = userService.getUserById(patientId);
        if (patientOpt.isEmpty()) {
            throw new AppException(HttpStatus.NOT_FOUND, "Patient not found with id: " + patientId);
        }
        
        User patient = patientOpt.get();
        if (patient.getRoles().stream().noneMatch(role -> role.getName() == RoleEnum.PATIENT)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "User with id " + patientId + " is not a patient");
        }
        
        return patient;
    }

    private Appointment validateAndGetAppointment(Long appointmentId) {
        try {
            // We need to get the actual Appointment entity, not just the DTO
            // Let me check if we can get it from the repository
            return appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Appointment not found with id: " + appointmentId));
        } catch (Exception e) {
            throw new AppException(HttpStatus.NOT_FOUND, "Appointment not found with id: " + appointmentId);
        }
    }
} 