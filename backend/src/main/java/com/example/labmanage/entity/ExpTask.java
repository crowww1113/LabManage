package com.example.labmanage.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "exp_task")
@Data
public class ExpTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "term")
    private String term;

    @Column(name = "major")
    private String major;

    @Column(name = "class_id")
    private Integer classId;

    @Column(name = "student_count")
    private Integer studentCount;

    @Column(name = "student_level")
    private String studentLevel;

    @Column(name = "course_name")
    private String courseName;

    @Column(name = "course_type")
    private String courseType;

    @Column(name = "independent_course")
    private Boolean independentCourse;

    @Column(name = "total_exp_hour")
    private Integer totalExpHour;

    @Column(name = "current_exp_hour")
    private Integer currentExpHour;

    @Column(name = "total_practice_hour")
    private Integer totalPracticeHour;

    @Column(name = "current_practice_hour")
    private Integer currentPracticeHour;

    @Column(name = "total_training_hour")
    private Integer totalTrainingHour;

    @Column(name = "current_training_hour")
    private Integer currentTrainingHour;

    @Column(name = "organization")
    private String organization;

    @Column(name = "department")
    private String department;

    @Column(name = "teacher")
    private String teacher;

    @Column(name = "teacher_title")
    private String teacherTitle;

    @Column(name = "technician")
    private String technician;

    @Column(name = "technician_title")
    private String technicianTitle;

    @Column(name = "textbook_name")
    private String textbookName;

    @Column(name = "guidebook_name")
    private String guidebookName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Transient
    private Long termId;

    @Transient
    private String className;

    @Transient
    private String grade;

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
