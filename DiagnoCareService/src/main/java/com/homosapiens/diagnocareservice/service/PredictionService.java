package com.homosapiens.diagnocareservice.service;

import com.homosapiens.diagnocareservice.dto.PredictionDTO;
import com.homosapiens.diagnocareservice.dto.PredictionRequestDTO;
import com.homosapiens.diagnocareservice.model.entity.Prediction;

import java.util.List;
import java.util.Optional;

public interface PredictionService {
    Prediction createPrediction(PredictionRequestDTO requestDTO);
    Prediction updatePrediction(Long id, PredictionRequestDTO requestDTO);
    void deletePrediction(Long id);
    Optional<Prediction> getPredictionById(Long id);
    List<Prediction> getAllPredictions();
    List<Prediction> getRedAlertPredictions();
    List<Prediction> getPredictionsBySessionSymptomId(Long sessionSymptomId);
    List<Prediction> getPredictionsByUserId(Long userId);
    PredictionDTO convertToDTO(Prediction prediction);
    List<PredictionDTO> convertToDTOList(List<Prediction> predictions);
}
