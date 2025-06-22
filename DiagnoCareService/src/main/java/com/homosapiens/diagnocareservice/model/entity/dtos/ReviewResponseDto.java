package com.homosapiens.diagnocareservice.model.entity.dtos;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewResponseDto {
    private Long id;
    private Integer rating;
    private String comment;
    private LocalDateTime reviewDate;
    private Long doctorId;
    private String doctorName;
    private Long patientId;
    private String patientName;
    private Long appointmentId;
    
    public static ReviewResponseDto fromEntity(com.homosapiens.diagnocareservice.model.entity.Review review) {
        ReviewResponseDto dto = new ReviewResponseDto();
        dto.setId(review.getId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setReviewDate(review.getReviewDate());
        dto.setDoctorId(review.getDoctor().getId());
        dto.setDoctorName(review.getDoctor().getFirstName() + " " + review.getDoctor().getLastName());
        dto.setPatientId(review.getPatient().getId());
        dto.setPatientName(review.getPatient().getFirstName() + " " + review.getPatient().getLastName());
        if (review.getAppointment() != null) {
            dto.setAppointmentId(review.getAppointment().getId());
        }
        return dto;
    }
} 