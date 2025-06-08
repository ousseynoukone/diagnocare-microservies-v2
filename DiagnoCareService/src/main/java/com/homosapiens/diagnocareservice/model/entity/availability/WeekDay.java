package com.homosapiens.diagnocareservice.model.entity.availability;

import com.homosapiens.diagnocareservice.core.exception.AppException;
import com.homosapiens.diagnocareservice.model.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "weekdays")
@EqualsAndHashCode(exclude = "availability")

public class WeekDay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Start time is required")
    private LocalTime fromTime;

    @NotNull(message = "End time is required")
    private LocalTime toTime;

    @NotNull(message = "Week day is required")
    @Enumerated(EnumType.STRING)
    private DaysOfWeek daysOfWeek;

    @NotNull(message = "Slot duration is required")
    @Min(value = 10, message = "Slot duration must be at least 10 minutes")
    private Integer slotDuration;



//    @NotNull(message = "Availability is required")
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "availability_id")
//    private Availability availability;


    @PrePersist
    @PreUpdate
    private void validateTimeRange() {
        if (fromTime != null && toTime != null && fromTime.isAfter(toTime)) {
            throw new AppException(HttpStatus.BAD_REQUEST,"Start time must be before end time");
        }
    }
}


