package com.homosapiens.diagnocareservice.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;
import java.util.Date;

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

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Date updatedAt;

}