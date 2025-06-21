package com.homosapiens.diagnocareservice.model.entity.appointment;

import com.homosapiens.diagnocareservice.model.entity.availability.Availability;
import com.homosapiens.diagnocareservice.model.entity.availability.WeekDay;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "schedule_slots")
public class ScheduleSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalTime fromTime;
    private LocalTime toTime;
    private Boolean isActive;

    @NotNull(message = "Booking status is required")
    private boolean IsBooked;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "week_day_id")
    private WeekDay weekDay;

    @OneToOne(fetch = FetchType.LAZY )
    private Appointment appointment;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private LocalDate slotDate;

    // Helper method to get availability through weekDay
    public Availability getAvailability() {
        return weekDay != null ? weekDay.getAvailability() : null;
    }
}