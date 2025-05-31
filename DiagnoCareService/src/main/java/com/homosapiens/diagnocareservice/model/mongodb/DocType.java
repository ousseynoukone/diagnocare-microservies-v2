package com.homosapiens.diagnocareservice.model.mongodb;

import jakarta.persistence.Column;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "doc_types")
public class DocType {
    @Id
    private String docTypeId;
    
    @Column(length = 254)
    private String label;
    
    @Column(columnDefinition = "TEXT")
    private String description;
} 