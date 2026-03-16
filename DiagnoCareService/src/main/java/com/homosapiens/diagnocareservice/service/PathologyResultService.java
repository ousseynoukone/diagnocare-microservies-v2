package com.homosapiens.diagnocareservice.service;

import com.homosapiens.diagnocareservice.dto.PathologyResultDTO;
import com.homosapiens.diagnocareservice.dto.PathologyResultRequestDTO;
import com.homosapiens.diagnocareservice.model.entity.PathologyResult;

import java.util.List;
import java.util.Optional;

public interface PathologyResultService {
    PathologyResult createPathologyResult(PathologyResultRequestDTO requestDTO);
    PathologyResult createPathologyResult(Long predictionId, PathologyResultRequestDTO requestDTO);
    PathologyResult updatePathologyResult(Long id, PathologyResultRequestDTO requestDTO);
    void deletePathologyResult(Long id);
    Optional<PathologyResult> getPathologyResultById(Long id);
    List<PathologyResult> getPathologyResultsByPredictionId(Long predictionId);
    PathologyResultDTO convertToDTO(PathologyResult pathologyResult);
    List<PathologyResultDTO> convertToDTOList(List<PathologyResult> pathologyResults);
}
