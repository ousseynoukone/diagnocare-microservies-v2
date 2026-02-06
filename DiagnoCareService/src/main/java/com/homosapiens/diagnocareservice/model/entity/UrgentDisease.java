package com.homosapiens.diagnocareservice.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(name = "urgent_diseases")
public class UrgentDisease extends BaseEntity {

    @Column(name = "disease_name", length = 255, nullable = false, unique = true)
    private String diseaseName;
}
