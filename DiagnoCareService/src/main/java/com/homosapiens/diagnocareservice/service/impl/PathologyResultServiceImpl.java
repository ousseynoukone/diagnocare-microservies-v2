package com.homosapiens.diagnocareservice.service.impl;

import com.homosapiens.diagnocareservice.core.exception.AppException;
import com.homosapiens.diagnocareservice.dto.PathologyResultDTO;
import com.homosapiens.diagnocareservice.dto.PathologyResultRequestDTO;
import com.homosapiens.diagnocareservice.model.entity.Doctor;
import com.homosapiens.diagnocareservice.model.entity.Pathology;
import com.homosapiens.diagnocareservice.model.entity.PathologyResult;
import com.homosapiens.diagnocareservice.model.entity.Prediction;
import com.homosapiens.diagnocareservice.repository.DoctorRepository;
import com.homosapiens.diagnocareservice.repository.PathologyRepository;
import com.homosapiens.diagnocareservice.repository.PathologyResultRepository;
import com.homosapiens.diagnocareservice.repository.PredictionRepository;
import com.homosapiens.diagnocareservice.service.PathologyResultService;
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
public class PathologyResultServiceImpl implements PathologyResultService {

    private final PathologyResultRepository pathologyResultRepository;
    private final PathologyRepository pathologyRepository;
    private final DoctorRepository doctorRepository;
    private final PredictionRepository predictionRepository;

    @Override
    public PathologyResult createPathologyResult(PathologyResultRequestDTO requestDTO) {
        Long predictionId = null;
        if (requestDTO.getPredictionId() != null && !requestDTO.getPredictionId().isEmpty()) {
            try {
                predictionId = Long.parseLong(requestDTO.getPredictionId());
            } catch (NumberFormatException e) {
                throw new AppException(HttpStatus.BAD_REQUEST, 
                        "Invalid prediction ID format: " + requestDTO.getPredictionId());
            }
        }
        return createPathologyResult(predictionId, requestDTO);
    }

    @Override
    public PathologyResult createPathologyResult(Long predictionId, PathologyResultRequestDTO requestDTO) {
        Pathology pathology = pathologyRepository.findById(requestDTO.getPathologyId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, 
                        "Pathology not found with id: " + requestDTO.getPathologyId()));

        Doctor doctor = doctorRepository.findById(requestDTO.getDoctorId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, 
                        "Doctor not found with id: " + requestDTO.getDoctorId()));

        Prediction prediction = null;
        if (predictionId != null) {
            prediction = predictionRepository.findById(predictionId)
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, 
                            "Prediction not found with id: " + predictionId));
        } else if (requestDTO.getPredictionId() != null && !requestDTO.getPredictionId().isEmpty()) {
            try {
                Long parsedId = Long.parseLong(requestDTO.getPredictionId());
                prediction = predictionRepository.findById(parsedId)
                        .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, 
                                "Prediction not found with id: " + requestDTO.getPredictionId()));
            } catch (NumberFormatException e) {
                throw new AppException(HttpStatus.BAD_REQUEST, 
                        "Invalid prediction ID format: " + requestDTO.getPredictionId());
            }
        }

        PathologyResult pathologyResult = new PathologyResult();
        pathologyResult.setPathology(pathology);
        pathologyResult.setDoctor(doctor);
        pathologyResult.setPrediction(prediction);
        pathologyResult.setDiseaseScore(requestDTO.getDiseaseScore());
        pathologyResult.setDescription(requestDTO.getDescription());
        pathologyResult.setLocalizedDiseaseName(requestDTO.getLocalizedDiseaseName());
        pathologyResult.setLocalizedSpecialistLabel(requestDTO.getLocalizedSpecialistLabel());

        return pathologyResultRepository.save(pathologyResult);
    }

    @Override
    public PathologyResult updatePathologyResult(Long id, PathologyResultRequestDTO requestDTO) {
        PathologyResult pathologyResult = pathologyResultRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, 
                        "Pathology result not found with id: " + id));

        if (requestDTO.getPathologyId() != null) {
            Pathology pathology = pathologyRepository.findById(requestDTO.getPathologyId())
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, 
                            "Pathology not found with id: " + requestDTO.getPathologyId()));
            pathologyResult.setPathology(pathology);
        }

        if (requestDTO.getDoctorId() != null) {
            Doctor doctor = doctorRepository.findById(requestDTO.getDoctorId())
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, 
                            "Doctor not found with id: " + requestDTO.getDoctorId()));
            pathologyResult.setDoctor(doctor);
        }

        if (requestDTO.getDiseaseScore() != null) {
            pathologyResult.setDiseaseScore(requestDTO.getDiseaseScore());
        }
        if (requestDTO.getDescription() != null) {
            pathologyResult.setDescription(requestDTO.getDescription());
        }
        if (requestDTO.getLocalizedDiseaseName() != null) {
            pathologyResult.setLocalizedDiseaseName(requestDTO.getLocalizedDiseaseName());
        }
        if (requestDTO.getLocalizedSpecialistLabel() != null) {
            pathologyResult.setLocalizedSpecialistLabel(requestDTO.getLocalizedSpecialistLabel());
        }

        return pathologyResultRepository.save(pathologyResult);
    }

    @Override
    public void deletePathologyResult(Long id) {
        pathologyResultRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PathologyResult> getPathologyResultById(Long id) {
        return pathologyResultRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PathologyResult> getPathologyResultsByPredictionId(Long predictionId) {
        return pathologyResultRepository.findByPredictionId(predictionId);
    }

    @Override
    public PathologyResultDTO convertToDTO(PathologyResult pathologyResult) {
        PathologyResultDTO dto = new PathologyResultDTO();
        dto.setId(pathologyResult.getId());
        dto.setDiseaseScore(pathologyResult.getDiseaseScore());
        dto.setDescription(pathologyResult.getDescription());
        dto.setPathologyId(pathologyResult.getPathology().getId());
        dto.setPathologyName(pathologyResult.getPathology().getPathologyName());
        dto.setDoctorId(pathologyResult.getDoctor().getId());
        dto.setDoctorSpecialistLabel(pathologyResult.getDoctor().getSpecialistLabel());
        dto.setPredictionId(pathologyResult.getPrediction() != null ? 
                pathologyResult.getPrediction().getId().toString() : null);
        return dto;
    }

    @Override
    public List<PathologyResultDTO> convertToDTOList(List<PathologyResult> pathologyResults) {
        return pathologyResults.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}
