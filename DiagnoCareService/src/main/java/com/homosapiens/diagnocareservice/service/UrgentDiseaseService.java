package com.homosapiens.diagnocareservice.service;

import com.homosapiens.diagnocareservice.dto.UrgentDiseaseDTO;
import com.homosapiens.diagnocareservice.dto.UrgentDiseaseRequestDTO;

import java.util.List;

public interface UrgentDiseaseService {
    List<UrgentDiseaseDTO> getAll();
    UrgentDiseaseDTO create(UrgentDiseaseRequestDTO requestDTO);
    void delete(Long id);
    boolean isUrgentDisease(String diseaseName);
}
