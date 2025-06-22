package com.homosapiens.diagnocareservice.model.entity.appointment;

import com.homosapiens.diagnocareservice.model.entity.User;
import jakarta.persistence.*;
import lombok.Data;
import org.aspectj.weaver.patterns.ConcreteCflowPointcut;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "appointments")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private User doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private User patient;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", unique = true)
    private ScheduleSlot slot;
    
    @Column(columnDefinition = "TEXT")
    private String reason;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime  updatedAt;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;

    @Enumerated(EnumType.STRING)
    private AppointmentType type;
}

