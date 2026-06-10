package com.example.labmanage.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "schedule_booking")
public class ScheduleBookingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64, unique = true)
    private String bookingNo;

    @Column(nullable = false)
    private Long termId;

    private Long teachingTaskId;

    private Long courseId;

    private Long clazzId;

    @Column(nullable = false)
    private Long teacherId;

    @Column(nullable = false, length = 128)
    private String buildingName;

    @Column(nullable = false, length = 64)
    private String roomNumber;

    @Column(nullable = false)
    private Integer dayOfWeek;

    @Column(length = 100)
    private String weekRange;

    @Column(length = 200)
    private String timeSlotInfo;

    @Column(nullable = false)
    private Integer studentCount;

    private Integer groupCount;

    private Integer studentsPerGroup;

    @Column(nullable = false, length = 500)
    private String experimentContent;

    @Column(length = 500)
    private String remark;

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

    @Column(nullable = false, length = 32)
    private String status;

    @Column(length = 500)
    private String rejectReason;

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
