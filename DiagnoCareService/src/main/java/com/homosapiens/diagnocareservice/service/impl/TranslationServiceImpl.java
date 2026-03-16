package com.homosapiens.diagnocareservice.service.impl;

import com.homosapiens.diagnocareservice.dto.MLTranslationRequestDTO;
import com.homosapiens.diagnocareservice.dto.MLTranslationResponseDTO;
import com.homosapiens.diagnocareservice.service.MLPredictionClient;
import com.homosapiens.diagnocareservice.service.TranslationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TranslationServiceImpl implements TranslationService {

    private final MLPredictionClient mlPredictionClient;

    @Override
    public MLTranslationResponseDTO translate(MLTranslationRequestDTO request) {
        if (request == null || "en".equalsIgnoreCase(request.getLanguage())) {
            return fallbackResponse(request);
        }
        try {
            return mlPredictionClient.translate(request);
        } catch (Exception ex) {
            log.warn("Failed to translate via ML service. Using fallback values.", ex);
            return fallbackResponse(request);
        }
    }

    private MLTranslationResponseDTO fallbackResponse(MLTranslationRequestDTO request) {
        if (request == null) {
            return MLTranslationResponseDTO.builder()
                    .language("fr")
                    .symptoms(Collections.emptyList())
                    .diseases(Collections.emptyList())
                    .specialists(Collections.emptyList())
                    .build();
        }
        return MLTranslationResponseDTO.builder()
                .language(request.getLanguage())
                .symptoms(defaultList(request.getSymptoms()))
                .diseases(defaultList(request.getDiseases()))
                .specialists(defaultList(request.getSpecialists()))
                .build();
    }

    private List<String> defaultList(List<String> values) {
        return values != null ? values : Collections.emptyList();
    }
}
