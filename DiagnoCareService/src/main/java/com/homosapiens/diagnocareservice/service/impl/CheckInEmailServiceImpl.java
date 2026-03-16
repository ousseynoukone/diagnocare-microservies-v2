package com.homosapiens.diagnocareservice.service.impl;

import com.homosapiens.diagnocareservice.model.entity.CheckIn;
import com.homosapiens.diagnocareservice.model.entity.PathologyResult;
import com.homosapiens.diagnocareservice.model.entity.Prediction;
import com.homosapiens.diagnocareservice.repository.PathologyResultRepository;
import com.homosapiens.diagnocareservice.service.AppSettingService;
import com.homosapiens.diagnocareservice.service.CheckInEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
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
        String body = buildHtmlBody(language, checkIn, prediction, topResult, checkInUrl);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setTo(checkIn.getUser().getEmail());
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new IllegalStateException("Failed to build email message", e);
        }
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

    private String buildHtmlBody(String language,
                             CheckIn checkIn,
                             Prediction prediction,
                             PathologyResult topResult,
                             String checkInUrl) {
        boolean isFrench = "fr".equals(language);
        String greeting = isFrench ? "Bonjour" : "Hello";
        String name = checkIn.getUser().getFirstName() != null ? checkIn.getUser().getFirstName() : "";
        String summaryTitle = isFrench ? "Résumé de votre dernière évaluation" : "Summary of your last assessment";
        String bestScoreLabel = isFrench ? "Score principal" : "Best score";
        String redAlertLabel = isFrench ? "Alerte rouge" : "Red alert";
        String topDiseaseLabel = isFrench ? "Pathologie principale" : "Top disease";
        String specialistLabel = isFrench ? "Spécialiste recommandé" : "Recommended specialist";
        String action = isFrench
                ? "Merci de mettre à jour vos symptômes via le lien ci-dessous."
                : "Please update your symptoms using the button below.";
        String buttonLabel = isFrench ? "Faire mon check-in" : "Complete my check-in";
        String benefitsTitle = isFrench ? "Pourquoi faire un check-in ?" : "Why complete a check-in?";
        String benefit1 = isFrench ? "Ajuster vos recommandations avec des données récentes" : "Update recommendations using your latest data";
        String benefit2 = isFrench ? "Détecter rapidement une évolution de votre état" : "Detect changes in your health early";
        String benefit3 = isFrench ? "Améliorer le suivi personnalisé de votre santé" : "Improve your personalized follow-up";
        String redAlertTitle = isFrench ? "Alerte rouge" : "Red alert";
        String redAlertMessage = isFrench
                ? "Votre évaluation indique une alerte rouge. Veuillez contacter un professionnel de santé rapidement et ne perdez pas de temps."
                : "Your assessment shows a red alert. Please contact a healthcare professional promptly and do not delay.";

        String topDisease = resolveLocalizedDisease(topResult, isFrench);
        String specialist = resolveLocalizedSpecialist(topResult, isFrench);
        String bestScore = prediction != null && prediction.getBestScore() != null
                ? prediction.getBestScore().toString()
                : "-";
        String redAlert = prediction != null && Boolean.TRUE.equals(prediction.getIsRedAlert())
                ? (isFrench ? "Oui" : "Yes")
                : (isFrench ? "Non" : "No");

        String redAlertHtml = prediction != null && Boolean.TRUE.equals(prediction.getIsRedAlert())
                ? """
                  <div style="margin:16px 0 12px;padding:12px 14px;border-radius:8px;background:#ffe7e6;color:#8a1f17;">
                    <div style="font-weight:700;margin-bottom:4px;">%s</div>
                    <div style="font-size:14px;line-height:1.5;">%s</div>
                  </div>
                  """.formatted(escapeHtml(redAlertTitle), escapeHtml(redAlertMessage))
                : "";

        return """
                <!doctype html>
                <html>
                  <head>
                    <meta charset="UTF-8" />
                  </head>
                  <body style="margin:0;padding:0;background-color:#f3f6fb;font-family:Arial,Helvetica,sans-serif;color:#1f2a44;">
                    <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="background-color:#f3f6fb;padding:24px 0;">
                      <tr>
                        <td align="center">
                          <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="600" style="background:#ffffff;border-radius:12px;box-shadow:0 6px 24px rgba(31,42,68,0.12);overflow:hidden;">
                            <tr>
                              <td style="padding:28px 32px;background:linear-gradient(90deg,#1b84ff,#58a6ff);color:#ffffff;">
                                <div style="font-size:20px;font-weight:700;">DiagnoCare</div>
                                <div style="font-size:14px;opacity:0.9;">%s %s,</div>
                              </td>
                            </tr>
                            <tr>
                              <td style="padding:28px 32px;">
                                <div style="font-size:18px;font-weight:600;margin-bottom:12px;">%s</div>
                                <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="border-collapse:collapse;">
                                  <tr>
                                    <td style="padding:8px 0;font-weight:600;">%s</td>
                                    <td style="padding:8px 0;text-align:right;">%s</td>
                                  </tr>
                                  <tr>
                                    <td style="padding:8px 0;font-weight:600;">%s</td>
                                    <td style="padding:8px 0;text-align:right;">%s</td>
                                  </tr>
                                  <tr>
                                    <td style="padding:8px 0;font-weight:600;">%s</td>
                                    <td style="padding:8px 0;text-align:right;">%s</td>
                                  </tr>
                                  <tr>
                                    <td style="padding:8px 0;font-weight:600;">%s</td>
                                    <td style="padding:8px 0;text-align:right;">%s</td>
                                  </tr>
                                </table>
                                %s
                                <div style="margin:24px 0 8px;font-size:14px;line-height:1.5;color:#3d4b66;">%s</div>
                                <div style="margin:8px 0 12px;font-size:14px;color:#3d4b66;">
                                  <div style="font-weight:600;margin-bottom:6px;">%s</div>
                                  <ul style="margin:0;padding-left:18px;">
                                    <li style="margin-bottom:4px;">%s</li>
                                    <li style="margin-bottom:4px;">%s</li>
                                    <li>%s</li>
                                  </ul>
                                </div>
                                <a href="%s" style="display:inline-block;padding:12px 18px;background:#1b84ff;color:#ffffff;text-decoration:none;border-radius:8px;font-weight:600;">%s</a>
                                <div style="margin-top:16px;font-size:12px;color:#7b879b;">%s</div>
                              </td>
                            </tr>
                          </table>
                        </td>
                      </tr>
                    </table>
                  </body>
                </html>
                """.formatted(
                greeting,
                escapeHtml(name),
                escapeHtml(summaryTitle),
                escapeHtml(bestScoreLabel),
                escapeHtml(bestScore),
                escapeHtml(redAlertLabel),
                escapeHtml(redAlert),
                escapeHtml(topDiseaseLabel),
                escapeHtml(topDisease),
                escapeHtml(specialistLabel),
                escapeHtml(specialist),
                redAlertHtml,
                escapeHtml(action),
                escapeHtml(benefitsTitle),
                escapeHtml(benefit1),
                escapeHtml(benefit2),
                escapeHtml(benefit3),
                escapeHtml(checkInUrl),
                escapeHtml(buttonLabel),
                escapeHtml(checkInUrl)
        );
    }

    private String normalizeLanguage(String language) {
        if (language == null || language.trim().isEmpty()) {
            return "fr";
        }
        String normalized = language.trim().toLowerCase();
        return normalized.equals("fr") || normalized.equals("en") ? normalized : "fr";
    }

    private String resolveLocalizedDisease(PathologyResult topResult, boolean isFrench) {
        if (topResult == null) {
            return "-";
        }
        if (isFrench) {
            return (topResult.getLocalizedDiseaseName() != null && !topResult.getLocalizedDiseaseName().isBlank())
                    ? topResult.getLocalizedDiseaseName()
                    : "-";
        }
        if (topResult.getPathology() != null && topResult.getPathology().getPathologyName() != null) {
            return topResult.getPathology().getPathologyName();
        }
        return "-";
    }

    private String resolveLocalizedSpecialist(PathologyResult topResult, boolean isFrench) {
        if (topResult == null) {
            return "-";
        }
        if (isFrench) {
            return (topResult.getLocalizedSpecialistLabel() != null && !topResult.getLocalizedSpecialistLabel().isBlank())
                    ? topResult.getLocalizedSpecialistLabel()
                    : "-";
        }
        if (topResult.getDoctor() != null && topResult.getDoctor().getSpecialistLabel() != null) {
            return topResult.getDoctor().getSpecialistLabel();
        }
        return "-";
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
