package com.homosapiens.diagnocareservice.service.impl;

import com.homosapiens.diagnocareservice.core.exception.AppException;
import com.homosapiens.diagnocareservice.dto.request.ClinicRequestDTO;
import com.homosapiens.diagnocareservice.dto.response.ClinicResponseDTO;
import com.homosapiens.diagnocareservice.model.mapper.ClinicMapper;
import com.homosapiens.diagnocareservice.model.entity.Clinic;
import com.homosapiens.diagnocareservice.model.entity.User;
import com.homosapiens.diagnocareservice.model.entity.enums.RoleEnum;
import com.homosapiens.diagnocareservice.repository.ClinicRepository;
import com.homosapiens.diagnocareservice.service.ClinicService;
import com.homosapiens.diagnocareservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ClinicServiceImpl implements ClinicService {
    private final ClinicRepository clinicRepository;
    private final UserService userService;
    private final ClinicMapper clinicMapper;

    @Override
    public ClinicResponseDTO createClinic(ClinicRequestDTO clinicDTO) {
        if (clinicRepository.existsByPhoneNumber(clinicDTO.getPhoneNumber())) {
            throw new AppException(HttpStatus.CONFLICT, "Clinic with this phone number already exists");
        }

        User user = userService.getUserById(clinicDTO.getUserId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "User not found with id: " + clinicDTO.getUserId()));

        if (user.getRoles().stream().noneMatch(role -> role.getName() == RoleEnum.DOCTOR)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "User must be a doctor to create a clinic");
        }

        Clinic clinic = clinicMapper.toEntity(clinicDTO, user);
        Clinic savedClinic = clinicRepository.save(clinic);
        return clinicMapper.toResponseDTO(savedClinic);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClinicResponseDTO> findAllClinics() {
        return clinicRepository.findAll().stream()
                .map(clinicMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ClinicResponseDTO findClinicById(Long id) {
        Clinic clinic = clinicRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Clinic not found with id: " + id));
        return clinicMapper.toResponseDTO(clinic);
    }

    @Override
    public ClinicResponseDTO updateClinic(Long id, ClinicRequestDTO clinicDTO) {
        Clinic clinic = clinicRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Clinic not found with id: " + id));

        if (!clinic.getPhoneNumber().equals(clinicDTO.getPhoneNumber()) &&
            clinicRepository.existsByPhoneNumber(clinicDTO.getPhoneNumber())) {
            throw new AppException(HttpStatus.CONFLICT, "Phone number already in use");
        }

        User user = userService.getUserById(clinicDTO.getUserId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "User not found with id: " + clinicDTO.getUserId()));

        if (user.getRoles().stream().noneMatch(role -> role.getName() == RoleEnum.DOCTOR)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "User must be a doctor to update a clinic");
        }

        clinic.setName(clinicDTO.getName());
        clinic.setAddress(clinicDTO.getAddress());
        clinic.setCity(clinicDTO.getCity());
        clinic.setPostalCode(clinicDTO.getPostalCode());
        clinic.setPhoneNumber(clinicDTO.getPhoneNumber());
        clinic.setUser(user);

        Clinic updatedClinic = clinicRepository.save(clinic);
        return clinicMapper.toResponseDTO(updatedClinic);
    }

    @Override
    public void deleteClinic(Long id) {
        if (!clinicRepository.existsById(id)) {
            throw new AppException(HttpStatus.NOT_FOUND, "Clinic not found with id: " + id);
        }
        clinicRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClinicResponseDTO> findClinicsByCity(String city) {
        return clinicRepository.findByCity(city).stream()
                .map(clinicMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClinicResponseDTO> searchClinicsByName(String name) {
        return clinicRepository.findByNameContainingIgnoreCase(name).stream()
                .map(clinicMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClinicResponseDTO> findClinicsByUserId(Long userId) {
        return clinicRepository.findByUserId(userId).stream()
                .map(clinicMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
} 