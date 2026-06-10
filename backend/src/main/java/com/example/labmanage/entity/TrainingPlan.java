package com.example.labmanage.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "training_plan")
@Data
public class TrainingPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "course_id")
    private Integer courseId;

    @Column(name = "organization_mode")
    private String organizationMode;

    @Column(name = "training_place")
    private String trainingPlace;

    @Column(name = "training_objective")
    private String trainingObjective;

    @Column(name = "training_content")
    private String trainingContent;

    @Column(name = "training_method")
    private String trainingMethod;

    @Column(name = "assessment_method")
    private String assessmentMethod;

    @Column(name = "quality_measures")
    private String qualityMeasures;

    @Column(name = "exp_center_opinion")
    private String expCenterOpinion;

    @Column(name = "dept_opinion")
    private String deptOpinion;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}