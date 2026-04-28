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

    private String formatName(String raw) {
        if (raw == null || raw.isBlank()) return "";
        return java.util.Arrays.stream(raw.replace("_", " ").trim().split("\\s+"))
                .map(w -> w.isEmpty() ? "" : Character.toUpperCase(w.charAt(0)) + w.substring(1).toLowerCase())
                .collect(java.util.stream.Collectors.joining(" "));
    }

    private String formatDateTime(String isoDateTime) {
        if (isoDateTime == null) return "";
        try {
            java.time.LocalDateTime dt = java.time.LocalDateTime.parse(isoDateTime);
            return dt.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        } catch (Exception e) {
            return isoDateTime.contains("T") ? isoDateTime.replace("T", " ").substring(0, Math.min(16, isoDateTime.length())) : isoDateTime;
        }
    }

    private String generateHtmlContent(ConsultationSummaryDTO summary) {
        boolean isEnglish = "en".equals(summary.getLanguage());

        // --- Labels ---
        String title        = isEnglish ? "Consultation Summary – DiagnoCare" : "Résumé de Consultation – DiagnoCare";
        String patientInfo  = isEnglish ? "Patient Information" : "Informations Patient";
        String nameLabel    = isEnglish ? "Name" : "Nom";
        String generatedLabel = isEnglish ? "Generated" : "Généré le";
        String countLabel   = isEnglish ? "Symptoms" : "Symptômes";
        String descTitle    = isEnglish ? "Symptoms Description" : "Description des Symptômes";
        String sympTitle    = isEnglish ? "Declared Symptoms" : "Symptômes Déclarés";
        String redTitle     = isEnglish ? "⚠ Safety Alerts" : "⚠ Alertes de Sécurité";
        String pathTitle    = isEnglish ? "Potential Pathologies" : "Pathologies Potentielles";
        String potentialNote = isEnglish
                ? "Not a medical diagnosis – consult a healthcare professional."
                : "Ceci n'est pas un diagnostic médical – consultez un professionnel de santé.";
        String detailsTitle = isEnglish ? "Pathology Details" : "Détails des Pathologies";
        String specialistCol = isEnglish ? "Specialist" : "Spécialiste";
        String scoreCol     = isEnglish ? "Confidence" : "Confiance";
        String recommendedTitle = isEnglish ? "Recommended Specialty" : "Spécialité Recommandée";
        String questionsTitle   = isEnglish ? "Questions for Your Doctor" : "Questions pour Votre Médecin";
        String checkInTitle     = isEnglish ? "Follow-up Information" : "Informations de Suivi";
        String checkInTypeLabel = isEnglish ? "Type" : "Type";
        String checkInStatusLabel  = isEnglish ? "Status" : "Statut";
        String checkInOutcomeLabel = isEnglish ? "Outcome" : "Évolution";
        String prevScoreLabel   = isEnglish ? "Previous score" : "Score précédent";
        String currScoreLabel   = isEnglish ? "Current score" : "Score actuel";
        String deltaLabel       = isEnglish ? "Delta" : "Variation";
        String worseLabel       = isEnglish ? "Worsening reason" : "Raison d'aggravation";
        String countCheckinLabel = isEnglish ? "Total follow-ups" : "Nombre de suivis";
        String timelineTitle    = isEnglish ? "Timeline" : "Chronologie";
        String dateCol          = isEnglish ? "Date" : "Date";
        String sympCol          = isEnglish ? "Symptoms" : "Symptômes";
        String outcomeCol       = isEnglish ? "Outcome" : "Évolution";

        // --- Name formatting ---
        String patientName = formatName(summary.getPatientName());

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'><style>");
        // Compact, professional CSS
        html.append("* { box-sizing: border-box; margin: 0; padding: 0; }");
        html.append("body { font-family: Arial, Helvetica, sans-serif; font-size: 11px; color: #222; background: #fff; padding: 18px 22px; }");
        html.append("h1 { font-size: 16px; color: #1a5276; border-bottom: 2px solid #1a5276; padding-bottom: 5px; margin-bottom: 10px; }");
        html.append("h2 { font-size: 12px; color: #1a5276; margin-bottom: 5px; text-transform: uppercase; letter-spacing: 0.5px; }");
        html.append(".sec { margin-bottom: 10px; border: 1px solid #d5e8f3; border-left: 3px solid #1a5276; border-radius: 2px; padding: 8px 10px; background: #f7fbfe; }");
        html.append(".sec.alert { border-left-color: #c0392b; background: #fdf3f2; }");
        html.append(".grid2 { display: table; width: 100%; }");
        html.append(".col { display: table-cell; width: 50%; vertical-align: top; padding-right: 8px; }");
        html.append(".row { margin-bottom: 3px; }");
        html.append(".lbl { font-weight: bold; color: #444; }");
        html.append(".pills { margin-top: 4px; }");
        html.append(".pill { display: inline-block; background: #d6eaf8; border-radius: 10px; padding: 1px 7px; margin: 2px 2px 2px 0; font-size: 10px; }");
        html.append("table { width: 100%; border-collapse: collapse; font-size: 10.5px; margin-top: 4px; }");
        html.append("th { background: #1a5276; color: #fff; text-align: left; padding: 4px 6px; font-size: 10px; }");
        html.append("td { padding: 3px 6px; border-bottom: 1px solid #e0e0e0; vertical-align: top; }");
        html.append("tr:nth-child(even) td { background: #f0f8ff; }");
        html.append(".footer { margin-top: 10px; padding-top: 6px; border-top: 1px solid #ccc; color: #888; font-size: 9px; }");
        html.append(".note { font-style: italic; color: #666; font-size: 10px; margin-bottom: 3px; }");
        html.append(".recommended { font-size: 13px; font-weight: bold; color: #1a5276; }");
        html.append("ul { margin: 3px 0 0 14px; padding: 0; }");
        html.append("li { margin-bottom: 2px; }");
        html.append("</style></head><body>");

        // Title
        html.append("<h1>").append(title).append("</h1>");

        // Patient info – compact two-col grid
        html.append("<div class='sec'><h2>").append(patientInfo).append("</h2><div class='grid2'>");
        html.append("<div class='col'>");
        html.append("<div class='row'><span class='lbl'>").append(nameLabel).append(":</span> ").append(patientName).append("</div>");
        html.append("</div>");
        html.append("<div class='col'>");
        if (summary.getGeneratedAt() != null) {
            html.append("<div class='row'><span class='lbl'>").append(generatedLabel).append(":</span> ").append(formatDateTime(summary.getGeneratedAt())).append("</div>");
        }
        if (summary.getSymptomsCount() != null) {
            html.append("<div class='row'><span class='lbl'>").append(countLabel).append(":</span> ").append(summary.getSymptomsCount()).append("</div>");
        }
        html.append("</div></div></div>");

        // Red flags
        if (summary.getHasRedFlags() && summary.getRedFlags() != null && !summary.getRedFlags().isEmpty()) {
            html.append("<div class='sec alert'><h2>").append(redTitle).append("</h2><ul>");
            summary.getRedFlags().forEach(f -> html.append("<li><strong>").append(f).append("</strong></li>"));
            html.append("</ul></div>");
        }

        // Symptoms description (only if non-empty)
        if (summary.getSymptomsDescription() != null && !summary.getSymptomsDescription().isBlank()) {
            html.append("<div class='sec'><h2>").append(descTitle).append("</h2>");
            html.append("<p style='margin-top:3px;'>").append(summary.getSymptomsDescription()).append("</p></div>");
        }

        // Declared symptoms as pills
        if (summary.getSymptoms() != null && !summary.getSymptoms().isEmpty()) {
            html.append("<div class='sec'><h2>").append(sympTitle).append("</h2><div class='pills'>");
            summary.getSymptoms().forEach(s -> html.append("<span class='pill'>").append(s).append("</span>"));
            html.append("</div></div>");
        }

        // Pathology details as table
        if (summary.getPathologyDetails() != null && !summary.getPathologyDetails().isEmpty()) {
            html.append("<div class='sec'><h2>").append(detailsTitle).append("</h2>");
            html.append("<p class='note'>").append(potentialNote).append("</p>");
            html.append("<table><thead><tr><th>").append(pathTitle).append("</th><th>").append(scoreCol).append("</th><th>").append(specialistCol).append("</th></tr></thead><tbody>");
            summary.getPathologyDetails().forEach(d -> {
                html.append("<tr><td>").append(d.getPathologyName()).append("</td>");
                html.append("<td>").append(d.getDiseaseScore() != null ? formatScore(d.getDiseaseScore()) : "–").append("</td>");
                html.append("<td>").append(d.getSpecialist() != null ? d.getSpecialist() : "–").append("</td></tr>");
                if (d.getDescription() != null && !d.getDescription().isBlank()) {
                    html.append("<tr><td colspan='3' style='color:#555;font-style:italic;font-size:10px;padding-bottom:5px;'>").append(d.getDescription()).append("</td></tr>");
                }
            });
            html.append("</tbody></table></div>");
        }

        // Recommended specialty
        html.append("<div class='sec'><h2>").append(recommendedTitle).append("</h2>");
        html.append("<p class='recommended'>").append(summary.getRecommendedSpecialty()).append("</p></div>");

        // Questions for doctor
        if (summary.getQuestionsForDoctor() != null && !summary.getQuestionsForDoctor().isEmpty()) {
            html.append("<div class='sec'><h2>").append(questionsTitle).append("</h2><ul>");
            summary.getQuestionsForDoctor().forEach(q -> html.append("<li>").append(q).append("</li>"));
            html.append("</ul></div>");
        }

        // Follow-up info
        if (summary.getCheckIn() != null) {
            html.append("<div class='sec'><h2>").append(checkInTitle).append("</h2><div class='grid2'><div class='col'>");
            String typeVal = summary.getCheckIn() ? (isEnglish ? "Follow-up" : "Suivi") : (isEnglish ? "Initial" : "Initial");
            html.append("<div class='row'><span class='lbl'>").append(checkInTypeLabel).append(":</span> ").append(typeVal).append("</div>");
            if (summary.getCheckInStatus()  != null) html.append("<div class='row'><span class='lbl'>").append(checkInStatusLabel).append(":</span> ").append(summary.getCheckInStatus()).append("</div>");
            if (summary.getCheckInOutcome() != null) html.append("<div class='row'><span class='lbl'>").append(checkInOutcomeLabel).append(":</span> ").append(summary.getCheckInOutcome()).append("</div>");
            if (summary.getCheckInCount()   != null) html.append("<div class='row'><span class='lbl'>").append(countCheckinLabel).append(":</span> ").append(summary.getCheckInCount()).append("</div>");
            html.append("</div><div class='col'>");
            if (summary.getPreviousBestScore() != null) html.append("<div class='row'><span class='lbl'>").append(prevScoreLabel).append(":</span> ").append(formatScore(summary.getPreviousBestScore())).append("</div>");
            if (summary.getCurrentBestScore()  != null) html.append("<div class='row'><span class='lbl'>").append(currScoreLabel).append(":</span> ").append(formatScore(summary.getCurrentBestScore())).append("</div>");
            if (summary.getBestScoreDelta()    != null) html.append("<div class='row'><span class='lbl'>").append(deltaLabel).append(":</span> ").append(formatScore(summary.getBestScoreDelta())).append("</div>");
            if (summary.getWorseReason()       != null) html.append("<div class='row'><span class='lbl'>").append(worseLabel).append(":</span> ").append(summary.getWorseReason()).append("</div>");
            html.append("</div></div></div>");
        }

        // Timeline as table
        if (summary.getTimeline() != null && !summary.getTimeline().isEmpty()) {
            html.append("<div class='sec'><h2>").append(timelineTitle).append("</h2>");
            html.append("<table><thead><tr><th>").append(checkInTypeLabel).append("</th><th>").append(dateCol).append("</th><th>").append(scoreCol).append("</th><th>").append(deltaLabel).append("</th><th>").append(outcomeCol).append("</th><th>").append(sympCol).append("</th></tr></thead><tbody>");
            summary.getTimeline().forEach(e -> {
                html.append("<tr>");
                html.append("<td>").append(e.getType() != null ? e.getType() : "–").append("</td>");
                html.append("<td>").append(e.getDate() != null ? formatDateTime(e.getDate()) : "–").append("</td>");
                html.append("<td>").append(e.getScore() != null ? formatScore(e.getScore()) : "–").append("</td>");
                html.append("<td>").append(e.getDelta() != null ? formatScore(e.getDelta()) : "–").append("</td>");
                html.append("<td>").append(e.getOutcome() != null ? e.getOutcome() : "–").append("</td>");
                html.append("<td>").append(e.getSymptoms() != null && !e.getSymptoms().isEmpty() ? String.join(", ", e.getSymptoms()) : "–").append("</td>");
                html.append("</tr>");
            });
            html.append("</tbody></table></div>");
        }

        // Footer
        html.append("<div class='footer'>Document généré par DiagnoCare – ")
            .append(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
            .append("</div>");

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
