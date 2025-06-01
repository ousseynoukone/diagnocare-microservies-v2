package com.homosapiens.diagnocareservice.model.entity;

import com.homosapiens.diagnocareservice.model.entity.appointment.Appointment;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "reports")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 254)
    private String label;

    @Column(length = 254)
    private String diagnostic;

    private LocalDateTime date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    private String identifiant_1;
} 