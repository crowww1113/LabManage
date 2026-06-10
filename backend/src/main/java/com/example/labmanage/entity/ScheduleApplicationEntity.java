package com.example.labmanage.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "schedule_application")
public class ScheduleApplicationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64, unique = true)
    private String applicationNo;

    @Column
    private Long termId;

    @Column
    private Long teacherId; // 保留，通过Token解析填入

    @Column(length = 128)
    private String preferredBuildingName;

    @Column(length = 64)
    private String preferredRoomNumber;

    private Integer preferredWeekNo;

    private Integer preferredDayOfWeek;

    private LocalDate preferredDate;

    private Long preferredTimeSlotId;

    @Column(length = 32)
    private String applicationType;

    private Long teachingTaskId;

    @Column(length = 128)
    private String expectedLab;

    @Column(length = 64)
    private String targetWeeks;

    private Integer targetDayOfWeek;

    @Column(length = 32)
    private String targetTimeSlot;

    // 新增人性化字段
    @Column(length = 200)
    private String projectName;

    @Column(length = 50)
    private String projectCategory;

    @Column(length = 50)
    private String projectLeader;

    @Column(length = 20)
    private String contactPhone;

    @Column(length = 20)
    private String grade;

    @Column(length = 50)
    private String className;

    private Integer participantCount;

    @Column(precision = 5, scale = 1)
    private BigDecimal duration;

    @Column
    private Integer studentCount;

    private Integer groupCount;

    private Integer studentsPerGroup;

    @Column(length = 500)
    private String experimentContent;

    @Column(length = 500)
    private String remark;

    @Column(length = 500)
    private String experimentRequirement;

    @Column(length = 500)
    private String equipmentRequirement;

    @Column(length = 500)
    private String reviewComment;

    private Long reviewedBy;

    private LocalDateTime reviewedAt;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(nullable = false)
    private Boolean deleted;

    private Long createdBy;

    private Long updatedBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (deleted == null) {
            deleted = false;
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
