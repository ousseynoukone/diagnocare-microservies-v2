package com.homosapiens.diagnocareservice.model.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "specializations")
public class Specialization {

    @Id
    @GeneratedValue
    private Long id;
    @Column(length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;



}