package com.homosapiens.diagnocareservice.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(name = "resultat_pathologie")
public class PathologyResult extends BaseEntity {

    @Column(name = "disease_score", precision = 10, scale = 2)
    private BigDecimal diseaseScore;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "localized_disease_name", length = 255)
    private String localizedDiseaseName;

    @Column(name = "localized_specialist_label", length = 255)
    private String localizedSpecialistLabel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pathologie_id", nullable = false)
    private Pathology pathology;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medecin_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_prediction", nullable = false)
    private Prediction prediction;
}
