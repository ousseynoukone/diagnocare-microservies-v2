package com.homosapiens.diagnocareservice.service.impl;

import com.homosapiens.diagnocareservice.dto.SymptomDTO;
import com.homosapiens.diagnocareservice.model.entity.Symptom;
import com.homosapiens.diagnocareservice.repository.SymptomRepository;
import com.homosapiens.diagnocareservice.service.SymptomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class SymptomServiceImpl implements SymptomService {

    private final SymptomRepository symptomRepository;

    @Override
    public Symptom createSymptom(Symptom symptom) {
        return symptomRepository.save(symptom);
    }

    @Override
    public Symptom updateSymptom(Long id, Symptom symptom) {
        if (symptomRepository.existsById(id)) {
            symptom.setId(id);
            return symptomRepository.save(symptom);
        }
        throw new RuntimeException("Symptom not found with id: " + id);
    }

    @Override
    public void deleteSymptom(Long id) {
        symptomRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Symptom> getSymptomById(Long id) {
        return symptomRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Symptom> getAllSymptoms() {
        return symptomRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Symptom> searchSymptomsByLabel(String label) {
        return symptomRepository.findByLabelContainingIgnoreCase(label);
    }

    @Override
    public SymptomDTO convertToDTO(Symptom symptom) {
        SymptomDTO dto = new SymptomDTO();
        dto.setId(symptom.getId());
        dto.setLabel(symptom.getLabel());
        dto.setSymptomLabelId(symptom.getSymptomLabelId());
        return dto;
    }

    @Override
    public List<SymptomDTO> convertToDTOList(List<Symptom> symptoms) {
        return symptoms.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}
