package com.homosapiens.diagnocareservice.service.impl;

import com.homosapiens.diagnocareservice.model.entity.CheckIn;
import com.homosapiens.diagnocareservice.model.entity.PathologyResult;
import com.homosapiens.diagnocareservice.model.entity.Prediction;
import com.homosapiens.diagnocareservice.repository.PathologyResultRepository;
import com.homosapiens.diagnocareservice.service.AppSettingService;
import com.homosapiens.diagnocareservice.service.CheckInEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckInEmailServiceImpl implements CheckInEmailService {

    private static final String CHECKIN_BASE_URL_KEY = "CHECKIN_BASE_URL";

    private final JavaMailSender mailSender;
    private final PathologyResultRepository pathologyResultRepository;
    private final AppSettingService appSettingService;

    @Value("${app.checkin.base-url:http://localhost:3000/check-in}")
    private String defaultCheckInBaseUrl;

    @Override
    public void sendCheckInReminder(CheckIn checkIn, boolean isSecondReminder) {
        if (checkIn.getUser() == null || checkIn.getUser().getEmail() == null) {
            return;
        }

        String language = normalizeLanguage(checkIn.getUser().getLang());
        Prediction prediction = checkIn.getPreviousPrediction();
        PathologyResult topResult = resolveTopResult(prediction);

        String checkInUrl = buildCheckInUrl(checkIn);
        String subject = buildSubject(language, isSecondReminder);
        String body = buildBody(language, checkIn, prediction, topResult, checkInUrl);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(checkIn.getUser().getEmail());
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }

    private PathologyResult resolveTopResult(Prediction prediction) {
        if (prediction == null) {
            return null;
        }
        List<PathologyResult> results = pathologyResultRepository.findByPredictionId(prediction.getId());
        return results.stream()
                .max(Comparator.comparing(PathologyResult::getDiseaseScore, Comparator.nullsLast(BigDecimal::compareTo)))
                .orElse(null);
    }

    private String buildCheckInUrl(CheckIn checkIn) {
        String baseUrl = appSettingService.getValue(CHECKIN_BASE_URL_KEY, defaultCheckInBaseUrl);
        String delimiter = baseUrl.contains("?") ? "&" : "?";
        return baseUrl + delimiter + "checkInId=" + checkIn.getId();
    }

    private String buildSubject(String language, boolean isSecondReminder) {
        if ("fr".equals(language)) {
            return isSecondReminder
                    ? "Rappel: Faites votre check-in santé"
                    : "Check-in santé: donnez-nous des nouvelles";
        }
        return isSecondReminder
                ? "Reminder: Please complete your health check-in"
                : "Health check-in: how are you feeling?";
    }

    private String buildBody(String language,
                             CheckIn checkIn,
                             Prediction prediction,
                             PathologyResult topResult,
                             String checkInUrl) {
        String greeting = "fr".equals(language) ? "Bonjour" : "Hello";
        String name = checkIn.getUser().getFirstName() != null ? checkIn.getUser().getFirstName() : "";
        String summaryTitle = "fr".equals(language) ? "Résumé de votre dernière évaluation" : "Summary of your last assessment";
        String bestScoreLabel = "fr".equals(language) ? "Score principal" : "Best score";
        String redAlertLabel = "fr".equals(language) ? "Alerte rouge" : "Red alert";
        String topDiseaseLabel = "fr".equals(language) ? "Pathologie principale" : "Top disease";
        String specialistLabel = "fr".equals(language) ? "Spécialiste recommandé" : "Recommended specialist";
        String action = "fr".equals(language)
                ? "Merci de mettre à jour vos symptômes via ce lien :"
                : "Please update your symptoms using this link:";

        String topDisease = topResult != null && topResult.getPathology() != null
                ? topResult.getPathology().getPathologyName()
                : "-";
        String specialist = topResult != null && topResult.getDoctor() != null
                ? topResult.getDoctor().getSpecialistLabel()
                : "-";
        String bestScore = prediction != null && prediction.getBestScore() != null
                ? prediction.getBestScore().toString()
                : "-";
        String redAlert = prediction != null && Boolean.TRUE.equals(prediction.getIsRedAlert())
                ? ("fr".equals(language) ? "Oui" : "Yes")
                : ("fr".equals(language) ? "Non" : "No");

        return String.join("\n",
                greeting + " " + name + ",",
                "",
                summaryTitle + ":",
                "- " + bestScoreLabel + ": " + bestScore,
                "- " + redAlertLabel + ": " + redAlert,
                "- " + topDiseaseLabel + ": " + topDisease,
                "- " + specialistLabel + ": " + specialist,
                "",
                action,
                checkInUrl
        );
    }

    private String normalizeLanguage(String language) {
        if (language == null || language.trim().isEmpty()) {
            return "fr";
        }
        String normalized = language.trim().toLowerCase();
        return normalized.equals("fr") || normalized.equals("en") ? normalized : "fr";
    }
}
