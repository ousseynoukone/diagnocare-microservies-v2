package com.homosapiens.diagnocareservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Request body for creating a new prediction. Only userId and symptom labels are required.")
public class CreatePredictionRequestDTO {

    @NotNull(message = "User ID is required")
    @Schema(description = "ID of the user (patient)", example = "8", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;

    @NotEmpty(message = "At least one symptom label is required")
    @Schema(
            description = "List of symptom labels (e.g. chest_pain, breathlessness). Use underscores, lowercase.",
            example = "[\"chest_pain\", \"breathlessness\", \"sweating\", \"fatigue\", \"dizziness\", \"palpitations\"]",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private List<String> symptomLabels;
}
