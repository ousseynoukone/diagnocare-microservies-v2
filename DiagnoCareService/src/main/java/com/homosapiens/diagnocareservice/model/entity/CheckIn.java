package com.homosapiens.diagnocareservice.model.entity;

import com.homosapiens.diagnocareservice.model.entity.enums.CheckInOutcome;
import com.homosapiens.diagnocareservice.model.entity.enums.CheckInStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(name = "check_ins")
public class CheckIn extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_prediction_id", nullable = false)
    private Prediction previousPrediction;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CheckInStatus status = CheckInStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CheckInOutcome outcome;

    @Column(name = "worse_reason", columnDefinition = "TEXT")
    private String worseReason;

    @Column(name = "first_reminder_at")
    private LocalDateTime firstReminderAt;

    @Column(name = "second_reminder_at")
    private LocalDateTime secondReminderAt;

    @Column(name = "first_sent_at")
    private LocalDateTime firstSentAt;

    @Column(name = "second_sent_at")
    private LocalDateTime secondSentAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
