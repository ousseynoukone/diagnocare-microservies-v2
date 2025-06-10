package com.homosapiens.diagnocareservice.model.entity.dtos;

import com.homosapiens.diagnocareservice.model.entity.appointment.AppointmentType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AppointmentRequestDto {
    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotNull(message = "Schedule slot ID is required")
    private Long scheduleSlotId;

    private String reason;

    @NotNull(message = "Appointment type is required")
    private AppointmentType type;
} 