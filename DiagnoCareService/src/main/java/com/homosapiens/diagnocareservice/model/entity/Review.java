package com.homosapiens.diagnocareservice.model.entity;

import com.homosapiens.diagnocareservice.core.exception.AppException;
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
    @JoinColumn(name = "user_id")
    private User doctor;

    @PrePersist
    @PreUpdate
    private void validateUserRole() {
        if (this.doctor == null || this.doctor.getRoles().stream()
                .noneMatch(role -> role.getName() == RoleEnum.DOCTOR)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "User must be a doctor");
        }
    }
} 