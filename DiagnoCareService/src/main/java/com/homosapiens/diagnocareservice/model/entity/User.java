package com.homosapiens.diagnocareservice.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String firstName;

    @Column(length = 100)
    private String lastName;

    private LocalDate birthDate;

    private Boolean gender;

    @Column(length = 255, unique = true)
    private String email;

    @Column(length = 255)
    private String address;

    @Column(length = 13)
    private String phoneNumber;

    private Boolean isActive;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    private List<Role> roles = new ArrayList<>();

    // Doctor specific fields
    @Column(length = 255)
    private String stripeCustomerId;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Clinic clinic;

    @Column(length = 11)
    private String npi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialization_id")
    private Specialization specialization;

    // Patient specific fields
    @Column(length = 13)
    private String healthAssuranceNumber;
    
    @Column(length = 50)
    private String credit;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;

    @LastModifiedDate
    @Column(updatable = false, name = "updated_at")
    private Date updatedAt;
} 