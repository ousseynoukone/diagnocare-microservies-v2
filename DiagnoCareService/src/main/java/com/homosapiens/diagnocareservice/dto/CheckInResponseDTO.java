package com.homosapiens.diagnocareservice.dto;

import com.homosapiens.diagnocareservice.model.entity.enums.CheckInOutcome;
import com.homosapiens.diagnocareservice.model.entity.enums.CheckInStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CheckInResponseDTO {
    private Long id;
    private Long userId;
    private Long previousPredictionId;
    private CheckInStatus status;
    private CheckInOutcome outcome;
    private String worseReason;
    private BigDecimal previousBestScore;
    private BigDecimal newBestScore;
    private BigDecimal bestScoreDelta;
    private LocalDateTime firstReminderAt;
    private LocalDateTime secondReminderAt;
    private LocalDateTime completedAt;
}
