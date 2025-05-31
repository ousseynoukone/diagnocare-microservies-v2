//package com.homosapiens.diagnocareservice.service;
//
//import com.homosapiens.diagnocareservice.model.entity.Clinic;
//import com.homosapiens.diagnocareservice.repository.ClinicRepository;
//import lombok.AllArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Optional;
//
//@Service
//@AllArgsConstructor
//public class ClinicService {
//
//    private final ClinicRepository clinicRepository;
//
//    // Method to create a clinic
//    public void createClinic(Clinic clinic) {
//        clinicRepository.save(clinic);
//    }
//
//    // Method to update a clinic
//    public void updateClinic(Clinic clinic) {
//        clinicRepository.save(clinic);
//    }
//
//    // Method to delete a clinic
//    public void deleteClinic(Clinic clinic) {
//        clinicRepository.delete(clinic);
//    }
//
//    // Get all clinics
//    public List<Clinic> findAllClinic() {
//        return clinicRepository.findAll();
//    }
//
//    // Find clinics by name
//    public List<Clinic> findClinicByName(String name) {
//        // Assuming you have a method in your repository like:
//        return clinicRepository.findByNameContainingIgnoreCase(name); // Search by name with case insensitivity
//    }
//
//    // Find clinic by ID
//    public Optional<Clinic> findClinicById(Long id) {
//        return clinicRepository.findById(id);
//    }
//}
