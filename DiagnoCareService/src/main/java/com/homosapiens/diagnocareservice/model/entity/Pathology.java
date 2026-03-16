package com.homosapiens.diagnocareservice.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(name = "pathologies")
public class Pathology extends BaseEntity {

    @Column(name = "pathology_name", length = 255, nullable = false)
    private String pathologyName;

    @Column(columnDefinition = "TEXT")
    private String description;
}
