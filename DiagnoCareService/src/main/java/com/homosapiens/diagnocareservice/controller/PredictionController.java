package com.homosapiens.diagnocareservice.controller;

import com.homosapiens.diagnocareservice.dto.*;
import com.homosapiens.diagnocareservice.model.entity.*;
import com.homosapiens.diagnocareservice.model.entity.enums.GenderEnum;
import com.homosapiens.diagnocareservice.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("predictions")
@Tag(name = "Prediction Management", description = "APIs for managing AI predictions")
@RequiredArgsConstructor
@Slf4j
public class PredictionController {

    private final PredictionService predictionService;
    private final SessionSymptomService sessionSymptomService;
    private final MLPredictionClient mlPredictionClient;
    private final PatientMedicalProfileService patientMedicalProfileService;
    private final PathologyService pathologyService;
    private final DoctorService doctorService;
    private final PathologyResultService pathologyResultService;
    private final UserService userService;

    @PostMapping
    @Operation(summary = "Create a new prediction", description = "Creates a new AI prediction based on symptom session")
    public ResponseEntity<PredictionDTO> makePrediction(
            @Valid @RequestBody SessionSymptomRequestDTO sessionSymptomRequestDTO) {
        
        try {
            log.info("Creating prediction for session symptom request: userId={}", sessionSymptomRequestDTO.getUserId());
            
            // Create or get the session symptom
            SessionSymptom sessionSymptom = sessionSymptomService.createSessionSymptom(sessionSymptomRequestDTO);
            
            // Resolve language from user profile (default: "fr")
            String language = resolveUserLanguage(
                    sessionSymptomRequestDTO.getUserId(),
                    null
            );
            
            // Use explicit symptoms (English) for prediction
            List<String> extractedSymptoms = sessionSymptom.getSymptoms() != null
                    ? sessionSymptom.getSymptoms().stream()
                        .map(Symptom::getLabel)
                        .toList()
                    : List.of();
            if (extractedSymptoms.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            log.info("Using {} symptoms for prediction (language: {})", extractedSymptoms.size(), language);
            
            // Get patient medical profile
            Optional<PatientMedicalProfile> profileOpt = patientMedicalProfileService.getProfileByUserId(sessionSymptomRequestDTO.getUserId());
            
            // Build ML prediction request
            MLPredictionRequestDTO mlRequest = buildMLRequest(extractedSymptoms, profileOpt, language);
            
            // Call ML service
            MLPredictionResponseDTO mlResponse = mlPredictionClient.predict(mlRequest);
            log.info("ML service returned {} predictions", mlResponse.getPredictions().size());
            
            // Determine if red alert (highest probability disease might be critical)
            boolean isRedAlert = determineRedAlert(mlResponse);
            
            // Calculate global score (average of top prediction probabilities)
            BigDecimal globalScore = calculateGlobalScore(mlResponse);
            
            // Create prediction entity
            PredictionRequestDTO predictionRequest = new PredictionRequestDTO();
            predictionRequest.setSessionSymptomId(sessionSymptom.getId());
            predictionRequest.setGlobalScore(globalScore);
            predictionRequest.setIsRedAlert(isRedAlert);
            predictionRequest.setComment("AI prediction based on symptoms and patient profile");
            
            Prediction created = predictionService.createPrediction(predictionRequest);
            
            // Create pathology results for top predictions (using descriptions from Flask)
            createPathologyResults(created, mlResponse, language);
            
            log.info("Prediction created successfully with id: {}", created.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(predictionService.convertToDTO(created));
            
        } catch (Exception e) {
            log.error("Error creating prediction", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    private MLPredictionRequestDTO buildMLRequest(List<String> symptoms, Optional<PatientMedicalProfile> profileOpt, String language) {
        MLPredictionRequestDTO.MLPredictionRequestDTOBuilder builder = MLPredictionRequestDTO.builder()
                .symptoms(symptoms)
                .language(language);
        
        if (profileOpt.isPresent()) {
            PatientMedicalProfile profile = profileOpt.get();
            builder.age(profile.getAge())
                   .weight(profile.getWeight())
                   .height(170f) // default height (can be calculated from BMI if needed)
                   .tension_moyenne(profile.getMeanBloodPressure())
                   .cholesterole_moyen(profile.getMeanCholesterol())
                   .gender(profile.getGender() != null ? profile.getGender().name() : "Male")
                   .smoking(profile.getIsSmoking() != null && profile.getIsSmoking() ? "Yes" : "No")
                   .alcohol(profile.getAlcohol() != null && profile.getAlcohol() ? "Moderate" : "None")
                   .sedentarite(profile.getSedentary() != null && profile.getSedentary() ? "High" : "Moderate")
                   .family_history(profile.getFamilyAntecedents() != null && !profile.getFamilyAntecedents().isEmpty() ? "Yes" : "No");
            
            // Map blood pressure and cholesterol (simplified mapping)
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
            // Default values if no profile
            builder.age(35)
                   .weight(75f)
                   .height(170f)
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

    private String normalizeLanguage(String language) {
        if (language == null || language.trim().isEmpty()) {
            return "fr";
        }
        String normalized = language.trim().toLowerCase();
        return normalized.equals("fr") || normalized.equals("en") ? normalized : "fr";
    }
    
    private boolean determineRedAlert(MLPredictionResponseDTO mlResponse) {
        // Red alert if top prediction has very high probability (>90%) or specific critical diseases
        if (mlResponse.getPredictions() != null && !mlResponse.getPredictions().isEmpty()) {
            MLPredictionResponseDTO.PredictionResult topResult = mlResponse.getPredictions().get(0);
            if (topResult.getProbability() != null && topResult.getProbability() > 90.0) {
                return true;
            }
            // Check for critical disease names (can be extended)
            String disease = topResult.getDisease() != null ? topResult.getDisease().toLowerCase() : "";
            if (disease.contains("heart") || disease.contains("cardiac") || 
                disease.contains("stroke") || disease.contains("severe")) {
                return true;
            }
        }
        return false;
    }
    
    private BigDecimal calculateGlobalScore(MLPredictionResponseDTO mlResponse) {
        if (mlResponse.getPredictions() == null || mlResponse.getPredictions().isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // Average of top 3 predictions
        double sum = mlResponse.getPredictions().stream()
                .limit(3)
                .mapToDouble(p -> p.getProbability() != null ? p.getProbability() : 0.0)
                .sum();
        
        int count = Math.min(3, mlResponse.getPredictions().size());
        return BigDecimal.valueOf(sum / count);
    }
    
    private void createPathologyResults(Prediction prediction, MLPredictionResponseDTO mlResponse, String language) {
        if (mlResponse.getPredictions() == null || mlResponse.getPredictions().isEmpty()) {
            return;
        }
        
        // Create pathology results for top 3 predictions
        mlResponse.getPredictions().stream()
                .limit(3)
                .forEach(result -> {
                    try {
                        // Get translated disease name based on language
                        String diseaseName = "fr".equals(language) && result.getDisease_fr() != null 
                                ? result.getDisease_fr() 
                                : result.getDisease();
                        
                        // Get translated specialist name based on language
                        String specialistName = "fr".equals(language) && result.getSpecialist_fr() != null 
                                ? result.getSpecialist_fr() 
                                : result.getSpecialist();
                        
                        // Find or create Pathology (use original EN name for lookup)
                        Pathology pathology = pathologyService.getPathologyByName(result.getDisease())
                                .orElseGet(() -> {
                                    Pathology newPathology = new Pathology();
                                    newPathology.setPathologyName(result.getDisease()); // Store EN name
                                    newPathology.setDescription("AI predicted pathology");
                                    return pathologyService.createPathology(newPathology);
                                });
                        
                        // Find or create Doctor/Specialist (use original EN name for lookup)
                        Doctor doctor = doctorService.getDoctorBySpecialistLabel(result.getSpecialist())
                                .orElseGet(() -> {
                                    Doctor newDoctor = new Doctor();
                                    newDoctor.setSpecialistLabel(result.getSpecialist()); // Store EN name
                                    newDoctor.setSpecialistScore(BigDecimal.valueOf(result.getSpecialist_probability() != null ? result.getSpecialist_probability() : 0.0));
                                    newDoctor.setDescription("AI recommended specialist");
                                    return doctorService.createDoctor(newDoctor);
                                });
                        
                        // Create PathologyResult using description from Flask
                        PathologyResultRequestDTO pathologyResultRequest = new PathologyResultRequestDTO();
                        pathologyResultRequest.setPredictionId(prediction.getId().toString());
                        pathologyResultRequest.setPathologyId(pathology.getId());
                        pathologyResultRequest.setDoctorId(doctor.getId());
                        pathologyResultRequest.setDiseaseScore(BigDecimal.valueOf(result.getProbability() != null ? result.getProbability() : 0.0));
                        
                        // Use description from Flask if available, otherwise generate a simple one
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

    @PutMapping("/{id}")
    @Operation(summary = "Update a prediction", description = "Updates an existing prediction by ID")
    public ResponseEntity<PredictionDTO> updatePrediction(
            @Parameter(description = "Prediction ID") @PathVariable Long id,
            @Valid @RequestBody PredictionRequestDTO requestDTO) {
        try {
            Prediction updated = predictionService.updatePrediction(id, requestDTO);
            return ResponseEntity.ok(predictionService.convertToDTO(updated));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a prediction", description = "Deletes a prediction by ID")
    public ResponseEntity<Void> deletePrediction(
            @Parameter(description = "Prediction ID") @PathVariable Long id) {
        predictionService.deletePrediction(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get prediction by ID", description = "Retrieves a prediction by its ID")
    public ResponseEntity<PredictionDTO> getPredictionById(
            @Parameter(description = "Prediction ID") @PathVariable Long id) {
        return predictionService.getPredictionById(id)
                .map(prediction -> ResponseEntity.ok(predictionService.convertToDTO(prediction)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/session-symptom/{sessionSymptomId}")
    @Operation(summary = "Get predictions by session symptom ID", description = "Retrieves all predictions for a specific session symptom")
    public ResponseEntity<List<PredictionDTO>> getPredictionsBySessionSymptomId(
            @Parameter(description = "Session Symptom ID") @PathVariable Long sessionSymptomId) {
        List<Prediction> predictions = predictionService.getPredictionsBySessionSymptomId(sessionSymptomId);
        return ResponseEntity.ok(predictionService.convertToDTOList(predictions));
    }

    @GetMapping
    @Operation(summary = "Get all predictions", description = "Retrieves all predictions in the system")
    public ResponseEntity<List<PredictionDTO>> getAllPredictions() {
        List<Prediction> predictions = predictionService.getAllPredictions();
        return ResponseEntity.ok(predictionService.convertToDTOList(predictions));
    }

    @GetMapping("/red-alerts")
    @Operation(summary = "Get red alert predictions", description = "Retrieves all predictions marked as red alerts")
    public ResponseEntity<List<PredictionDTO>> getRedAlertPredictions() {
        List<Prediction> predictions = predictionService.getRedAlertPredictions();
        return ResponseEntity.ok(predictionService.convertToDTOList(predictions));
    }
}
