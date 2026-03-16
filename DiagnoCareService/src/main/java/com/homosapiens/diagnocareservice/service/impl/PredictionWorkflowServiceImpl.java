package com.homosapiens.diagnocareservice.service.impl;

import com.homosapiens.diagnocareservice.core.exception.AppException;
import com.homosapiens.diagnocareservice.dto.*;
import com.homosapiens.diagnocareservice.model.entity.*;
import com.homosapiens.diagnocareservice.repository.SymptomRepository;
import com.homosapiens.diagnocareservice.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PredictionWorkflowServiceImpl implements PredictionWorkflowService {

    private final PredictionService predictionService;
    private final SessionSymptomService sessionSymptomService;
    private final MLPredictionClient mlPredictionClient;
    private final PatientMedicalProfileService patientMedicalProfileService;
    private final PathologyService pathologyService;
    private final DoctorService doctorService;
    private final PathologyResultService pathologyResultService;
    private final UserService userService;
    private final SymptomRepository symptomRepository;
    private final UrgentDiseaseService urgentDiseaseService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PredictionCreationResult createPrediction(SessionSymptomRequestDTO requestDTO, Long previousPredictionId) {
        log.info("Creating prediction for session symptom request: userId={}", requestDTO.getUserId());

        String language = resolveUserLanguage(requestDTO.getUserId(), null);

        List<String> extractedSymptoms = resolveSymptomsForPrediction(requestDTO);
        if (extractedSymptoms.isEmpty()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Symptoms are required");
        }
        log.info("Using {} symptoms for prediction (language: {})", extractedSymptoms.size(), language);

        Optional<PatientMedicalProfile> profileOpt = patientMedicalProfileService.getProfileByUserId(requestDTO.getUserId());

        MLPredictionRequestDTO mlRequest = buildMLRequest(extractedSymptoms, profileOpt, language);

        MLPredictionResponseDTO mlResponse = mlPredictionClient.predict(mlRequest);
        log.info("ML service returned {} predictions", mlResponse.getPredictions().size());

        boolean isRedAlert = determineRedAlert(mlResponse);
        BigDecimal bestScore = calculateBestScore(mlResponse);

        SessionSymptom sessionSymptom = sessionSymptomService.createSessionSymptom(requestDTO);

        PredictionRequestDTO predictionRequest = new PredictionRequestDTO();
        predictionRequest.setSessionSymptomId(sessionSymptom.getId());
        predictionRequest.setBestScore(bestScore);
        predictionRequest.setIsRedAlert(isRedAlert);
        predictionRequest.setComment("AI prediction based on symptoms and patient profile");
        predictionRequest.setPreviousPredictionId(previousPredictionId);

        Prediction created = predictionService.createPrediction(predictionRequest);

        createPathologyResults(created, mlResponse, language);

        return PredictionCreationResult.builder()
                .prediction(created)
                .mlResponse(mlResponse)
                .build();
    }

    private MLPredictionRequestDTO buildMLRequest(List<String> symptoms, Optional<PatientMedicalProfile> profileOpt, String language) {
        MLPredictionRequestDTO.MLPredictionRequestDTOBuilder builder = MLPredictionRequestDTO.builder()
                .symptoms(symptoms)
                .language(language);

        if (profileOpt.isPresent()) {
            PatientMedicalProfile profile = profileOpt.get();
            builder.age(profile.getAge())
                   .weight(profile.getWeight())
                   .bmi((float) profile.getBmi())
                   .tension_moyenne(profile.getMeanBloodPressure())
                   .cholesterole_moyen(profile.getMeanCholesterol())
                   .gender(profile.getGender() != null ? profile.getGender().name() : "Male")
                   .smoking(profile.getIsSmoking() != null && profile.getIsSmoking() ? "Yes" : "No")
                   .alcohol(profile.getAlcohol() != null && profile.getAlcohol() ? "Moderate" : "None")
                   .sedentarite(profile.getSedentary() != null && profile.getSedentary() ? "High" : "Moderate")
                   .family_history(profile.getFamilyAntecedents() != null && !profile.getFamilyAntecedents().isEmpty() ? "Yes" : "No");

            if (profile.getMeanBloodPressure() != null) {
                if (profile.getMeanBloodPressure() > 140) {
                    builder.blood_pressure("High");
                } else if (profile.getMeanBloodPressure() < 90) {
                    builder.blood_pressure("Low");
                } else {
                    builder.blood_pressure("Normal");
                }
            } else {
                builder.blood_pressure("Normal");
            }

            if (profile.getMeanCholesterol() != null) {
                if (profile.getMeanCholesterol() > 240) {
                    builder.cholesterol_level("High");
                } else if (profile.getMeanCholesterol() < 200) {
                    builder.cholesterol_level("Low");
                } else {
                    builder.cholesterol_level("Normal");
                }
            } else {
                builder.cholesterol_level("Normal");
            }
        } else {
            builder.age(35)
                   .weight(75f)
                   .bmi(24.2f)
                   .tension_moyenne(120f)
                   .cholesterole_moyen(190f)
                   .gender("Male")
                   .blood_pressure("Normal")
                   .cholesterol_level("Normal")
                   .smoking("No")
                   .alcohol("None")
                   .sedentarite("Moderate")
                   .family_history("No");
        }

        builder.outcome_variable("Negative");

        return builder.build();
    }

    private String resolveUserLanguage(Long userId, String fallbackLanguage) {
        String language = null;
        if (userId != null) {
            language = userService.getUserById(userId)
                    .map(User::getLang)
                    .orElse(null);
        }
        if (language == null || language.trim().isEmpty()) {
            language = fallbackLanguage;
        }
        return normalizeLanguage(language);
    }

    private List<String> resolveSymptomsForPrediction(SessionSymptomRequestDTO requestDTO) {
        if (requestDTO.getSymptomIds() != null && !requestDTO.getSymptomIds().isEmpty()) {
            List<Symptom> symptoms = symptomRepository.findAllById(requestDTO.getSymptomIds());
            if (symptoms.size() != requestDTO.getSymptomIds().size()) {
                throw new AppException(HttpStatus.NOT_FOUND, "One or more symptoms not found");
            }
            return symptoms.stream()
                    .map(Symptom::getLabel)
                    .toList();
        }

        if (requestDTO.getSymptomLabels() != null && !requestDTO.getSymptomLabels().isEmpty()) {
            return requestDTO.getSymptomLabels();
        }

        throw new AppException(HttpStatus.BAD_REQUEST, "Symptoms are required");
    }

    private String normalizeLanguage(String language) {
        if (language == null || language.trim().isEmpty()) {
            return "fr";
        }
        String normalized = language.trim().toLowerCase();
        return normalized.equals("fr") || normalized.equals("en") ? normalized : "fr";
    }

    private boolean determineRedAlert(MLPredictionResponseDTO mlResponse) {
        if (mlResponse.getPredictions() != null && !mlResponse.getPredictions().isEmpty()) {
            MLPredictionResponseDTO.PredictionResult topResult = mlResponse.getPredictions().get(0);
            return urgentDiseaseService.isUrgentDisease(topResult.getDisease());
        }
        return false;
    }

    private BigDecimal calculateBestScore(MLPredictionResponseDTO mlResponse) {
        if (mlResponse.getPredictions() == null || mlResponse.getPredictions().isEmpty()) {
            return BigDecimal.ZERO;
        }
        Double bestProb = mlResponse.getPredictions().get(0).getProbability();
        return BigDecimal.valueOf(bestProb != null ? bestProb : 0.0);
    }

    private void createPathologyResults(Prediction prediction, MLPredictionResponseDTO mlResponse, String language) {
        if (mlResponse.getPredictions() == null || mlResponse.getPredictions().isEmpty()) {
            return;
        }

        mlResponse.getPredictions().stream()
                .limit(3)
                .forEach(result -> {
                    try {
                        String diseaseName = "fr".equals(language) && result.getDisease_fr() != null
                                ? result.getDisease_fr()
                                : result.getDisease();

                        String specialistName = "fr".equals(language) && result.getSpecialist_fr() != null
                                ? result.getSpecialist_fr()
                                : result.getSpecialist();

                        Pathology pathology = pathologyService.getPathologyByName(result.getDisease())
                                .orElseGet(() -> {
                                    Pathology newPathology = new Pathology();
                                    newPathology.setPathologyName(result.getDisease());
                                    newPathology.setDescription("AI predicted pathology");
                                    return pathologyService.createPathology(newPathology);
                                });

                        Doctor doctor = doctorService.getDoctorBySpecialistLabel(result.getSpecialist())
                                .orElseGet(() -> {
                                    Doctor newDoctor = new Doctor();
                                    newDoctor.setSpecialistLabel(result.getSpecialist());
                                    newDoctor.setSpecialistScore(BigDecimal.valueOf(result.getSpecialist_probability() != null ? result.getSpecialist_probability() : 0.0));
                                    newDoctor.setDescription("AI recommended specialist");
                                    return doctorService.createDoctor(newDoctor);
                                });

                        PathologyResultRequestDTO pathologyResultRequest = new PathologyResultRequestDTO();
                        pathologyResultRequest.setPredictionId(prediction.getId().toString());
                        pathologyResultRequest.setPathologyId(pathology.getId());
                        pathologyResultRequest.setDoctorId(doctor.getId());
                        pathologyResultRequest.setDiseaseScore(BigDecimal.valueOf(result.getProbability() != null ? result.getProbability() : 0.0));
                        pathologyResultRequest.setLocalizedDiseaseName(diseaseName);
                        pathologyResultRequest.setLocalizedSpecialistLabel(specialistName);

                        String description = result.getDescription();
                        if (description == null || description.isEmpty()) {
                            description = String.format("Predicted disease: %s (%.2f%%) - Recommended specialist: %s",
                                    diseaseName, result.getProbability(), specialistName);
                        }
                        pathologyResultRequest.setDescription(description);

                        pathologyResultService.createPathologyResult(prediction.getId(), pathologyResultRequest);

                    } catch (Exception e) {
                        log.error("Error creating pathology result for disease: {}", result.getDisease(), e);
                    }
                });
    }
}
