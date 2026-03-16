package com.homosapiens.diagnocareservice.service;

import com.homosapiens.diagnocareservice.core.config.MLServiceConfig;
import com.homosapiens.diagnocareservice.core.exception.AppException;
import com.homosapiens.diagnocareservice.dto.MLPredictionRequestDTO;
import com.homosapiens.diagnocareservice.dto.MLPredictionResponseDTO;
import com.homosapiens.diagnocareservice.dto.MLTranslationRequestDTO;
import com.homosapiens.diagnocareservice.dto.MLTranslationResponseDTO;
import com.homosapiens.diagnocareservice.dto.MLSymptomExtractionRequestDTO;
import com.homosapiens.diagnocareservice.dto.MLSymptomExtractionResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MLPredictionClient {

    private final RestTemplate directRestTemplate;
    private final RestTemplate loadBalancedRestTemplate;
    private final MLServiceConfig mlServiceConfig;

    public MLPredictionResponseDTO predict(MLPredictionRequestDTO request) {
        try {
            String url = mlServiceConfig.getMlServiceUrl() + "/predict";
            log.info("Calling ML service at: {}", url);

            RestTemplate restTemplate = selectRestTemplate(url);
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

    public MLTranslationResponseDTO translate(MLTranslationRequestDTO request) {
        try {
            String url = mlServiceConfig.getMlServiceUrl() + "/translate";
            log.info("Calling ML translation service at: {}", url);

            RestTemplate restTemplate = selectRestTemplate(url);
            ResponseEntity<MLTranslationResponseDTO> response = restTemplate.postForEntity(
                    url,
                    request,
                    MLTranslationResponseDTO.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }

            log.error("ML translation service returned non-success status: {}", response.getStatusCode());
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "ML translation service returned an error: " + response.getStatusCode());
        } catch (RestClientException e) {
            log.error("Error calling ML translation service: {}", e.getMessage(), e);
            throw new AppException(HttpStatus.SERVICE_UNAVAILABLE,
                    "ML translation service is unavailable: " + e.getMessage());
        }
    }

    private RestTemplate selectRestTemplate(String url) {
        try {
            URI uri = URI.create(url);
            String host = uri.getHost();
            int port = uri.getPort();

            if (port != -1) {
                return directRestTemplate;
            }
            if (host == null) {
                return loadBalancedRestTemplate;
            }
            String lowerHost = host.toLowerCase();
            if ("localhost".equals(lowerHost) || "127.0.0.1".equals(lowerHost)) {
                return directRestTemplate;
            }
            if (host.contains(".")) {
                return directRestTemplate;
            }
        } catch (IllegalArgumentException ignored) {
            return loadBalancedRestTemplate;
        }

        return loadBalancedRestTemplate;
    }
}
