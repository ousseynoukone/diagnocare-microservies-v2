package com.homosapiens.diagnocareservice.service.impl;

import com.homosapiens.diagnocareservice.core.exception.AppException;
import com.homosapiens.diagnocareservice.dto.ConsultationSummaryDTO;
import com.homosapiens.diagnocareservice.dto.PathologyResultDTO;
import com.homosapiens.diagnocareservice.model.entity.SessionSymptom;
import com.homosapiens.diagnocareservice.model.entity.User;
import com.homosapiens.diagnocareservice.model.entity.Prediction;
import com.homosapiens.diagnocareservice.repository.PredictionRepository;
import com.homosapiens.diagnocareservice.repository.SessionSymptomRepository;
import com.homosapiens.diagnocareservice.service.ConsultationSummaryService;
import com.homosapiens.diagnocareservice.service.PathologyResultService;
import com.itextpdf.html2pdf.HtmlConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConsultationSummaryServiceImpl implements ConsultationSummaryService {

    private final PredictionRepository predictionRepository;
    private final SessionSymptomRepository sessionSymptomRepository;
    private final PathologyResultService pathologyResultService;

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
        String patientName = user.getFirstName() + " " + user.getLastName();

        List<String> symptoms = sessionSymptom.getSymptoms() != null ?
                sessionSymptom.getSymptoms().stream()
                        .map(symptom -> symptom.getLabel())
                        .collect(Collectors.toList()) : new ArrayList<>();

        List<com.homosapiens.diagnocareservice.model.entity.PathologyResult> pathologyResults = pathologyResultService
                .getPathologyResultsByPredictionId(predictionId);

        List<PathologyResultDTO> pathologyResultDTOs = pathologyResultService
                .convertToDTOList(pathologyResults);

        List<String> potentialPathologies = pathologyResultDTOs.stream()
                .map(pr -> pr.getPathologyName())
                .collect(Collectors.toList());

        String recommendedSpecialty = pathologyResultDTOs.stream()
                .findFirst()
                .map(pr -> pr.getDoctorSpecialistLabel())
                .orElse("Médecine générale");

        ConsultationSummaryDTO summary = new ConsultationSummaryDTO();
        summary.setPatientName(patientName);
        summary.setSymptomsDescription(sessionSymptom.getRawDescription());
        summary.setSymptoms(symptoms);
        summary.setHasRedFlags(prediction.getIsRedAlert() != null && prediction.getIsRedAlert());
        summary.setRedFlags(prediction.getIsRedAlert() != null && prediction.getIsRedAlert() ? 
                List.of("Alerte rouge détectée - Consultation urgente recommandée") : new ArrayList<>());
        summary.setPotentialPathologies(potentialPathologies);
        summary.setRecommendedSpecialty(recommendedSpecialty);
        summary.setQuestionsForDoctor(generateQuestionsForDoctor(potentialPathologies));
        summary.setPdfUrl(prediction.getPdfReportUrl());

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
        
        html.append("<h1>Résumé de Consultation - DiagnoCare</h1>");
        html.append("<div class='section'><h2>Informations Patient</h2>");
        html.append("<p><strong>Nom:</strong> ").append(summary.getPatientName()).append("</p>");
        html.append("</div>");
        
        html.append("<div class='section'><h2>Description des Symptômes</h2>");
        html.append("<p>").append(summary.getSymptomsDescription()).append("</p>");
        html.append("</div>");
        
        if (summary.getSymptoms() != null && !summary.getSymptoms().isEmpty()) {
            html.append("<div class='section'><h2>Symptômes Déclarés</h2><ul>");
            summary.getSymptoms().forEach(symptom -> 
                html.append("<li>").append(symptom).append("</li>"));
            html.append("</ul></div>");
        }
        
        if (summary.getHasRedFlags()) {
            html.append("<div class='section red-alert'><h2>⚠️ Alertes de Sécurité (Red Flags)</h2><ul>");
            summary.getRedFlags().forEach(flag -> 
                html.append("<li><strong>").append(flag).append("</strong></li>"));
            html.append("</ul></div>");
        }
        
        if (summary.getPotentialPathologies() != null && !summary.getPotentialPathologies().isEmpty()) {
            html.append("<div class='section'><h2>Pathologies Potentielles Envisagées</h2>");
            html.append("<p><em>Note: Ceci n'est pas un diagnostic médical. Seul un professionnel de santé peut établir un diagnostic.</em></p><ul>");
            summary.getPotentialPathologies().forEach(pathology -> 
                html.append("<li>").append(pathology).append("</li>"));
            html.append("</ul></div>");
        }
        
        html.append("<div class='section'><h2>Spécialité Médicale Recommandée</h2>");
        html.append("<p><strong>").append(summary.getRecommendedSpecialty()).append("</strong></p>");
        html.append("</div>");
        
        if (summary.getQuestionsForDoctor() != null && !summary.getQuestionsForDoctor().isEmpty()) {
            html.append("<div class='section'><h2>Questions Pertinentes à Poser au Médecin</h2><ul>");
            summary.getQuestionsForDoctor().forEach(question -> 
                html.append("<li>").append(question).append("</li>"));
            html.append("</ul></div>");
        }
        
        html.append("<div style='margin-top: 40px; padding-top: 20px; border-top: 2px solid #ddd; color: #7f8c8d;'>");
        html.append("<p><small>Document généré par DiagnoCare - ").append(java.time.LocalDateTime.now()).append("</small></p>");
        html.append("</div>");
        
        html.append("</body></html>");
        return html.toString();
    }

    private List<String> generateQuestionsForDoctor(List<String> pathologies) {
        List<String> questions = new ArrayList<>();
        questions.add("Quelle est la cause probable de mes symptômes ?");
        questions.add("Quels examens complémentaires sont nécessaires ?");
        questions.add("Quel est le traitement recommandé ?");
        questions.add("Y a-t-il des mesures préventives à prendre ?");
        questions.add("Quand dois-je revenir pour un suivi ?");
        
        if (pathologies != null && !pathologies.isEmpty()) {
            questions.add("Ces pathologies potentielles sont-elles préoccupantes ?");
        }
        
        return questions;
    }
}
