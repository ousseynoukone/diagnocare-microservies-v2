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
    public CheckInResponseDTO activateCheckIn(Long predictionId, Long userId) {
        Prediction prediction = predictionService.getPredictionById(predictionId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Prediction not found"));

        User user = userService.getUserById(userId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "User not found"));

        if (!prediction.getSessionSymptom().getUser().getId().equals(user.getId())) {
            throw new AppException(HttpStatus.FORBIDDEN, "Prediction does not belong to user");
        }

        // Idempotent: return if already activated
        List<CheckIn> existing = checkInRepository.findAllByPreviousPredictionIdAndUserId(predictionId, userId);
        if (!existing.isEmpty()) {
            existing.sort(Comparator.comparing(CheckIn::getFirstReminderAt, Comparator.nullsLast(LocalDateTime::compareTo)));
            CheckIn first = existing.get(0);
            CheckInResponseDTO dto = toDto(first, prediction, null);
            LocalDateTime j2Time = existing.size() > 1
                    ? existing.get(1).getFirstReminderAt()
                    : first.getFirstReminderAt().plusMinutes(firstReminderMinutes);
            dto.setSecondReminderAt(j2Time);
            return dto;
        }

        // J+1 email reminder fires 24h after prediction creation (anchored to prediction, not activation)
        LocalDateTime baseTime = prediction.getCreatedDate() != null
                ? prediction.getCreatedDate()
                : LocalDateTime.now();

        CheckIn checkIn1 = new CheckIn();
        checkIn1.setUser(user);
        checkIn1.setPreviousPrediction(prediction);
        checkIn1.setFirstReminderAt(baseTime.plusMinutes(firstReminderMinutes));
        checkIn1.setStatus(CheckInStatus.PENDING);
        CheckIn saved1 = checkInRepository.save(checkIn1);

        // J+2 is created when J+1 is submitted — estimate the date for display only
        CheckInResponseDTO dto = toDto(saved1, prediction, null);
        dto.setSecondReminderAt(saved1.getFirstReminderAt().plusMinutes(firstReminderMinutes));
        return dto;
    }

    @Override
    @Transactional
    public CheckInResponseDTO submitCheckIn(CheckInCreateRequestDTO requestDTO) {
        CheckIn checkIn = checkInRepository.findById(requestDTO.getCheckInId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Check-in not found with id: " + requestDTO.getCheckInId()));

        if (!checkIn.getUser().getId().equals(requestDTO.getUserId())) {
            throw new AppException(HttpStatus.FORBIDDEN, "Check-in does not belong to user");
        }

        if (checkIn.getStatus() == CheckInStatus.COMPLETED) {
            Prediction previous = checkIn.getPreviousPrediction();
            Prediction latestChild = resolveLatestChildPrediction(previous);
            return toDto(checkIn, previous, latestChild);
        }

        Prediction previousPrediction = checkIn.getPreviousPrediction();

        SessionSymptomRequestDTO symptomRequestDTO = new SessionSymptomRequestDTO();
        symptomRequestDTO.setUserId(requestDTO.getUserId());
        symptomRequestDTO.setSymptomLabels(requestDTO.getSymptomLabels());

        PredictionCreationResult result = predictionWorkflowService.createPrediction(symptomRequestDTO, previousPrediction.getId());
        Prediction newPrediction = result.getPrediction();

        CheckInOutcome outcome = determineOutcome(previousPrediction, newPrediction);
        String worseReason = determineWorseReason(previousPrediction, newPrediction);

        checkIn.setOutcome(outcome);
        checkIn.setWorseReason(worseReason);
        checkIn.setStatus(CheckInStatus.COMPLETED);
        checkIn.setCompletedAt(LocalDateTime.now());

        CheckIn saved = checkInRepository.save(checkIn);

        // If this was the first (and only) check-in for this prediction, auto-create J+2
        // J+2 email fires 24h from now, giving the user time to feel the difference
        List<CheckIn> existing = checkInRepository.findAllByPreviousPredictionIdAndUserId(
                previousPrediction.getId(), saved.getUser().getId());
        if (existing.size() == 1) {
            CheckIn checkIn2 = new CheckIn();
            checkIn2.setUser(saved.getUser());
            checkIn2.setPreviousPrediction(previousPrediction);
            checkIn2.setFirstReminderAt(LocalDateTime.now().plusMinutes(firstReminderMinutes));
            checkIn2.setStatus(CheckInStatus.PENDING);
            checkInRepository.save(checkIn2);
        }

        return toDto(saved, previousPrediction, newPrediction);
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
        List<Prediction> children = predictionRepository.findByPreviousPredictionIdAndDeletedFalse(previousPrediction.getId());
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
