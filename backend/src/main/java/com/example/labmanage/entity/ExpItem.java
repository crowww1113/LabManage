package com.example.labmanage.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "exp_item")
@Data
public class ExpItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "course_id")
    private Integer courseId;

    @Column(name = "item_name")
    private String itemName;

    @Column(name = "`hour`")
    private Integer hour;

    @Column(name = "exp_type")
    private String expType;

    @Column(name = "requirement")
    private String requirement;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}