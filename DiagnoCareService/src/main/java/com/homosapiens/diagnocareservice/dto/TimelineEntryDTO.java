package com.homosapiens.diagnocareservice.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class TimelineEntryDTO {
    private Long predictionId;
    private String type;
    private String date;
    private List<String> symptoms;
    private BigDecimal score;
    private BigDecimal delta;
    private String outcome;
    private String status;
}
