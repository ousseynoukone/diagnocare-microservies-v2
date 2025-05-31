package com.homosapiens.diagnocareservice.model.mongodb;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "files")
public class File {
    @Id
    private String fileId;
    private String filePath;
    private String fileName;
    private String fileType;
    private Double fileSize;
}