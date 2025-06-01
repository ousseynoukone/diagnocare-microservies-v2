package com.homosapiens.diagnocareservice.controller;

import com.homosapiens.diagnocareservice.model.entity.appointment.Appointment;
import com.homosapiens.diagnocareservice.model.entity.appointment.AppointmentStatus;
import com.homosapiens.diagnocareservice.model.entity.appointment.AppointmentType;
import com.homosapiens.diagnocareservice.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/appointments")
public class AppointmentController {
    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<Appointment> createAppointment(@Valid @RequestBody Appointment appointment) {
        return new ResponseEntity<>(appointmentService.createAppointment(appointment), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Appointment>> getAllAppointments() {
        return ResponseEntity.ok(appointmentService.findAllAppointments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getAppointmentById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.findAppointmentById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Appointment> updateAppointment(@PathVariable Long id, @Valid @RequestBody Appointment appointment) {
        return ResponseEntity.ok(appointmentService.updateAppointment(id, appointment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Appointment> updateAppointmentStatus(
            @PathVariable Long id,
            @RequestParam AppointmentStatus status) {
        return ResponseEntity.ok(appointmentService.updateAppointmentStatus(id, status));
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<Appointment>> getAppointmentsByDoctorId(@PathVariable Long doctorId) {
        return ResponseEntity.ok(appointmentService.findAppointmentsByDoctorId(doctorId));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Appointment>> getAppointmentsByPatientId(@PathVariable Long patientId) {
        return ResponseEntity.ok(appointmentService.findAppointmentsByPatientId(patientId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Appointment>> getAppointmentsByStatus(@PathVariable AppointmentStatus status) {
        return ResponseEntity.ok(appointmentService.findAppointmentsByStatus(status));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Appointment>> getAppointmentsByType(@PathVariable AppointmentType type) {
        return ResponseEntity.ok(appointmentService.findAppointmentsByType(type));
    }

    @GetMapping("/doctor/{doctorId}/date-range")
    public ResponseEntity<List<Appointment>> getDoctorAppointmentsInDateRange(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(appointmentService.findDoctorAppointmentsInDateRange(doctorId, start, end));
    }

    @GetMapping("/patient/{patientId}/date-range")
    public ResponseEntity<List<Appointment>> getPatientAppointmentsInDateRange(
            @PathVariable Long patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(appointmentService.findPatientAppointmentsInDateRange(patientId, start, end));
    }
} 