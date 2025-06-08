package com.homosapiens.diagnocareservice.model.entity.appointment;

import com.homosapiens.diagnocareservice.model.entity.availability.Availability;
import com.homosapiens.diagnocareservice.model.entity.availability.WeekDay;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalTime;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "availability_id")
    private Availability availability;

    @NotNull(message = "Booking status is required")
    private boolean IsBooked;

    @NotNull(message = "Availability status is required")
    private boolean isAvailable;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "week_day_id", nullable = false)
    private WeekDay weekDay;




}