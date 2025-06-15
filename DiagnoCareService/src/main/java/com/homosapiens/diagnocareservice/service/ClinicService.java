package com.homosapiens.diagnocareservice.service;

import com.homosapiens.diagnocareservice.dto.request.ClinicRequestDTO;
import com.homosapiens.diagnocareservice.dto.response.ClinicResponseDTO;

import java.util.List;

public interface ClinicService {
    ClinicResponseDTO createClinic(ClinicRequestDTO clinic);
    List<ClinicResponseDTO> findAllClinics();
    ClinicResponseDTO findClinicById(Long id);
    ClinicResponseDTO updateClinic(Long id, ClinicRequestDTO clinic);
    void deleteClinic(Long id);
    List<ClinicResponseDTO> findClinicsByCity(String city);
    List<ClinicResponseDTO> searchClinicsByName(String name);
    List<ClinicResponseDTO> findClinicsByUserId(Long userId);
} 