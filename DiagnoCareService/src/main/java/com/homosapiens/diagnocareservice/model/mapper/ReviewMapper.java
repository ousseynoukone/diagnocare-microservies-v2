package com.homosapiens.diagnocareservice.model.mapper;

import com.homosapiens.diagnocareservice.model.entity.Review;
import com.homosapiens.diagnocareservice.model.entity.User;
import com.homosapiens.diagnocareservice.model.entity.appointment.Appointment;
import com.homosapiens.diagnocareservice.model.entity.dtos.ReviewRequestDto;
import com.homosapiens.diagnocareservice.model.entity.dtos.ReviewResponseDto;
import com.homosapiens.diagnocareservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReviewMapper {
    
    private final UserService userService;
    
    public Review toEntity(ReviewRequestDto dto) {
        Review review = new Review();
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        
        // Set doctor
        Optional<User> doctor = userService.getUserById(dto.getDoctorId());
        if (doctor.isPresent()) {
            review.setDoctor(doctor.get());
        }
        
        // Set patient
        Optional<User> patient = userService.getUserById(dto.getPatientId());
        if (patient.isPresent()) {
            review.setPatient(patient.get());
        }
        
        // Set appointment if provided
        if (dto.getAppointmentId() != null) {
            // Note: You might need to inject AppointmentService here if you want to validate the appointment
            // For now, we'll just set the ID and let the entity handle the relationship
        }
        
        return review;
    }
    
    public ReviewResponseDto toDto(Review review) {
        return ReviewResponseDto.fromEntity(review);
    }
} 