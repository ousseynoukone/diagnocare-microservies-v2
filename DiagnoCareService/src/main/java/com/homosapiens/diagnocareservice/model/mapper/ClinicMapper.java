package com.homosapiens.diagnocareservice.model.mapper;

import com.homosapiens.diagnocareservice.dto.request.ClinicRequestDTO;
import com.homosapiens.diagnocareservice.dto.response.ClinicResponseDTO;
import com.homosapiens.diagnocareservice.model.entity.Clinic;
import com.homosapiens.diagnocareservice.model.entity.User;
import org.springframework.stereotype.Component;

@Component
public class ClinicMapper {
    
    public Clinic toEntity(ClinicRequestDTO dto, User user) {
        Clinic clinic = new Clinic();
        clinic.setName(dto.getName());
        clinic.setAddress(dto.getAddress());
        clinic.setCity(dto.getCity());
        clinic.setPostalCode(dto.getPostalCode());
        clinic.setPhoneNumber(dto.getPhoneNumber());
        clinic.setUser(user);
        return clinic;
    }

    public ClinicResponseDTO toResponseDTO(Clinic clinic) {
        ClinicResponseDTO dto = new ClinicResponseDTO();
        dto.setId(clinic.getId());
        dto.setName(clinic.getName());
        dto.setAddress(clinic.getAddress());
        dto.setCity(clinic.getCity());
        dto.setPostalCode(clinic.getPostalCode());
        dto.setPhoneNumber(clinic.getPhoneNumber());
        dto.setCreatedAt(clinic.getCreatedAt());
        
        if (clinic.getUser() != null) {
            ClinicResponseDTO.UserDTO userDTO = new ClinicResponseDTO.UserDTO();
            User user = clinic.getUser();
            userDTO.setId(user.getId());
            userDTO.setFirstName(user.getFirstName());
            userDTO.setLastName(user.getLastName());
            userDTO.setEmail(user.getEmail());
            userDTO.setPhoneNumber(user.getPhoneNumber());
            dto.setUser(userDTO);
        }
        
        return dto;
    }
} 