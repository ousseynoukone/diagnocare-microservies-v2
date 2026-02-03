package com.homosapiens.diagnocareservice.service.impl;

import com.homosapiens.diagnocareservice.core.exception.AppException;
import com.homosapiens.diagnocareservice.dto.PathologyResultRequestDTO;
import com.homosapiens.diagnocareservice.dto.PredictionDTO;
import com.homosapiens.diagnocareservice.dto.PredictionRequestDTO;
import com.homosapiens.diagnocareservice.model.entity.Prediction;
import com.homosapiens.diagnocareservice.model.entity.SessionSymptom;
import com.homosapiens.diagnocareservice.repository.PredictionRepository;
import com.homosapiens.diagnocareservice.repository.SessionSymptomRepository;
import com.homosapiens.diagnocareservice.service.PathologyResultService;
import com.homosapiens.diagnocareservice.service.PredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PredictionServiceImpl implements PredictionService {

    private final PredictionRepository predictionRepository;
    private final SessionSymptomRepository sessionSymptomRepository;
    private final PathologyResultService pathologyResultService;

    @Override
    public Prediction createPrediction(PredictionRequestDTO requestDTO) {
        SessionSymptom sessionSymptom = sessionSymptomRepository.findById(requestDTO.getSessionSymptomId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, 
                        "Session symptom not found with id: " + requestDTO.getSessionSymptomId()));

        Prediction prediction = new Prediction();
        prediction.setSessionSymptom(sessionSymptom);
        prediction.setGlobalScore(requestDTO.getGlobalScore());
        prediction.setIsRedAlert(requestDTO.getIsRedAlert() != null ? requestDTO.getIsRedAlert() : false);
        prediction.setComment(requestDTO.getComment());

        // Handle previous prediction relationship
        if (requestDTO.getPreviousPredictionId() != null) {
            Prediction previousPrediction = predictionRepository.findById(requestDTO.getPreviousPredictionId())
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, 
                            "Previous prediction not found with id: " + requestDTO.getPreviousPredictionId()));
            prediction.setPreviousPrediction(previousPrediction);
        }

        Prediction savedPrediction = predictionRepository.save(prediction);

        // Create pathology results if provided
        if (requestDTO.getPathologyResults() != null && !requestDTO.getPathologyResults().isEmpty()) {
            for (PathologyResultRequestDTO pathologyResultRequest : requestDTO.getPathologyResults()) {
                pathologyResultRequest.setPredictionId(savedPrediction.getId().toString());
                pathologyResultService.createPathologyResult(savedPrediction.getId(), pathologyResultRequest);
            }
        }

        return savedPrediction;
    }

    @Override
    public Prediction updatePrediction(Long id, PredictionRequestDTO requestDTO) {
        Prediction prediction = predictionRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, 
                        "Prediction not found with id: " + id));

        if (requestDTO.getGlobalScore() != null) {
            prediction.setGlobalScore(requestDTO.getGlobalScore());
        }
        if (requestDTO.getIsRedAlert() != null) {
            prediction.setIsRedAlert(requestDTO.getIsRedAlert());
        }
        if (requestDTO.getComment() != null) {
            prediction.setComment(requestDTO.getComment());
        }
        if (requestDTO.getPreviousPredictionId() != null) {
            Prediction previousPrediction = predictionRepository.findById(requestDTO.getPreviousPredictionId())
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, 
                            "Previous prediction not found with id: " + requestDTO.getPreviousPredictionId()));
            prediction.setPreviousPrediction(previousPrediction);
        }

        return predictionRepository.save(prediction);
    }

    @Override
    public void deletePrediction(Long id) {
        predictionRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Prediction> getPredictionById(Long id) {
        return predictionRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Prediction> getAllPredictions() {
        return predictionRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Prediction> getRedAlertPredictions() {
        return predictionRepository.findByIsRedAlert(true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Prediction> getPredictionsBySessionSymptomId(Long sessionSymptomId) {
        return predictionRepository.findBySessionSymptomId(sessionSymptomId);
    }

    @Override
    public PredictionDTO convertToDTO(Prediction prediction) {
        PredictionDTO dto = new PredictionDTO();
        dto.setId(prediction.getId());
        dto.setGlobalScore(prediction.getGlobalScore());
        dto.setPdfReportUrl(prediction.getPdfReportUrl());
        dto.setIsRedAlert(prediction.getIsRedAlert());
        dto.setComment(prediction.getComment());
        dto.setSessionSymptomId(prediction.getSessionSymptom().getId());
        dto.setPreviousPredictionId(prediction.getPreviousPrediction() != null ? 
                prediction.getPreviousPrediction().getId() : null);
        dto.setCreatedAt(prediction.getCreatedDate());
        return dto;
    }

    @Override
    public List<PredictionDTO> convertToDTOList(List<Prediction> predictions) {
        return predictions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}
