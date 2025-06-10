package com.homosapiens.diagnocareservice.repository;

import com.homosapiens.diagnocareservice.model.entity.appointment.Appointment;
import com.homosapiens.diagnocareservice.model.entity.appointment.AppointmentStatus;
import com.homosapiens.diagnocareservice.model.entity.appointment.AppointmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByDoctorId(Long doctorId);
    List<Appointment> findByPatientId(Long patientId);
    List<Appointment> findByStatus(AppointmentStatus status);
    List<Appointment> findByType(AppointmentType type);
    List<Appointment> findByPatientIdAndSlot_FromTimeBetween(Long doctorId, LocalTime start, LocalTime end);
    List<Appointment> findByDoctorIdAndSlot_FromTimeBetween(Long patientId, LocalTime start, LocalTime end);
    List<Appointment> findByDoctorIdAndStatus(Long doctorId, AppointmentStatus status);
    List<Appointment> findByPatientIdAndStatus(Long patientId, AppointmentStatus status);
}
