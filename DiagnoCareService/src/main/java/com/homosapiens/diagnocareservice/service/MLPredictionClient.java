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

}
