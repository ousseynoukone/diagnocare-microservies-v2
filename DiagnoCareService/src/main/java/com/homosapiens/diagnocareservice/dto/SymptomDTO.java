package com.homosapiens.diagnocareservice.dto;

import lombok.Data;

@Data
public class SymptomDTO {
    private Long id;
    private String label;
    private Long symptomLabelId;
}
