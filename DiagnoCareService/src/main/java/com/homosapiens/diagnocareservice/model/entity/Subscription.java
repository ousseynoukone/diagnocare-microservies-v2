package com.homosapiens.diagnocareservice.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;
import java.util.Date;

@Data
@Entity
@Table(name = "subscriptions")
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255)
    private String subscriptionType;
    
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    
    @Column(length = 255)
    private String subscriptionStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private Plans plan;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Date updatedAt;
} 