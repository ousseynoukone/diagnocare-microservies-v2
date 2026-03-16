package com.homosapiens.diagnocareservice.service;

import com.homosapiens.diagnocareservice.dto.MLTranslationRequestDTO;
import com.homosapiens.diagnocareservice.dto.MLTranslationResponseDTO;

public interface TranslationService {
    MLTranslationResponseDTO translate(MLTranslationRequestDTO request);
}
