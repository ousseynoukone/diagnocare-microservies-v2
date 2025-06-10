package com.homosapiens.diagnocareservice.model.entity.dtos;

import com.homosapiens.diagnocareservice.model.entity.appointment.AppointmentStatus;
import com.homosapiens.diagnocareservice.model.entity.appointment.AppointmentType;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppointmentResponseDto {
    private Long id;
    private Long doctorId;
    private Long patientId;
    private ScheduleSlotResponseDto slot;
    private String reason;
    private AppointmentType type;
    private AppointmentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 