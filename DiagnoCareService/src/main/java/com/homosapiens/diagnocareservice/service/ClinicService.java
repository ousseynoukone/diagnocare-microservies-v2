package com.homosapiens.diagnocareservice.service;

import com.homosapiens.diagnocareservice.model.entity.Clinic;
import java.util.List;

public interface ClinicService {
    Clinic createClinic(Clinic clinic);
    List<Clinic> findAllClinics();
    Clinic findClinicById(Long id);
    Clinic updateClinic(Long id, Clinic clinicDetails);
    void deleteClinic(Long id);
    List<Clinic> findClinicsByCity(String city);
    List<Clinic> searchClinicsByName(String name);
    List<Clinic> findClinicsByUserId(Long userId);
} 