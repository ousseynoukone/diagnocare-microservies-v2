package com.homosapiens.diagnocareservice.service;

import com.homosapiens.diagnocareservice.dto.PredictionCreationResult;
import com.homosapiens.diagnocareservice.dto.SessionSymptomRequestDTO;

public interface PredictionWorkflowService {
    PredictionCreationResult createPrediction(SessionSymptomRequestDTO requestDTO, Long previousPredictionId);
}
