package com.homosapiens.diagnocareservice.service.impl;

import com.homosapiens.diagnocareservice.core.exception.AppException;
import com.homosapiens.diagnocareservice.dto.UrgentDiseaseDTO;
import com.homosapiens.diagnocareservice.dto.UrgentDiseaseRequestDTO;
import com.homosapiens.diagnocareservice.model.entity.UrgentDisease;
import com.homosapiens.diagnocareservice.repository.UrgentDiseaseRepository;
import com.homosapiens.diagnocareservice.service.UrgentDiseaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UrgentDiseaseServiceImpl implements UrgentDiseaseService {

    private final UrgentDiseaseRepository urgentDiseaseRepository;

    @Override
    public List<UrgentDiseaseDTO> getAll() {
        return urgentDiseaseRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public UrgentDiseaseDTO create(UrgentDiseaseRequestDTO requestDTO) {
        String name = requestDTO.getDiseaseName().trim();
        if (urgentDiseaseRepository.existsByDiseaseNameIgnoreCase(name)) {
            throw new AppException(HttpStatus.CONFLICT, "Urgent disease already exists");
        }
        UrgentDisease urgentDisease = new UrgentDisease();
        urgentDisease.setDiseaseName(name);
        return toDto(urgentDiseaseRepository.save(urgentDisease));
    }

    @Override
    public void delete(Long id) {
        if (!urgentDiseaseRepository.existsById(id)) {
            throw new AppException(HttpStatus.NOT_FOUND, "Urgent disease not found");
        }
        urgentDiseaseRepository.deleteById(id);
    }

    @Override
    public boolean isUrgentDisease(String diseaseName) {
        if (diseaseName == null || diseaseName.trim().isEmpty()) {
            return false;
        }
        return urgentDiseaseRepository.existsByDiseaseNameIgnoreCase(diseaseName.trim());
    }

    private UrgentDiseaseDTO toDto(UrgentDisease urgentDisease) {
        UrgentDiseaseDTO dto = new UrgentDiseaseDTO();
        dto.setId(urgentDisease.getId());
        dto.setDiseaseName(urgentDisease.getDiseaseName());
        return dto;
    }
}
