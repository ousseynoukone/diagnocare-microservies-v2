package com.homosapiens.diagnocareservice.service;

import com.homosapiens.diagnocareservice.dto.SessionSymptomDTO;
import com.homosapiens.diagnocareservice.dto.SessionSymptomRequestDTO;
import com.homosapiens.diagnocareservice.model.entity.SessionSymptom;

import java.util.List;
import java.util.Optional;

public interface SessionSymptomService {
    SessionSymptom createSessionSymptom(SessionSymptomRequestDTO requestDTO);
    SessionSymptom updateSessionSymptom(Long id, SessionSymptomRequestDTO requestDTO);
    void deleteSessionSymptom(Long id);
    Optional<SessionSymptom> getSessionSymptomById(Long id);
    List<SessionSymptom> getSessionSymptomsByUserId(Long userId);
    SessionSymptomDTO convertToDTO(SessionSymptom sessionSymptom);
    List<SessionSymptomDTO> convertToDTOList(List<SessionSymptom> sessionSymptoms);
}
