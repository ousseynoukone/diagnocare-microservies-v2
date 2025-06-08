package com.homosapiens.diagnocareservice.service;

import com.homosapiens.diagnocareservice.model.entity.appointment.Appointment;
import com.homosapiens.diagnocareservice.model.entity.appointment.AppointmentStatus;
import com.homosapiens.diagnocareservice.model.entity.appointment.AppointmentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentService {
    Appointment createAppointment(Appointment appointment);
    Page<Appointment> findAllAppointments(Pageable pageable);
    Appointment findAppointmentById(Long id);
    Appointment updateAppointment(Long id, Appointment appointmentDetails);
    void deleteAppointment(Long id);
    Appointment updateAppointmentStatus(Long id, AppointmentStatus newStatus);
    List<Appointment> findAppointmentsByDoctorId(Long doctorId);
    List<Appointment> findAppointmentsByPatientId(Long patientId);
    List<Appointment> findAppointmentsByStatus(AppointmentStatus status);
    List<Appointment> findAppointmentsByType(AppointmentType type);
    List<Appointment> findDoctorAppointmentsInDateRange(Long doctorId, LocalDateTime start, LocalDateTime end);
    List<Appointment> findPatientAppointmentsInDateRange(Long patientId, LocalDateTime start, LocalDateTime end);
} 