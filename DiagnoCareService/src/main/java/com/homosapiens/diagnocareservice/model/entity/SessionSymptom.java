package com.homosapiens.diagnocareservice.model.entity;


import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@Entity(name = "session_symptomes")
public class SessionSymptome  extends BaseEntity{
    @ManyToOne
    private User user;

    @Column(nullable = false)
    @NotBlank(message = "Description ne doit pas etre nul")
    private String  description;




}
