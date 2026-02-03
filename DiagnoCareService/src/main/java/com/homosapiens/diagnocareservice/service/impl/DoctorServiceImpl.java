package com.homosapiens.diagnocareservice.service.impl;

import com.homosapiens.diagnocareservice.dto.DoctorDTO;
import com.homosapiens.diagnocareservice.model.entity.Doctor;
import com.homosapiens.diagnocareservice.repository.DoctorRepository;
import com.homosapiens.diagnocareservice.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;

    @Override
    public Doctor createDoctor(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    @Override
    public Doctor updateDoctor(Long id, Doctor doctor) {
        if (doctorRepository.existsById(id)) {
            doctor.setId(id);
            return doctorRepository.save(doctor);
        }
        throw new RuntimeException("Doctor not found with id: " + id);
    }

    @Override
    public void deleteDoctor(Long id) {
        doctorRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Doctor> getDoctorById(Long id) {
        return doctorRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Doctor> searchDoctorsBySpecialty(String specialty) {
        return doctorRepository.findBySpecialistLabelContainingIgnoreCase(specialty);
    }

    @Override
    public DoctorDTO convertToDTO(Doctor doctor) {
        DoctorDTO dto = new DoctorDTO();
        dto.setId(doctor.getId());
        dto.setSpecialistLabel(doctor.getSpecialistLabel());
        dto.setSpecialistScore(doctor.getSpecialistScore());
        dto.setDescription(doctor.getDescription());
        return dto;
    }

    @Override
    public List<DoctorDTO> convertToDTOList(List<Doctor> doctors) {
        return doctors.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}
