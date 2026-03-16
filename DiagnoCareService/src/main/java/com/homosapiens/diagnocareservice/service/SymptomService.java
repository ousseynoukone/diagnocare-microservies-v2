package com.homosapiens.diagnocareservice.service;

import com.homosapiens.diagnocareservice.dto.SymptomDTO;
import com.homosapiens.diagnocareservice.model.entity.Symptom;

import java.util.List;
import java.util.Optional;

public interface SymptomService {
    Symptom createSymptom(Symptom symptom);
    Symptom updateSymptom(Long id, Symptom symptom);
    void deleteSymptom(Long id);
    Optional<Symptom> getSymptomById(Long id);
    List<Symptom> getAllSymptoms();
    List<Symptom> searchSymptomsByLabel(String label);
    SymptomDTO convertToDTO(Symptom symptom);
    List<SymptomDTO> convertToDTOList(List<Symptom> symptoms);
}
