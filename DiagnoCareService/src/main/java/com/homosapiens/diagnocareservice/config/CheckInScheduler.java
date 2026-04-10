package com.homosapiens.diagnocareservice.config;

import com.homosapiens.diagnocareservice.model.entity.CheckIn;
import com.homosapiens.diagnocareservice.model.entity.enums.CheckInStatus;
import com.homosapiens.diagnocareservice.repository.CheckInRepository;
import com.homosapiens.diagnocareservice.service.CheckInEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CheckInScheduler {

    private static final int MAX_REMINDER_AGE_DAYS = 7;

    private final CheckInRepository checkInRepository;
    private final CheckInEmailService checkInEmailService;

    @Scheduled(
        initialDelayString  = "${app.checkin.scheduler-initial-delay-ms:60000}",
        fixedDelayString    = "${app.checkin.scheduler-delay-ms:900000}"
    )
    @Transactional
    public void sendCheckInReminders() {
        List<CheckIn> due = checkInRepository.findDueReminders(LocalDateTime.now());
        LocalDateTime cutoff = LocalDateTime.now().minusDays(MAX_REMINDER_AGE_DAYS);

        for (CheckIn checkIn : due) {
            try {
                // Ne pas envoyer de rappel pour des check-ins trop anciens
                // (évite le spam lors d'un redémarrage après une longue période de tests)
                if (checkIn.getFirstReminderAt() != null
                        && checkIn.getFirstReminderAt().isBefore(cutoff)) {
                    log.warn("CheckIn id={} ignoré : firstReminderAt trop ancien ({})",
                            checkIn.getId(), checkIn.getFirstReminderAt());
                    checkIn.setStatus(CheckInStatus.COMPLETED);
                    checkInRepository.save(checkIn);
                    continue;
                }

                if (checkIn.getFirstSentAt() == null && checkIn.getFirstReminderAt() != null
                        && !LocalDateTime.now().isBefore(checkIn.getFirstReminderAt())) {
                    checkInEmailService.sendCheckInReminder(checkIn, false);
                    checkIn.setFirstSentAt(LocalDateTime.now());
                    checkIn.setStatus(CheckInStatus.SENT_24H);
                } else if (checkIn.getSecondSentAt() == null && checkIn.getSecondReminderAt() != null
                        && !LocalDateTime.now().isBefore(checkIn.getSecondReminderAt())) {
                    checkInEmailService.sendCheckInReminder(checkIn, true);
                    checkIn.setSecondSentAt(LocalDateTime.now());
                    checkIn.setStatus(CheckInStatus.SENT_48H);
                }
                checkInRepository.save(checkIn);
            } catch (Exception e) {
                log.error("Failed to send check-in reminder for checkInId={}", checkIn.getId(), e);
            }
        }
    }
}
