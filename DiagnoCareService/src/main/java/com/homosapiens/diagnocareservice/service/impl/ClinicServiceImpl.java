package com.homosapiens.diagnocareservice.service.impl;

import com.homosapiens.diagnocareservice.core.exception.AppException;
import com.homosapiens.diagnocareservice.model.entity.Clinic;
import com.homosapiens.diagnocareservice.repository.ClinicRepository;
import com.homosapiens.diagnocareservice.service.ClinicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ClinicServiceImpl implements ClinicService {
    private final ClinicRepository clinicRepository;

    @Override
    public Clinic createClinic(Clinic clinic) {
        if (clinicRepository.existsByPhoneNumber(clinic.getPhoneNumber())) {
            throw new AppException(HttpStatus.CONFLICT, "Clinic with this phone number already exists");
        }
        return clinicRepository.save(clinic);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Clinic> findAllClinics() {
        return clinicRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Clinic findClinicById(Long id) {
        return clinicRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Clinic not found with id: " + id));
    }

    @Override
    public Clinic updateClinic(Long id, Clinic clinicDetails) {
        Clinic clinic = findClinicById(id);

        if (!clinic.getPhoneNumber().equals(clinicDetails.getPhoneNumber()) &&
            clinicRepository.existsByPhoneNumber(clinicDetails.getPhoneNumber())) {
            throw new AppException(HttpStatus.CONFLICT, "Phone number already in use");
        }

        clinic.setName(clinicDetails.getName());
        clinic.setAddress(clinicDetails.getAddress());
        clinic.setCity(clinicDetails.getCity());
        clinic.setPostalCode(clinicDetails.getPostalCode());
        clinic.setPhoneNumber(clinicDetails.getPhoneNumber());
        clinic.setUser(clinicDetails.getUser());

        return clinicRepository.save(clinic);
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
    public List<Clinic> findClinicsByCity(String city) {
        return clinicRepository.findByCity(city);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Clinic> searchClinicsByName(String name) {
        return clinicRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Clinic> findClinicsByUserId(Long userId) {
        return clinicRepository.findByUserId(userId);
    }
} 