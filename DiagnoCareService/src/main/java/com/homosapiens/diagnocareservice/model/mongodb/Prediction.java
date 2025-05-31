package com.diagnocare.apidiagnocareservice.model.mongodb;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "predictions")
public class Prediction {
    @Id
    private String predictionId;
    private Float accuracyRate;
    private String recommendation;
    private String predictedDisease;
    private String inputData;
    private LocalDateTime createdAt;
}