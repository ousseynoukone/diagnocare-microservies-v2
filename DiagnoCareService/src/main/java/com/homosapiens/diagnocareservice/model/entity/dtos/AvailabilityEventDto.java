package com.homosapiens.diagnocareservice.model.entity.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityEventDto {
    private Long availabilityId;
    private Long userId;
    private String eventType;
    private String message;
} 