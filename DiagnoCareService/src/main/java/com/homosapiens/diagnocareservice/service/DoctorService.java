package com.homosapiens.diagnocareservice.service;

import com.homosapiens.diagnocareservice.dto.DoctorDTO;
import com.homosapiens.diagnocareservice.model.entity.Doctor;

import java.util.List;
import java.util.Optional;

public interface DoctorService {
    Doctor createDoctor(Doctor doctor);
    Doctor updateDoctor(Long id, Doctor doctor);
    void deleteDoctor(Long id);
    Optional<Doctor> getDoctorById(Long id);
    List<Doctor> getAllDoctors();
    List<Doctor> searchDoctorsBySpecialty(String specialty);
    DoctorDTO convertToDTO(Doctor doctor);
    List<DoctorDTO> convertToDTOList(List<Doctor> doctors);
}
