package com.homosapiens.diagnocareservice.controller;

import com.homosapiens.diagnocareservice.model.entity.appointment.AppointmentStatus;
import com.homosapiens.diagnocareservice.model.entity.appointment.AppointmentType;
import com.homosapiens.diagnocareservice.model.entity.dtos.AppointmentRequestDto;
import com.homosapiens.diagnocareservice.model.entity.dtos.AppointmentResponseDto;
import com.homosapiens.diagnocareservice.model.mapper.AppointmentMapper;
import com.homosapiens.diagnocareservice.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/appointments")
public class AppointmentController {
    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<AppointmentResponseDto> createAppointment(@Valid @RequestBody AppointmentRequestDto appointment) {
        return new ResponseEntity<>(appointmentService.createAppointment(appointment), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<AppointmentResponseDto>> getAllAppointments(@PageableDefault(size = 5, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(appointmentService.findAllAppointments(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponseDto> getAppointmentById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.findAppointmentById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResponseDto> updateAppointment(@PathVariable Long id, @Valid @RequestBody AppointmentRequestDto appointment) {
        return ResponseEntity.ok(appointmentService.updateAppointment(id, appointment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AppointmentResponseDto> updateAppointmentStatus(
            @PathVariable Long id,
            @RequestParam AppointmentStatus status) {
        return ResponseEntity.ok(appointmentService.updateAppointmentStatus(id, status));
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<AppointmentResponseDto>> getAppointmentsByDoctorId(@PathVariable Long doctorId) {
        return ResponseEntity.ok(appointmentService.findAppointmentsByDoctorId(doctorId));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AppointmentResponseDto>> getAppointmentsByPatientId(@PathVariable Long patientId) {
        return ResponseEntity.ok(appointmentService.findAppointmentsByPatientId(patientId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<AppointmentResponseDto>> getAppointmentsByStatus(@PathVariable AppointmentStatus status) {
        return ResponseEntity.ok(appointmentService.findAppointmentsByStatus(status));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<AppointmentResponseDto>> getAppointmentsByType(@PathVariable AppointmentType type) {
        return ResponseEntity.ok(appointmentService.findAppointmentsByType(type));
    }

    @GetMapping("/doctor/{doctorId}/date-range")
    public ResponseEntity<List<AppointmentResponseDto>> getDoctorAppointmentsInDateRange(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalTime end) {
        return ResponseEntity.ok(appointmentService.findDoctorAppointmentsInDateRange(doctorId, start, end));
    }

    @GetMapping("/patient/{patientId}/date-range")
    public ResponseEntity<List<AppointmentResponseDto>> getPatientAppointmentsInDateRange(
            @PathVariable Long patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalTime end) {
        return ResponseEntity.ok(appointmentService.findPatientAppointmentsInDateRange(patientId, start, end));
    }

    @PostMapping("cancel-appointment/{appointmentId}")
    public ResponseEntity<AppointmentResponseDto> cancelAppointment(@PathVariable Long appointmentId) {

        AppointmentResponseDto appointmentResponseDto = appointmentService.cancelAppointment(appointmentId) ;
        return ResponseEntity.ok().body(appointmentResponseDto);
    }
} 