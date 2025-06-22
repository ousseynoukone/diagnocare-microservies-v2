package com.homosapiens.diagnocareservice.model.entity;

import com.homosapiens.diagnocareservice.core.exception.AppException;
import com.homosapiens.diagnocareservice.model.entity.appointment.Appointment;
import com.homosapiens.diagnocareservice.model.entity.enums.RoleEnum;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer rating;
    
    @Column(columnDefinition = "TEXT")
    private String comment;
    
    private LocalDateTime reviewDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private User doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private User patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @PrePersist
    @PreUpdate
    private void validateReview() {
        // Validate that doctor is actually a doctor
        if (this.doctor == null || this.doctor.getRoles().stream()
                .noneMatch(role -> role.getName() == RoleEnum.DOCTOR)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Doctor must be a user with DOCTOR role");
        }
        
        // Validate that patient is actually a patient
        if (this.patient == null || this.patient.getRoles().stream()
                .noneMatch(role -> role.getName() == RoleEnum.PATIENT)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Patient must be a user with PATIENT role");
        }
        
        // Set review date if not set
        if (this.reviewDate == null) {
            this.reviewDate = LocalDateTime.now();
        }
        
        // Validate rating
        if (this.rating == null || this.rating < 1 || this.rating > 5) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5");
        }
    }
} 