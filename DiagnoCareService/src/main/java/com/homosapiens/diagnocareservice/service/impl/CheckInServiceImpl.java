package com.homosapiens.diagnocareservice.service.impl;

import com.homosapiens.diagnocareservice.core.exception.AppException;
import com.homosapiens.diagnocareservice.dto.CheckInCreateRequestDTO;
import com.homosapiens.diagnocareservice.dto.CheckInResponseDTO;
import com.homosapiens.diagnocareservice.dto.PredictionCreationResult;
import com.homosapiens.diagnocareservice.dto.SessionSymptomRequestDTO;
import com.homosapiens.diagnocareservice.model.entity.CheckIn;
import com.homosapiens.diagnocareservice.model.entity.PathologyResult;
import com.homosapiens.diagnocareservice.model.entity.Prediction;
import com.homosapiens.diagnocareservice.model.entity.User;
import com.homosapiens.diagnocareservice.model.entity.enums.CheckInOutcome;
import com.homosapiens.diagnocareservice.model.entity.enums.CheckInStatus;
import com.homosapiens.diagnocareservice.repository.CheckInRepository;
import com.homosapiens.diagnocareservice.repository.PathologyResultRepository;
import com.homosapiens.diagnocareservice.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckInServiceImpl implements CheckInService {

    private static final BigDecimal WORSE_THRESHOLD = BigDecimal.valueOf(10.0);

    private final CheckInRepository checkInRepository;
    private final PredictionService predictionService;
    private final PredictionWorkflowService predictionWorkflowService;
    private final UserService userService;
    private final UrgentDiseaseService urgentDiseaseService;
    private final PathologyResultRepository pathologyResultRepository;
    private final com.homosapiens.diagnocareservice.repository.PredictionRepository predictionRepository;

    @Value("${app.checkin.first-reminder-minutes:1440}")
    private long firstReminderMinutes;

    @Value("${app.checkin.second-reminder-minutes:2880}")
    private long secondReminderMinutes;

    @Override
    @Transactional
    public CheckIn scheduleCheckIn(Prediction prediction) {
        if (prediction.getPreviousPrediction() != null) {
            return null;
        }
        CheckIn checkIn = new CheckIn();
        checkIn.setUser(prediction.getSessionSymptom().getUser());
        checkIn.setPreviousPrediction(prediction);
        LocalDateTime baseTime = prediction.getCreatedDate() != null
                ? prediction.getCreatedDate()
                : LocalDateTime.now();
        checkIn.setFirstReminderAt(baseTime.plusMinutes(firstReminderMinutes));
        checkIn.setSecondReminderAt(baseTime.plusMinutes(secondReminderMinutes));
        checkIn.setStatus(CheckInStatus.PENDING);
        return checkInRepository.save(checkIn);
    }

    @Override
    @Transactional
    public CheckInResponseDTO submitCheckIn(CheckInCreateRequestDTO requestDTO) {
        Prediction previousPrediction = predictionService.getPredictionById(requestDTO.getPreviousPredictionId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Previous prediction not found"));

        User user = userService.getUserById(requestDTO.getUserId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "User not found"));

        if (!previousPrediction.getSessionSymptom().getUser().getId().equals(user.getId())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Prediction does not belong to user");
        }

        CheckIn existingCheckIn = checkInRepository.findByPreviousPredictionIdAndUserId(previousPrediction.getId(), user.getId())
                .orElse(null);
        if (existingCheckIn != null && CheckInStatus.COMPLETED.equals(existingCheckIn.getStatus())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Check-in already completed for this prediction");
        }

        SessionSymptomRequestDTO symptomRequestDTO = new SessionSymptomRequestDTO();
        symptomRequestDTO.setUserId(requestDTO.getUserId());
        symptomRequestDTO.setSymptomIds(requestDTO.getSymptomIds());
        symptomRequestDTO.setSymptomLabels(requestDTO.getSymptomLabels());

        PredictionCreationResult result = predictionWorkflowService.createPrediction(symptomRequestDTO, previousPrediction.getId());
        Prediction newPrediction = result.getPrediction();

        CheckIn checkIn = existingCheckIn != null
                ? existingCheckIn
                : createNewCheckIn(user, previousPrediction);

        CheckInOutcome outcome = determineOutcome(previousPrediction, newPrediction);
        String worseReason = determineWorseReason(previousPrediction, newPrediction);

        checkIn.setOutcome(outcome);
        checkIn.setWorseReason(worseReason);
        checkIn.setStatus(CheckInStatus.COMPLETED);
        checkIn.setCompletedAt(LocalDateTime.now());

        CheckIn saved = checkInRepository.save(checkIn);

        return toDto(saved, previousPrediction, newPrediction);
    }

    private CheckIn createNewCheckIn(User user, Prediction previousPrediction) {
        CheckIn created = new CheckIn();
        created.setUser(user);
        created.setPreviousPrediction(previousPrediction);
        created.setStatus(CheckInStatus.PENDING);
        return created;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CheckInResponseDTO> getCheckInsByUser(Long userId) {
        List<CheckIn> checkIns = checkInRepository.findByUserId(userId);
        return checkIns.stream()
                .map(checkIn -> {
                    Prediction previous = checkIn.getPreviousPrediction();
                    Prediction latestChild = resolveLatestChildPrediction(previous);
                    return toDto(checkIn, previous, latestChild);
                })
                .toList();
    }

    private CheckInOutcome determineOutcome(Prediction previousPrediction, Prediction newPrediction) {
        if (isWorse(previousPrediction, newPrediction)) {
            return CheckInOutcome.WORSENING;
        }
        BigDecimal delta = calculateDelta(previousPrediction, newPrediction);
        if (delta.compareTo(WORSE_THRESHOLD.negate()) <= 0) {
            return CheckInOutcome.IMPROVING;
        }
        return CheckInOutcome.STABLE;
    }

    private boolean isWorse(Prediction previousPrediction, Prediction newPrediction) {
        if (Boolean.TRUE.equals(newPrediction.getIsRedAlert())) {
            return true;
        }
        String topDisease = resolveTopDisease(newPrediction);
        if (urgentDiseaseService.isUrgentDisease(topDisease)) {
            return true;
        }
        BigDecimal delta = calculateDelta(previousPrediction, newPrediction);
        return delta.compareTo(WORSE_THRESHOLD) >= 0;
    }

    private String determineWorseReason(Prediction previousPrediction, Prediction newPrediction) {
        StringBuilder reason = new StringBuilder();
        if (Boolean.TRUE.equals(newPrediction.getIsRedAlert())) {
            reason.append("red_alert;");
        }
        String topDisease = resolveTopDisease(newPrediction);
        if (urgentDiseaseService.isUrgentDisease(topDisease)) {
            reason.append("urgent_disease;");
        }
        BigDecimal delta = calculateDelta(previousPrediction, newPrediction);
        if (delta.compareTo(WORSE_THRESHOLD) >= 0) {
            reason.append("score_increase;");
        }
        return reason.length() == 0 ? null : reason.toString();
    }

    private BigDecimal calculateDelta(Prediction previousPrediction, Prediction newPrediction) {
        BigDecimal previous = previousPrediction.getBestScore() != null ? previousPrediction.getBestScore() : BigDecimal.ZERO;
        BigDecimal current = newPrediction.getBestScore() != null ? newPrediction.getBestScore() : BigDecimal.ZERO;
        return current.subtract(previous);
    }

    private String resolveTopDisease(Prediction prediction) {
        List<PathologyResult> results = pathologyResultRepository.findByPredictionId(prediction.getId());
        return results.stream()
                .max(Comparator.comparing(PathologyResult::getDiseaseScore, Comparator.nullsLast(BigDecimal::compareTo)))
                .map(result -> result.getPathology() != null ? result.getPathology().getPathologyName() : null)
                .orElse(null);
    }

    private Prediction resolveLatestChildPrediction(Prediction previousPrediction) {
        if (previousPrediction == null) {
            return null;
        }
        List<Prediction> children = predictionRepository.findByPreviousPredictionId(previousPrediction.getId());
        return children.stream()
                .max(Comparator.comparing(Prediction::getCreatedDate, Comparator.nullsLast(LocalDateTime::compareTo)))
                .orElse(null);
    }

    private CheckInResponseDTO toDto(CheckIn checkIn, Prediction previousPrediction, Prediction newPrediction) {
        CheckInResponseDTO dto = new CheckInResponseDTO();
        dto.setId(checkIn.getId());
        dto.setUserId(checkIn.getUser().getId());
        dto.setPreviousPredictionId(previousPrediction != null ? previousPrediction.getId() : null);
        dto.setStatus(checkIn.getStatus());
        dto.setOutcome(checkIn.getOutcome());
        dto.setWorseReason(checkIn.getWorseReason());
        dto.setFirstReminderAt(checkIn.getFirstReminderAt());
        dto.setSecondReminderAt(checkIn.getSecondReminderAt());
        dto.setCompletedAt(checkIn.getCompletedAt());
        dto.setPreviousBestScore(previousPrediction != null ? previousPrediction.getBestScore() : null);
        dto.setNewBestScore(newPrediction != null ? newPrediction.getBestScore() : null);
        if (previousPrediction != null && newPrediction != null) {
            dto.setBestScoreDelta(calculateDelta(previousPrediction, newPrediction));
        }
        return dto;
    }
}
