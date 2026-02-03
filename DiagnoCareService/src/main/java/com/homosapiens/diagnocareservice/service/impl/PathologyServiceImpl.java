package com.homosapiens.diagnocareservice.service.impl;

import com.homosapiens.diagnocareservice.dto.PathologyDTO;
import com.homosapiens.diagnocareservice.model.entity.Pathology;
import com.homosapiens.diagnocareservice.repository.PathologyRepository;
import com.homosapiens.diagnocareservice.service.PathologyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PathologyServiceImpl implements PathologyService {

    private final PathologyRepository pathologyRepository;

    @Override
    public Pathology createPathology(Pathology pathology) {
        return pathologyRepository.save(pathology);
    }

    @Override
    public Pathology updatePathology(Long id, Pathology pathology) {
        if (pathologyRepository.existsById(id)) {
            pathology.setId(id);
            return pathologyRepository.save(pathology);
        }
        throw new RuntimeException("Pathology not found with id: " + id);
    }

    @Override
    public void deletePathology(Long id) {
        pathologyRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Pathology> getPathologyById(Long id) {
        return pathologyRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pathology> getAllPathologies() {
        return pathologyRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Pathology> getPathologyByName(String name) {
        return pathologyRepository.findByPathologyName(name);
    }

    @Override
    public PathologyDTO convertToDTO(Pathology pathology) {
        PathologyDTO dto = new PathologyDTO();
        dto.setId(pathology.getId());
        dto.setPathologyName(pathology.getPathologyName());
        dto.setDescription(pathology.getDescription());
        return dto;
    }

    @Override
    public List<PathologyDTO> convertToDTOList(List<Pathology> pathologies) {
        return pathologies.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}
