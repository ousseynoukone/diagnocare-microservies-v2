package com.homosapiens.diagnocareservice.service;

import com.homosapiens.diagnocareservice.core.exception.AppException;
import com.homosapiens.diagnocareservice.model.entity.appointment.Appointment;
import com.homosapiens.diagnocareservice.model.entity.appointment.AppointmentStatus;
import com.homosapiens.diagnocareservice.model.entity.appointment.AppointmentType;
import com.homosapiens.diagnocareservice.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;

    public Appointment createAppointment(Appointment appointment) {
        validateAppointmentTime(appointment);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        return appointmentRepository.save(appointment);
    }

    @Transactional(readOnly = true)
    public List<Appointment> findAllAppointments() {
        return appointmentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Appointment findAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Appointment not found with id: " + id));
    }

    public Appointment updateAppointment(Long id, Appointment appointmentDetails) {
        Appointment appointment = findAppointmentById(id);
        
        // Only allow updates if appointment is not completed or cancelled
        if (appointment.getStatus() == AppointmentStatus.COMPLETED || 
            appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new AppException(HttpStatus.BAD_REQUEST, 
                "Cannot update appointment that is " + appointment.getStatus().name().toLowerCase());
        }

        validateAppointmentTime(appointmentDetails);

        appointment.setStartTime(appointmentDetails.getStartTime());
        appointment.setEndTime(appointmentDetails.getEndTime());
        appointment.setReason(appointmentDetails.getReason());
        appointment.setType(appointmentDetails.getType());
        appointment.setAppointmentDate(appointmentDetails.getAppointmentDate());

        return appointmentRepository.save(appointment);
    }

    public void deleteAppointment(Long id) {
        Appointment appointment = findAppointmentById(id);
        
        // Only allow deletion if appointment is not completed
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Cannot delete completed appointment");
        }
        
        appointmentRepository.deleteById(id);
    }

    public Appointment updateAppointmentStatus(Long id, AppointmentStatus newStatus) {
        Appointment appointment = findAppointmentById(id);
        
        // Validate status transition
        validateStatusTransition(appointment.getStatus(), newStatus);
        
        appointment.setStatus(newStatus);
        return appointmentRepository.save(appointment);
    }

    @Transactional(readOnly = true)
    public List<Appointment> findAppointmentsByDoctorId(Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId);
    }

    @Transactional(readOnly = true)
    public List<Appointment> findAppointmentsByPatientId(Long patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    @Transactional(readOnly = true)
    public List<Appointment> findAppointmentsByStatus(AppointmentStatus status) {
        return appointmentRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Appointment> findAppointmentsByType(AppointmentType type) {
        return appointmentRepository.findByType(type);
    }

    @Transactional(readOnly = true)
    public List<Appointment> findDoctorAppointmentsInDateRange(Long doctorId, LocalDateTime start, LocalDateTime end) {
        return appointmentRepository.findByDoctorIdAndStartTimeBetween(doctorId, start, end);
    }

    @Transactional(readOnly = true)
    public List<Appointment> findPatientAppointmentsInDateRange(Long patientId, LocalDateTime start, LocalDateTime end) {
        return appointmentRepository.findByPatientIdAndStartTimeBetween(patientId, start, end);
    }

    private void validateAppointmentTime(Appointment appointment) {
        if (appointment.getStartTime() == null || appointment.getEndTime() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Start time and end time are required");
        }

        if (appointment.getStartTime().isAfter(appointment.getEndTime())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Start time must be before end time");
        }

        if (appointment.getStartTime().isBefore(LocalDateTime.now())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Cannot create appointment in the past");
        }

        // Check for overlapping appointments
        List<Appointment> overlappingAppointments = appointmentRepository
                .findByDoctorIdAndStartTimeBetween(
                    appointment.getDoctor().getId(),
                    appointment.getStartTime(),
                    appointment.getEndTime()
                );

        if (!overlappingAppointments.isEmpty()) {
            throw new AppException(HttpStatus.CONFLICT, "Doctor has overlapping appointments in this time slot");
        }
    }

    private void validateStatusTransition(AppointmentStatus currentStatus, AppointmentStatus newStatus) {
        if (currentStatus == AppointmentStatus.COMPLETED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Cannot change status of completed appointment");
        }

        if (currentStatus == AppointmentStatus.CANCELLED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Cannot change status of cancelled appointment");
        }

        // Add more specific status transition rules if needed
        if (currentStatus == AppointmentStatus.SCHEDULED && newStatus == AppointmentStatus.COMPLETED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Cannot mark scheduled appointment as completed");
        }
    }
} 