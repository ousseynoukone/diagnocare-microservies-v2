package com.homosapiens.diagnocareservice.service;

import com.homosapiens.diagnocareservice.dto.PathologyDTO;
import com.homosapiens.diagnocareservice.model.entity.Pathology;

import java.util.List;
import java.util.Optional;

public interface PathologyService {
    Pathology createPathology(Pathology pathology);
    Pathology updatePathology(Long id, Pathology pathology);
    void deletePathology(Long id);
    Optional<Pathology> getPathologyById(Long id);
    List<Pathology> getAllPathologies();
    Optional<Pathology> getPathologyByName(String name);
    PathologyDTO convertToDTO(Pathology pathology);
    List<PathologyDTO> convertToDTOList(List<Pathology> pathologies);
}
