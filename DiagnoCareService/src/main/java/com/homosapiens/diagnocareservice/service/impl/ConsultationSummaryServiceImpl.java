package com.homosapiens.diagnocareservice.service.impl;

import com.homosapiens.diagnocareservice.core.exception.AppException;
import com.homosapiens.diagnocareservice.dto.ConsultationSummaryDTO;
import com.homosapiens.diagnocareservice.dto.MLTranslationRequestDTO;
import com.homosapiens.diagnocareservice.dto.MLTranslationResponseDTO;
import com.homosapiens.diagnocareservice.dto.PathologySummaryDTO;
import com.homosapiens.diagnocareservice.dto.TimelineEntryDTO;
import com.homosapiens.diagnocareservice.model.entity.CheckIn;
import com.homosapiens.diagnocareservice.model.entity.enums.CheckInOutcome;
import com.homosapiens.diagnocareservice.model.entity.enums.CheckInStatus;
import com.homosapiens.diagnocareservice.model.entity.SessionSymptom;
import com.homosapiens.diagnocareservice.model.entity.User;
import com.homosapiens.diagnocareservice.model.entity.Prediction;
import com.homosapiens.diagnocareservice.repository.CheckInRepository;
import com.homosapiens.diagnocareservice.repository.PredictionRepository;
import com.homosapiens.diagnocareservice.repository.SessionSymptomRepository;
import com.homosapiens.diagnocareservice.service.ConsultationSummaryService;
import com.homosapiens.diagnocareservice.service.TranslationService;
import com.homosapiens.diagnocareservice.service.PathologyResultService;
import com.itextpdf.html2pdf.HtmlConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConsultationSummaryServiceImpl implements ConsultationSummaryService {

    private final PredictionRepository predictionRepository;
    private final SessionSymptomRepository sessionSymptomRepository;
    private final PathologyResultService pathologyResultService;
    private final TranslationService translationService;
    private final CheckInRepository checkInRepository;

    @Override
    public ByteArrayOutputStream generatePdfSummary(Long predictionId) {
        ConsultationSummaryDTO summary = generateSummaryData(predictionId);
        String htmlContent = generateHtmlContent(summary);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            HtmlConverter.convertToPdf(htmlContent, outputStream);
        } catch (Exception e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error generating PDF: " + e.getMessage(), e);
        }
        
        return outputStream;
    }

    @Override
    public ConsultationSummaryDTO generateSummaryData(Long predictionId) {
        Prediction prediction = predictionRepository.findById(predictionId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, 
                        "Prediction not found with id: " + predictionId));

        SessionSymptom sessionSymptom = prediction.getSessionSymptom();
        if (sessionSymptom == null) {
            throw new AppException(HttpStatus.NOT_FOUND, 
                    "Session symptom not found for prediction: " + predictionId);
        }

        User user = sessionSymptom.getUser();
        String language = normalizeLanguage(user.getLang());
        String patientName = user.getFirstName() + " " + user.getLastName();

        List<String> symptoms = sessionSymptom.getSymptoms() != null
                ? sessionSymptom.getSymptoms().stream()
                    .map(symptom -> symptom.getLabel())
                    .collect(Collectors.toList())
                : new ArrayList<>();

        List<com.homosapiens.diagnocareservice.model.entity.PathologyResult> pathologyResults = pathologyResultService
                .getPathologyResultsByPredictionId(predictionId);

        List<String> diseaseNames = pathologyResults.stream()
                .map(pr -> pr.getPathology().getPathologyName())
                .collect(Collectors.toList());

        List<String> specialistLabels = pathologyResults.stream()
                .map(pr -> pr.getDoctor().getSpecialistLabel())
                .collect(Collectors.toList());

        MLTranslationResponseDTO translations = translationService.translate(
                MLTranslationRequestDTO.builder()
                        .language(language)
                        .symptoms(symptoms)
                        .diseases(diseaseNames)
                        .specialists(specialistLabels)
                        .build()
        );

        List<String> translatedSymptoms = translations != null && translations.getSymptoms() != null
                ? translations.getSymptoms()
                : new ArrayList<>();

        List<String> potentialPathologies = translations != null && translations.getDiseases() != null
                ? translations.getDiseases()
                : new ArrayList<>();

        List<String> translatedSpecialists = translations != null && translations.getSpecialists() != null
                ? translations.getSpecialists()
                : new ArrayList<>();

        List<PathologySummaryDTO> pathologyDetails = new ArrayList<>();
        for (int i = 0; i < pathologyResults.size(); i++) {
            com.homosapiens.diagnocareservice.model.entity.PathologyResult result = pathologyResults.get(i);
            String pathologyName = i < potentialPathologies.size()
                    ? potentialPathologies.get(i)
                    : result.getPathology().getPathologyName();
            String specialist = i < translatedSpecialists.size()
                    ? translatedSpecialists.get(i)
                    : result.getDoctor().getSpecialistLabel();
            pathologyDetails.add(PathologySummaryDTO.builder()
                    .pathologyName(pathologyName)
                    .diseaseScore(result.getDiseaseScore())
                    .description(result.getDescription())
                    .specialist(specialist)
                    .build());
        }

        String recommendedSpecialty = (translations != null && translations.getSpecialists() != null
                && !translations.getSpecialists().isEmpty())
                ? translations.getSpecialists().get(0)
                : getDefaultSpecialty(language);

        ConsultationSummaryDTO summary = new ConsultationSummaryDTO();
        summary.setPatientName(patientName);
        summary.setSymptomsDescription(sessionSymptom.getRawDescription());
        summary.setSymptoms(translatedSymptoms);
        summary.setSymptomsCount(translatedSymptoms.size());
        summary.setHasRedFlags(prediction.getIsRedAlert() != null && prediction.getIsRedAlert());
        summary.setRedFlags(prediction.getIsRedAlert() != null && prediction.getIsRedAlert() ?
                List.of(getRedFlagMessage(language)) : new ArrayList<>());
        summary.setPotentialPathologies(potentialPathologies);
        summary.setPathologyDetails(pathologyDetails);
        summary.setRecommendedSpecialty(recommendedSpecialty);
        summary.setQuestionsForDoctor(generateQuestionsForDoctor(potentialPathologies, language));
        summary.setPdfUrl(prediction.getPdfReportUrl());
        summary.setLanguage(language);
        summary.setGeneratedAt(java.time.LocalDateTime.now().toString());
        applyCheckInDetails(summary, prediction, user, language);
        applyTimeline(summary, prediction, user, language);

        return summary;
    }

    @Override
    public String savePdfAndGetUrl(ByteArrayOutputStream pdfStream, Long predictionId) {
        // This would typically save to a file storage service (S3, Azure Blob, etc.)
        // For now, we'll just return a placeholder URL
        // In production, implement actual file storage
        String url = "/api/v1/diagnocare/predictions/" + predictionId + "/summary.pdf";
        
        // Update prediction with PDF URL
        predictionRepository.findById(predictionId).ifPresent(prediction -> {
            prediction.setPdfReportUrl(url);
            predictionRepository.save(prediction);
        });
        
        return url;
    }

    private String generateHtmlContent(ConsultationSummaryDTO summary) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; margin: 40px; }");
        html.append("h1 { color: #2c3e50; border-bottom: 3px solid #3498db; padding-bottom: 10px; }");
        html.append("h2 { color: #34495e; margin-top: 30px; }");
        html.append(".section { margin: 20px 0; padding: 15px; background-color: #f8f9fa; border-left: 4px solid #3498db; }");
        html.append(".red-alert { background-color: #fee; border-left-color: #e74c3c; }");
        html.append("ul { line-height: 1.8; }");
        html.append("li { margin: 5px 0; }");
        html.append("</style></head><body>");
        
        boolean isEnglish = "en".equals(summary.getLanguage());
        String title = isEnglish ? "Consultation Summary - DiagnoCare" : "Résumé de Consultation - DiagnoCare";
        String patientInfo = isEnglish ? "Patient Information" : "Informations Patient";
        String nameLabel = isEnglish ? "Name" : "Nom";
        String descriptionTitle = isEnglish ? "Symptoms Description" : "Description des Symptômes";
        String declaredSymptoms = isEnglish ? "Declared Symptoms" : "Symptômes Déclarés";
        String redFlagsTitle = isEnglish ? "⚠️ Safety Alerts (Red Flags)" : "⚠️ Alertes de Sécurité (Red Flags)";
        String potentialTitle = isEnglish ? "Potential Pathologies" : "Pathologies Potentielles Envisagées";
        String potentialNote = isEnglish
                ? "Note: This is not a medical diagnosis. Only a healthcare professional can provide a diagnosis."
                : "Note: Ceci n'est pas un diagnostic médical. Seul un professionnel de santé peut établir un diagnostic.";
        String recommendedTitle = isEnglish ? "Recommended Medical Specialty" : "Spécialité Médicale Recommandée";
        String questionsTitle = isEnglish ? "Questions to Ask Your Doctor" : "Questions Pertinentes à Poser au Médecin";
        String detailsTitle = isEnglish ? "Pathology Details" : "Détails des Pathologies";
        String specialistLabel = isEnglish ? "Specialist" : "Spécialiste";
        String scoreLabel = isEnglish ? "Confidence" : "Confiance";
        String checkInTitle = isEnglish ? "Follow-up" : "Suivi";
        String checkInTypeLabel = isEnglish ? "Prediction type" : "Type de prédiction";
        String checkInStatusLabel = isEnglish ? "Follow-up status" : "Statut du suivi";
        String checkInOutcomeLabel = isEnglish ? "Outcome" : "Évolution";
        String previousScoreLabel = isEnglish ? "Previous score" : "Score précédent";
        String currentScoreLabel = isEnglish ? "Current score" : "Score actuel";
        String deltaLabel = isEnglish ? "Score delta" : "Variation du score";
        String worseReasonLabel = isEnglish ? "Worsening reason" : "Raison d'aggravation";
        String checkInCountLabel = isEnglish ? "Total follow-ups" : "Nombre de suivis";
        String timelineTitle = isEnglish ? "Timeline" : "Chronologie";

        html.append("<h1>").append(title).append("</h1>");
        html.append("<div class='section'><h2>").append(patientInfo).append("</h2>");
        html.append("<p><strong>").append(nameLabel).append(":</strong> ").append(summary.getPatientName()).append("</p>");
        if (summary.getGeneratedAt() != null) {
            String generatedLabel = isEnglish ? "Generated at" : "Généré le";
            html.append("<p><strong>").append(generatedLabel).append(":</strong> ")
                .append(summary.getGeneratedAt()).append("</p>");
        }
        if (summary.getSymptomsCount() != null) {
            String countLabel = isEnglish ? "Symptoms count" : "Nombre de symptômes";
            html.append("<p><strong>").append(countLabel).append(":</strong> ")
                .append(summary.getSymptomsCount()).append("</p>");
        }
        html.append("</div>");
        
        html.append("<div class='section'><h2>").append(descriptionTitle).append("</h2>");
        html.append("<p>").append(summary.getSymptomsDescription()).append("</p>");
        html.append("</div>");
        
        if (summary.getSymptoms() != null && !summary.getSymptoms().isEmpty()) {
            html.append("<div class='section'><h2>").append(declaredSymptoms).append("</h2><ul>");
            summary.getSymptoms().forEach(symptom -> 
                html.append("<li>").append(symptom).append("</li>"));
            html.append("</ul></div>");
        }
        
        if (summary.getHasRedFlags()) {
            html.append("<div class='section red-alert'><h2>").append(redFlagsTitle).append("</h2><ul>");
            summary.getRedFlags().forEach(flag -> 
                html.append("<li><strong>").append(flag).append("</strong></li>"));
            html.append("</ul></div>");
        }
        
        if (summary.getPotentialPathologies() != null && !summary.getPotentialPathologies().isEmpty()) {
            html.append("<div class='section'><h2>").append(potentialTitle).append("</h2>");
            html.append("<p><em>").append(potentialNote).append("</em></p><ul>");
            summary.getPotentialPathologies().forEach(pathology -> 
                html.append("<li>").append(pathology).append("</li>"));
            html.append("</ul></div>");
        }

        if (summary.getPathologyDetails() != null && !summary.getPathologyDetails().isEmpty()) {
            html.append("<div class='section'><h2>").append(detailsTitle).append("</h2>");
            summary.getPathologyDetails().forEach(detail -> {
                html.append("<div style='margin-bottom: 12px;'>");
                html.append("<strong>").append(detail.getPathologyName()).append("</strong>");
                if (detail.getDiseaseScore() != null) {
                    html.append(" - ").append(scoreLabel).append(": ")
                        .append(formatScore(detail.getDiseaseScore()));
                }
                if (detail.getSpecialist() != null && !detail.getSpecialist().isBlank()) {
                    html.append("<br/><em>").append(specialistLabel).append(": ")
                        .append(detail.getSpecialist()).append("</em>");
                }
                if (detail.getDescription() != null && !detail.getDescription().isBlank()) {
                    html.append("<br/>").append(detail.getDescription());
                }
                html.append("</div>");
            });
            html.append("</div>");
        }
        
        html.append("<div class='section'><h2>").append(recommendedTitle).append("</h2>");
        html.append("<p><strong>").append(summary.getRecommendedSpecialty()).append("</strong></p>");
        html.append("</div>");
        
        if (summary.getQuestionsForDoctor() != null && !summary.getQuestionsForDoctor().isEmpty()) {
            html.append("<div class='section'><h2>").append(questionsTitle).append("</h2><ul>");
            summary.getQuestionsForDoctor().forEach(question -> 
                html.append("<li>").append(question).append("</li>"));
            html.append("</ul></div>");
        }

        if (summary.getCheckIn() != null) {
            html.append("<div class='section'><h2>").append(checkInTitle).append("</h2>");
            String typeLabel = summary.getCheckIn() ? (isEnglish ? "Follow-up" : "Suivi") : (isEnglish ? "Initial" : "Initial");
            html.append("<p><strong>").append(checkInTypeLabel).append(":</strong> ").append(typeLabel).append("</p>");
            if (summary.getCheckInStatus() != null) {
                html.append("<p><strong>").append(checkInStatusLabel).append(":</strong> ")
                    .append(summary.getCheckInStatus()).append("</p>");
            }
            if (summary.getCheckInCount() != null) {
                html.append("<p><strong>").append(checkInCountLabel).append(":</strong> ")
                    .append(summary.getCheckInCount()).append("</p>");
            }
            if (summary.getCheckInOutcome() != null) {
                html.append("<p><strong>").append(checkInOutcomeLabel).append(":</strong> ")
                    .append(summary.getCheckInOutcome()).append("</p>");
            }
            if (summary.getPreviousBestScore() != null) {
                html.append("<p><strong>").append(previousScoreLabel).append(":</strong> ")
                    .append(formatScore(summary.getPreviousBestScore())).append("</p>");
            }
            if (summary.getCurrentBestScore() != null) {
                html.append("<p><strong>").append(currentScoreLabel).append(":</strong> ")
                    .append(formatScore(summary.getCurrentBestScore())).append("</p>");
            }
            if (summary.getBestScoreDelta() != null) {
                html.append("<p><strong>").append(deltaLabel).append(":</strong> ")
                    .append(formatScore(summary.getBestScoreDelta())).append("</p>");
            }
            if (summary.getWorseReason() != null) {
                html.append("<p><strong>").append(worseReasonLabel).append(":</strong> ")
                    .append(summary.getWorseReason()).append("</p>");
            }
            html.append("</div>");
        }

        if (summary.getTimeline() != null && !summary.getTimeline().isEmpty()) {
            html.append("<div class='section'><h2>").append(timelineTitle).append("</h2>");
            summary.getTimeline().forEach(entry -> {
                html.append("<div style='margin-bottom: 12px;'>");
                html.append("<strong>").append(entry.getType()).append("</strong>");
                if (entry.getDate() != null) {
                    html.append(" - ").append(entry.getDate());
                }
                if (entry.getScore() != null) {
                    html.append(" | ").append(scoreLabel).append(": ")
                        .append(formatScore(entry.getScore()));
                }
                if (entry.getDelta() != null) {
                    html.append(" | ").append(deltaLabel).append(": ")
                        .append(formatScore(entry.getDelta()));
                }
                if (entry.getOutcome() != null) {
                    html.append("<br/>").append(checkInOutcomeLabel).append(": ")
                        .append(entry.getOutcome());
                }
                if (entry.getStatus() != null) {
                    html.append("<br/>").append(checkInStatusLabel).append(": ")
                        .append(entry.getStatus());
                }
                if (entry.getSymptoms() != null && !entry.getSymptoms().isEmpty()) {
                    html.append("<br/>").append(isEnglish ? "Symptoms" : "Symptômes").append(": ")
                        .append(String.join(", ", entry.getSymptoms()));
                }
                html.append("</div>");
            });
            html.append("</div>");
        }
        
        html.append("<div style='margin-top: 40px; padding-top: 20px; border-top: 2px solid #ddd; color: #7f8c8d;'>");
        html.append("<p><small>Document généré par DiagnoCare - ").append(java.time.LocalDateTime.now()).append("</small></p>");
        html.append("</div>");
        
        html.append("</body></html>");
        return html.toString();
    }

    private List<String> generateQuestionsForDoctor(List<String> pathologies, String language) {
        List<String> questions = new ArrayList<>();
        if ("en".equals(language)) {
            questions.add("What is the likely cause of my symptoms?");
            questions.add("What additional tests are needed?");
            questions.add("What treatment do you recommend?");
            questions.add("Are there preventive measures to take?");
            questions.add("When should I return for follow-up?");
        } else {
            questions.add("Quelle est la cause probable de mes symptômes ?");
            questions.add("Quels examens complémentaires sont nécessaires ?");
            questions.add("Quel est le traitement recommandé ?");
            questions.add("Y a-t-il des mesures préventives à prendre ?");
            questions.add("Quand dois-je revenir pour un suivi ?");
        }
        
        if (pathologies != null && !pathologies.isEmpty()) {
            if ("en".equals(language)) {
                questions.add("Are these potential pathologies concerning?");
            } else {
                questions.add("Ces pathologies potentielles sont-elles préoccupantes ?");
            }
        }
        
        return questions;
    }

    private String getDefaultSpecialty(String language) {
        return "en".equals(language) ? "General medicine" : "Médecine générale";
    }

    private String getRedFlagMessage(String language) {
        return "en".equals(language)
                ? "Red flag detected - urgent consultation recommended"
                : "Alerte rouge détectée - Consultation urgente recommandée";
    }

    private String normalizeLanguage(String language) {
        if (language == null || language.trim().isEmpty()) {
            return "fr";
        }
        String normalized = language.trim().toLowerCase();
        return normalized.equals("fr") || normalized.equals("en") ? normalized : "fr";
    }

    private String formatScore(java.math.BigDecimal score) {
        if (score == null) {
            return "";
        }
        double value = score.doubleValue();
        if (value <= 1.0) {
            value = value * 100.0;
        }
        return String.format("%.2f%%", value);
    }

    private void applyCheckInDetails(ConsultationSummaryDTO summary, Prediction prediction, User user, String language) {
        boolean isCheckIn = prediction.getPreviousPrediction() != null;
        summary.setCheckIn(isCheckIn);
        summary.setCurrentBestScore(prediction.getBestScore());

        Prediction previousPrediction = prediction.getPreviousPrediction();
        if (previousPrediction != null) {
            summary.setPreviousPredictionId(previousPrediction.getId());
            summary.setPreviousBestScore(previousPrediction.getBestScore());
            if (previousPrediction.getBestScore() != null && prediction.getBestScore() != null) {
                summary.setBestScoreDelta(prediction.getBestScore().subtract(previousPrediction.getBestScore()));
            }
        }

        Long lookupPredictionId = isCheckIn ? previousPrediction.getId() : prediction.getId();
        CheckIn checkIn = checkInRepository.findByPreviousPredictionIdAndUserId(lookupPredictionId, user.getId())
                .orElse(null);
        if (checkIn != null) {
            summary.setCheckInStatus(localizeCheckInStatus(checkIn.getStatus(), language));
            summary.setCheckInOutcome(localizeCheckInOutcome(checkIn.getOutcome(), language));
            summary.setWorseReason(checkIn.getWorseReason());
        }

        int checkInCount = checkInRepository.findByUserId(user.getId()).size();
        summary.setCheckInCount(checkInCount);
    }

    private void applyTimeline(ConsultationSummaryDTO summary, Prediction prediction, User user, String language) {
        List<Prediction> predictions = predictionRepository.findBySessionSymptomUserId(user.getId());
        if (predictions == null || predictions.isEmpty() || prediction == null) {
            summary.setTimeline(new ArrayList<>());
            return;
        }
        Prediction rootPrediction = resolveRootPrediction(prediction);
        List<CheckIn> checkIns = checkInRepository.findByUserId(user.getId());

        List<Prediction> scopedPredictions = predictions.stream()
                .filter(candidate -> resolveRootPrediction(candidate).getId().equals(rootPrediction.getId()))
                .collect(Collectors.toList());

        List<TimelineEntryDTO> entries = scopedPredictions.stream()
                .sorted((a, b) -> {
                    if (a.getCreatedDate() == null && b.getCreatedDate() == null) {
                        return 0;
                    }
                    if (a.getCreatedDate() == null) {
                        return 1;
                    }
                    if (b.getCreatedDate() == null) {
                        return -1;
                    }
                    return b.getCreatedDate().compareTo(a.getCreatedDate());
                })
                .map((Prediction pred) -> {
                    boolean isFollowUp = pred.getPreviousPrediction() != null;
                    CheckIn related = isFollowUp
                            ? checkIns.stream()
                                .filter(ci -> ci.getPreviousPrediction() != null
                                        && pred.getPreviousPrediction() != null
                                        && ci.getPreviousPrediction().getId().equals(pred.getPreviousPrediction().getId()))
                                .findFirst()
                                .orElse(null)
                            : checkIns.stream()
                                .filter(ci -> ci.getPreviousPrediction() != null
                                        && ci.getPreviousPrediction().getId().equals(pred.getId()))
                                .findFirst()
                                .orElse(null);

                    String type = isFollowUp ? ("en".equals(language) ? "Follow-up" : "Suivi")
                            : ("en".equals(language) ? "Initial" : "Initial");
                    String date = pred.getCreatedDate() != null ? pred.getCreatedDate().toString() : null;
                    List<String> symptoms = pred.getSessionSymptom() != null && pred.getSessionSymptom().getSymptoms() != null
                            ? pred.getSessionSymptom().getSymptoms().stream()
                                .map(symptom -> symptom.getLabel())
                                .collect(Collectors.toList())
                            : new ArrayList<>();
                    if (!"en".equals(language) && !symptoms.isEmpty()) {
                        MLTranslationResponseDTO symptomTranslations = translationService.translate(
                                MLTranslationRequestDTO.builder()
                                        .language(language)
                                        .symptoms(symptoms)
                                        .diseases(new ArrayList<>())
                                        .specialists(new ArrayList<>())
                                        .build()
                        );
                        if (symptomTranslations != null && symptomTranslations.getSymptoms() != null) {
                            symptoms = symptomTranslations.getSymptoms();
                        }
                    }

                    BigDecimal delta = null;
                    if (pred.getPreviousPrediction() != null
                            && pred.getPreviousPrediction().getBestScore() != null
                            && pred.getBestScore() != null) {
                        delta = pred.getBestScore().subtract(pred.getPreviousPrediction().getBestScore());
                    }

                    return TimelineEntryDTO.builder()
                            .predictionId(pred.getId())
                            .type(type)
                            .date(date)
                            .symptoms(symptoms)
                            .score(pred.getBestScore())
                            .delta(delta)
                            .outcome(related != null ? localizeCheckInOutcome(related.getOutcome(), language) : null)
                            .status(related != null ? localizeCheckInStatus(related.getStatus(), language) : null)
                            .build();
                })
                .collect(Collectors.toList());

        summary.setTimeline(entries);
    }

    private Prediction resolveRootPrediction(Prediction prediction) {
        Prediction current = prediction;
        while (current != null && current.getPreviousPrediction() != null) {
            current = current.getPreviousPrediction();
        }
        return current != null ? current : prediction;
    }

    private String localizeCheckInStatus(CheckInStatus status, String language) {
        if (status == null) {
            return null;
        }
        if ("en".equals(language)) {
            return switch (status) {
                case PENDING -> "Pending";
                case SENT_24H -> "Sent (24h)";
                case SENT_48H -> "Sent (48h)";
                case COMPLETED -> "Completed";
            };
        }
        return switch (status) {
            case PENDING -> "En attente";
            case SENT_24H -> "Envoyé (24h)";
            case SENT_48H -> "Envoyé (48h)";
            case COMPLETED -> "Terminé";
        };
    }

    private String localizeCheckInOutcome(CheckInOutcome outcome, String language) {
        if (outcome == null) {
            return null;
        }
        if ("en".equals(language)) {
            return switch (outcome) {
                case IMPROVING -> "Improving";
                case STABLE -> "Stable";
                case WORSENING -> "Worsening";
            };
        }
        return switch (outcome) {
            case IMPROVING -> "Amélioration";
            case STABLE -> "Stable";
            case WORSENING -> "Aggravation";
        };
    }
}
