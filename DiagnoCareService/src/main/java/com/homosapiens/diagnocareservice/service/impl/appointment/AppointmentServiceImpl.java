package com.homosapiens.diagnocareservice.service.impl.appointment;

import com.homosapiens.diagnocareservice.core.exception.AppException;
import com.homosapiens.diagnocareservice.model.entity.appointment.Appointment;
import com.homosapiens.diagnocareservice.model.entity.appointment.AppointmentStatus;
import com.homosapiens.diagnocareservice.model.entity.appointment.AppointmentType;
import com.homosapiens.diagnocareservice.model.entity.appointment.ScheduleSlot;
import com.homosapiens.diagnocareservice.model.entity.User;
import com.homosapiens.diagnocareservice.model.entity.dtos.AppointmentRequestDto;
import com.homosapiens.diagnocareservice.model.entity.dtos.AppointmentResponseDto;
import com.homosapiens.diagnocareservice.model.mapper.AppointmentMapper;
import com.homosapiens.diagnocareservice.repository.AppointmentRepository;
import com.homosapiens.diagnocareservice.repository.ScheduleSlotRepository;
import com.homosapiens.diagnocareservice.service.AppointmentService;
import com.homosapiens.diagnocareservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AppointmentServiceImpl implements AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;
    private final UserService userService;
    private final ScheduleSlotRepository scheduleSlotRepository;

    @Override
    public AppointmentResponseDto createAppointment(AppointmentRequestDto requestDto) {
        User doctor = userService.getUserById(requestDto.getDoctorId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Doctor not found"));
        User patient = userService.getUserById(requestDto.getPatientId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Patient not found"));
        ScheduleSlot slot = scheduleSlotRepository.findById(requestDto.getScheduleSlotId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Schedule slot not found"));

        if (slot.isIsBooked()) {
            throw new AppException(HttpStatus.CONFLICT, "This slot is already booked");
        }

        Appointment appointment = appointmentMapper.toEntity(requestDto, doctor, patient, slot);
        validateAppointmentTime(appointment);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        
        // Mark the slot as booked
        slot.setIsBooked(true);
        scheduleSlotRepository.save(slot);
        
        Appointment savedAppointment = appointmentRepository.save(appointment);
        return appointmentMapper.toDto(savedAppointment);
    }

    @Override
    public AppointmentResponseDto updateAppointment(AppointmentRequestDto appointment, Long id) {

        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponseDto> findAllAppointments(Pageable pageable) {
        return appointmentRepository.findAll(pageable)
                .map(appointmentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponseDto findAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .map(appointmentMapper::toDto)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Appointment not found with id: " + id));
    }

    @Override
    public AppointmentResponseDto updateAppointment(Long id, AppointmentRequestDto appointmentDetails) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Appointment not found with id: " + id));
        
        if (appointment.getStatus() == AppointmentStatus.COMPLETED || 
            appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new AppException(HttpStatus.BAD_REQUEST, 
                "Cannot update appointment that is " + appointment.getStatus().name().toLowerCase());
        }

        User doctor = userService.getUserById(appointmentDetails.getDoctorId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Doctor not found"));
        User patient = userService.getUserById(appointmentDetails.getPatientId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Patient not found"));
        ScheduleSlot slot = scheduleSlotRepository.findById(appointmentDetails.getScheduleSlotId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Schedule slot not found"));

        Appointment updatedAppointment = appointmentMapper.toEntity(appointmentDetails, doctor, patient, slot);
        validateAppointmentTime(updatedAppointment);

        appointment.setSlot(updatedAppointment.getSlot());
        appointment.setReason(updatedAppointment.getReason());
        appointment.setType(updatedAppointment.getType());


        return appointmentMapper.toDto(appointmentRepository.save(appointment));
    }

    @Override
    public void deleteAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Appointment not found with id: " + id));
        appointmentRepository.delete(appointment);
    }

    @Override
    public AppointmentResponseDto updateAppointmentStatus(Long id, AppointmentStatus newStatus) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Appointment not found with id: " + id));
        
        validateStatusTransition(appointment.getStatus(), newStatus);
        appointment.setStatus(newStatus);
        
        return appointmentMapper.toDto(appointmentRepository.save(appointment));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDto> findAppointmentsByDoctorId(Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId).stream()
                .map(appointmentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDto> findAppointmentsByPatientId(Long patientId) {
        return appointmentRepository.findByPatientId(patientId).stream()
                .map(appointmentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDto> findAppointmentsByStatus(AppointmentStatus status) {
        return appointmentRepository.findByStatus(status).stream()
                .map(appointmentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDto> findAppointmentsByType(AppointmentType type) {
        return appointmentRepository.findByType(type).stream()
                .map(appointmentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public AppointmentResponseDto cancelAppointment(Long id) {
        Optional<Appointment> appointmentOptional = appointmentRepository.findById(id);
        if (appointmentOptional.isPresent()) {
            Appointment appointment = appointmentOptional.get();
            appointment.setStatus(AppointmentStatus.CANCELLED);
         return appointmentMapper.toDto( appointmentRepository.save(appointment))  ;
        }else {
            throw new AppException(HttpStatus.NOT_FOUND, "Appointment not found with id: " + id);
        }

    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDto> findDoctorAppointmentsInDateRange(Long doctorId, LocalTime start, LocalTime end) {
        return appointmentRepository.findByDoctorIdAndSlot_FromTimeBetween(doctorId, start, end).stream()
                .map(appointmentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDto> findPatientAppointmentsInDateRange(Long patientId, LocalTime start, LocalTime end) {
        return appointmentRepository.findByPatientIdAndSlot_FromTimeBetween(patientId, start, end).stream()
                .map(appointmentMapper::toDto)
                .collect(Collectors.toList());
    }

    private void validateAppointmentTime(Appointment appointment) {
        if (appointment.getSlot().getFromTime() == null || appointment.getSlot().getToTime() == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Start time and end time are required");
        }

        if (appointment.getSlot().getFromTime().isAfter(appointment.getSlot().getToTime())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Start time must be before end time");
        }

        if (appointment.getSlot().getFromTime().isBefore(LocalTime.now())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Cannot create appointment in the past");
        }

        List<Appointment> overlappingAppointments = appointmentRepository
                .findByDoctorIdAndSlot_FromTimeBetween(
                    appointment.getDoctor().getId(),
                    appointment.getSlot().getFromTime(),
                    appointment.getSlot().getToTime()
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

        if (currentStatus == AppointmentStatus.SCHEDULED && newStatus == AppointmentStatus.COMPLETED) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Cannot mark scheduled appointment as completed");
        }
    }
} 