package com.homosapiens.diagnocareservice.model.entity.availability;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.homosapiens.diagnocareservice.core.exception.AppException;
import com.homosapiens.diagnocareservice.model.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "availabilities")
@NoArgsConstructor
public class Availability {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Future(message = "Repeat until date must be in the future")
    private LocalDate repeatUntil;

    @NotNull(message = "Availability date is required")
    private LocalDate availabilityDate;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "availability", fetch = FetchType.LAZY)
    @JsonManagedReference
    private Set<WeekDay> weekDays = new HashSet<>();

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


    @PrePersist
    public void prePersist() {
        if (availabilityDate == null) {
            availabilityDate = LocalDate.now();
        }
    }
}

