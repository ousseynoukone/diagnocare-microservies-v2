package com.homosapiens.diagnocareservice.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(name = "symptome_session")
public class SessionSymptom extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "raw_description", columnDefinition = "TEXT")
    private String rawDescription;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "symptome_session_symptome",
        joinColumns = @JoinColumn(name = "symptome_session_id"),
        inverseJoinColumns = @JoinColumn(name = "symptome_id")
    )
    private List<Symptom> symptoms;

    @OneToMany(mappedBy = "sessionSymptom", cascade = CascadeType.ALL)
    private List<Prediction> predictions;
}
