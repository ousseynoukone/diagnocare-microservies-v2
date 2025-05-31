package com.homosapiens.diagnocareservice.model.mongodb;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "file_sharing")
public class FileSharing {
    @Id
    private String id;
    private long sharedWithId;
    private String filePath;
    private LocalDateTime expirationDate;
    private LocalDateTime sharedDate;
    private String status;
}