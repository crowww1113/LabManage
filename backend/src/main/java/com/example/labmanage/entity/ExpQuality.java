package com.example.labmanage.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "exp_quality")
@Data
public class ExpQuality {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "task_id")
    private Integer taskId;

    @Column(name = "organization")
    private String organization;

    @Column(name = "course_name")
    private String courseName;

    @Column(name = "exp_hour")
    private Integer expHour;

    @Column(name = "independent_course")
    private Boolean independentCourse;

    @Column(name = "main_teacher")
    private String mainTeacher;

    @Column(name = "teacher_title")
    private String teacherTitle;

    @Column(name = "technician")
    private String technician;

    @Column(name = "technician_title")
    private String technicianTitle;

    @Column(name = "class_name")
    private String className;

    @Column(name = "student_count")
    private Integer studentCount;

    @Column(name = "planned_exp_count")
    private Integer plannedExpCount;

    @Column(name = "actual_exp_count")
    private Integer actualExpCount;

    @Column(name = "missed_exp_reason")
    private String missedExpReason;

    @Column(name = "assessment_method")
    private String assessmentMethod;

    @Column(name = "assessment_count")
    private Integer assessmentCount;

    @Column(name = "assessment_time")
    private String assessmentTime;

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
