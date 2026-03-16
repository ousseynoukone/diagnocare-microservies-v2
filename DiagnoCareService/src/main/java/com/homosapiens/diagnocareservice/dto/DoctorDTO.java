package com.homosapiens.diagnocareservice.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DoctorDTO {
    private Long id;
    private String specialistLabel;
    private BigDecimal specialistScore;
    private String description;
}
