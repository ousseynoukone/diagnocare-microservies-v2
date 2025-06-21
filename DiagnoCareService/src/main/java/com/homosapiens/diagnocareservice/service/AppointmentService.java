package com.homosapiens.diagnocareservice.service;

import com.homosapiens.diagnocareservice.model.entity.appointment.Appointment;
import com.homosapiens.diagnocareservice.model.entity.appointment.AppointmentStatus;
import com.homosapiens.diagnocareservice.model.entity.appointment.AppointmentType;
import com.homosapiens.diagnocareservice.model.entity.dtos.AppointmentRequestDto;
import com.homosapiens.diagnocareservice.model.entity.dtos.AppointmentResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

public interface AppointmentService {
    AppointmentResponseDto createAppointment(AppointmentRequestDto appointment);
    AppointmentResponseDto updateAppointment(AppointmentRequestDto appointment, Long id);
    Page<AppointmentResponseDto> findAllAppointments(Pageable pageable);
    AppointmentResponseDto findAppointmentById(Long id);
    AppointmentResponseDto updateAppointment(Long id, AppointmentRequestDto appointmentDetails);
    void deleteAppointment(Long id);
    AppointmentResponseDto updateAppointmentStatus(Long id, AppointmentStatus newStatus);
    List<AppointmentResponseDto> findAppointmentsByDoctorId(Long doctorId);
    List<AppointmentResponseDto> findAppointmentsByPatientId(Long patientId);
    List<AppointmentResponseDto> findAppointmentsByStatus(AppointmentStatus status);
    List<AppointmentResponseDto> findAppointmentsByType(AppointmentType type);
    AppointmentResponseDto cancelAppointment(Long id);
    @Transactional(readOnly = true)
    List<AppointmentResponseDto> findDoctorAppointmentsInDateRange(Long doctorId, LocalTime start, LocalTime end);

    @Transactional(readOnly = true)
    List<AppointmentResponseDto> findPatientAppointmentsInDateRange(Long patientId, LocalTime start, LocalTime end);

    // Utility method to handle appointments with inactive slots
    List<AppointmentResponseDto> findAppointmentsWithInactiveSlots();
    
    // Method to fix appointments with inactive slots by canceling them
    void fixAppointmentsWithInactiveSlots();
}