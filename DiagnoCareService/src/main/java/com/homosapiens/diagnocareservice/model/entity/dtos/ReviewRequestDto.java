package com.homosapiens.diagnocareservice.model.entity.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequestDto {
    
    @Schema(example = "5", description = "Rating from 1 to 5")
    @JsonProperty("rating")
    private Integer rating;
    
    @Schema(example = "Great doctor, very professional and caring", description = "Optional comment about the experience")
    @JsonProperty("comment")
    private String comment;
    
    @Schema(example = "152", description = "ID of the doctor being reviewed")
    @JsonProperty("doctorId")
    private Long doctorId;
    
    @Schema(example = "252", description = "ID of the patient writing the review")
    @JsonProperty("patientId")
    private Long patientId;
    
    @Schema(example = "123", description = "ID of the appointment this review is for (optional)")
    @JsonProperty("appointmentId")
    private Long appointmentId;
} 