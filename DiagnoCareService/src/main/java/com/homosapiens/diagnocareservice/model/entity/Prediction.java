package com.homosapiens.diagnocareservice.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(name = "predictions")
public class Prediction extends BaseEntity {

    @Column(name = "global_score", precision = 10, scale = 2)
    private BigDecimal bestScore;

    @Column(name = "pdf_report_url", length = 500)
    private String pdfReportUrl;

    @Column(name = "is_red_alert")
    private Boolean isRedAlert = false;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_symptome", nullable = false)
    private SessionSymptom sessionSymptom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pre_id_prediction")
    private Prediction previousPrediction;

    @OneToMany(mappedBy = "previousPrediction", cascade = CascadeType.ALL)
    private List<Prediction> subsequentPredictions;

    @OneToMany(mappedBy = "prediction", cascade = CascadeType.ALL)
    private List<PathologyResult> pathologyResults;

    @OneToMany(mappedBy = "prediction", cascade = CascadeType.ALL)
    private List<Report> reports;
}
