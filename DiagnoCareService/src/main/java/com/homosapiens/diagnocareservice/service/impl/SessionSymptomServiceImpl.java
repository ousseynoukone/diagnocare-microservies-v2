package com.homosapiens.diagnocareservice.service.impl;

import com.homosapiens.diagnocareservice.core.exception.AppException;
import com.homosapiens.diagnocareservice.dto.SessionSymptomDTO;
import com.homosapiens.diagnocareservice.dto.SessionSymptomRequestDTO;
import com.homosapiens.diagnocareservice.dto.SymptomDTO;
import com.homosapiens.diagnocareservice.model.entity.SessionSymptom;
import com.homosapiens.diagnocareservice.model.entity.Symptom;
import com.homosapiens.diagnocareservice.model.entity.User;
import com.homosapiens.diagnocareservice.repository.SessionSymptomRepository;
import com.homosapiens.diagnocareservice.repository.SymptomRepository;
import com.homosapiens.diagnocareservice.repository.UserRepository;
import com.homosapiens.diagnocareservice.service.SessionSymptomService;
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
public class SessionSymptomServiceImpl implements SessionSymptomService {

    private final SessionSymptomRepository sessionSymptomRepository;
    private final UserRepository userRepository;
    private final SymptomRepository symptomRepository;

    @Override
    public SessionSymptom createSessionSymptom(SessionSymptomRequestDTO requestDTO) {
        User user = userRepository.findById(requestDTO.getUserId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "User not found with id: " + requestDTO.getUserId()));

        SessionSymptom sessionSymptom = new SessionSymptom();
        sessionSymptom.setUser(user);
        sessionSymptom.setRawDescription(requestDTO.getRawDescription());

        List<Symptom> symptoms = requestDTO.getSymptomIds().stream()
                .map(id -> symptomRepository.findById(id)
                        .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Symptom not found with id: " + id)))
                .collect(Collectors.toList());

        sessionSymptom.setSymptoms(symptoms);
        return sessionSymptomRepository.save(sessionSymptom);
    }

    @Override
    public SessionSymptom updateSessionSymptom(Long id, SessionSymptomRequestDTO requestDTO) {
        SessionSymptom sessionSymptom = sessionSymptomRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Session symptom not found with id: " + id));

        if (requestDTO.getRawDescription() != null) {
            sessionSymptom.setRawDescription(requestDTO.getRawDescription());
        }

        if (requestDTO.getSymptomIds() != null && !requestDTO.getSymptomIds().isEmpty()) {
            List<Symptom> symptoms = requestDTO.getSymptomIds().stream()
                    .map(symptomId -> symptomRepository.findById(symptomId)
                            .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Symptom not found with id: " + symptomId)))
                    .collect(Collectors.toList());
            sessionSymptom.setSymptoms(symptoms);
        }

        return sessionSymptomRepository.save(sessionSymptom);
    }

    @Override
    public void deleteSessionSymptom(Long id) {
        sessionSymptomRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SessionSymptom> getSessionSymptomById(Long id) {
        return sessionSymptomRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionSymptom> getSessionSymptomsByUserId(Long userId) {
        return sessionSymptomRepository.findByUserId(userId);
    }

    @Override
    public SessionSymptomDTO convertToDTO(SessionSymptom sessionSymptom) {
        SessionSymptomDTO dto = new SessionSymptomDTO();
        dto.setId(sessionSymptom.getId());
        dto.setUserId(sessionSymptom.getUser().getId());
        dto.setRawDescription(sessionSymptom.getRawDescription());
        
        if (sessionSymptom.getPredictions() != null && !sessionSymptom.getPredictions().isEmpty()) {
            List<Long> predictionIds = sessionSymptom.getPredictions().stream()
                    .map(prediction -> prediction.getId())
                    .collect(Collectors.toList());
            dto.setPredictionIds(predictionIds);
        }

        if (sessionSymptom.getSymptoms() != null) {
            List<SymptomDTO> symptomDTOs = sessionSymptom.getSymptoms().stream()
                    .map(symptom -> {
                        SymptomDTO symptomDTO = new SymptomDTO();
                        symptomDTO.setId(symptom.getId());
                        symptomDTO.setLabel(symptom.getLabel());
                        symptomDTO.setSymptomLabelId(symptom.getSymptomLabelId());
                        return symptomDTO;
                    })
                    .collect(Collectors.toList());
            dto.setSymptoms(symptomDTOs);
        }

        return dto;
    }

    @Override
    public List<SessionSymptomDTO> convertToDTOList(List<SessionSymptom> sessionSymptoms) {
        return sessionSymptoms.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}
