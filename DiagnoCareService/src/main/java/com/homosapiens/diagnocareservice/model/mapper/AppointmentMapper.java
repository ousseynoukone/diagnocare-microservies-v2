package com.homosapiens.diagnocareservice.model.mapper;

import com.homosapiens.diagnocareservice.model.entity.appointment.Appointment;
import com.homosapiens.diagnocareservice.model.entity.appointment.ScheduleSlot;
import com.homosapiens.diagnocareservice.model.entity.dtos.AppointmentRequestDto;
import com.homosapiens.diagnocareservice.model.entity.dtos.AppointmentResponseDto;
import com.homosapiens.diagnocareservice.model.entity.dtos.ScheduleSlotResponseDto;
import com.homosapiens.diagnocareservice.model.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {

    public Appointment toEntity(AppointmentRequestDto dto, User doctor, User patient, ScheduleSlot slot) {
        Appointment appointment = new Appointment();
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setSlot(slot);
        appointment.setReason(dto.getReason());
        appointment.setType(dto.getType());
        return appointment;
    }

    public AppointmentResponseDto toDto(Appointment appointment) {
        AppointmentResponseDto dto = new AppointmentResponseDto();
        dto.setId(appointment.getId());
        dto.setDoctorId(appointment.getDoctor().getId());
        dto.setPatientId(appointment.getPatient().getId());
        dto.setSlot(ScheduleSlotResponseDto.fromEntity(appointment.getSlot()));
        dto.setReason(appointment.getReason());
        dto.setType(appointment.getType());
        dto.setStatus(appointment.getStatus());
        dto.setCreatedAt(appointment.getCreatedAt());
        dto.setUpdatedAt(appointment.getUpdatedAt());
        return dto;
    }
} 