package com.homosapiens.diagnocareservice.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(name = "medecins")
public class Doctor extends BaseEntity {

    @Column(name = "specialist_label", length = 255, nullable = false)
    private String specialistLabel;

    @Column(name = "specialist_score", precision = 10, scale = 2)
    private BigDecimal specialistScore;

    @Column(columnDefinition = "TEXT")
    private String description;
}
