package com.homosapiens.diagnocareservice.service;

import com.homosapiens.diagnocareservice.core.config.MLServiceConfig;
import com.homosapiens.diagnocareservice.core.exception.AppException;
import com.homosapiens.diagnocareservice.dto.MLPredictionRequestDTO;
import com.homosapiens.diagnocareservice.dto.MLPredictionResponseDTO;
import com.homosapiens.diagnocareservice.dto.MLSymptomExtractionRequestDTO;
import com.homosapiens.diagnocareservice.dto.MLSymptomExtractionResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MLPredictionClient {

    private final RestTemplate restTemplate;
    private final MLServiceConfig mlServiceConfig;

    public MLPredictionResponseDTO predict(MLPredictionRequestDTO request) {
        try {
            String url = mlServiceConfig.getMlServiceUrl() + "/predict";
            log.info("Calling ML service at: {}", url);
            
            ResponseEntity<MLPredictionResponseDTO> response = restTemplate.postForEntity(
                    url,
                    request,
                    MLPredictionResponseDTO.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("ML prediction successful. Found {} predictions", 
                        response.getBody().getPredictions() != null ? response.getBody().getPredictions().size() : 0);
                return response.getBody();
            } else {
                log.error("ML service returned non-success status: {}", response.getStatusCode());
                throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, 
                        "ML service returned an error: " + response.getStatusCode());
            }
        } catch (RestClientException e) {
            log.error("Error calling ML service: {}", e.getMessage(), e);
            throw new AppException(HttpStatus.SERVICE_UNAVAILABLE, 
                    "ML service is unavailable: " + e.getMessage());
        }
    }

    public List<String> extractSymptoms(String rawDescription, String language) {
        try {
            String url = mlServiceConfig.getMlServiceUrl() + "/extract-symptoms";
            log.info("Calling ML service for symptom extraction at: {}", url);
            
            MLSymptomExtractionRequestDTO request = MLSymptomExtractionRequestDTO.builder()
                    .raw_description(rawDescription)
                    .language(language != null ? language : "fr")
                    .build();
            
            ResponseEntity<MLSymptomExtractionResponseDTO> response = restTemplate.postForEntity(
                    url,
                    request,
                    MLSymptomExtractionResponseDTO.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<String> symptoms = response.getBody().getSymptoms();
                log.info("ML symptom extraction successful. Found {} symptoms", 
                        symptoms != null ? symptoms.size() : 0);
                return symptoms != null ? symptoms : List.of();
            } else {
                log.error("ML service returned non-success status: {}", response.getStatusCode());
                throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, 
                        "ML service returned an error: " + response.getStatusCode());
            }
        } catch (RestClientException e) {
            log.error("Error calling ML service for symptom extraction: {}", e.getMessage(), e);
            throw new AppException(HttpStatus.SERVICE_UNAVAILABLE, 
                    "ML service is unavailable: " + e.getMessage());
        }
    }

    public boolean healthCheck() {
        try {
            String url = mlServiceConfig.getMlServiceUrl() + "/health";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (RestClientException e) {
            log.warn("ML service health check failed: {}", e.getMessage());
            return false;
        }
    }
}
