package com.homosapiens.diagnocareservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class SessionSymptomDTO {
    private Long id;
    private Long userId;
    private String rawDescription;
    private List<SymptomDTO> symptoms;
    private List<Long> predictionIds;
}
