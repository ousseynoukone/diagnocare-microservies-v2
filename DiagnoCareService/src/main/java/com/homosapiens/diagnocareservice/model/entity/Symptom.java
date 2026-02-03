package com.homosapiens.diagnocareservice.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(name = "symptomes")
public class Symptom extends BaseEntity {

    @Column(name = "label", length = 500)
    private String label;

    @Column(name = "symptom_label_id")
    private Long symptomLabelId;
}
