package com.example.labmanage.entity;

import com.example.labmanage.enums.ScheduleRegistrationStatusEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "schedule_usage_record")
public class ScheduleUsageRecordEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long reservationId;

    private Long courseId;

    @Column(nullable = false)
    private Long clazzId;

    @Column(nullable = false)
    private Long timeSlotId;

    @Column(length = 128)
    private String department;

    private LocalDateTime fillTime;

    @Column(length = 128)
    private String labName;

    @Column(length = 128)
    private String buildingName;

    @Column(length = 64)
    private String roomNumber;

    private LocalDate usageDate;

    @Column(length = 200)
    private String courseOrProjectName;

    private Double plannedHours;

    @Column(length = 100)
    private String className;

    private Integer expectedAttendance;

    private Long reporterId;

    @Column(length = 100)
    private String reporterName;

    @Column(length = 200)
    private String experimentItemName;

    @Column(length = 100)
    private String experimentItemType;

    private Double actualHours;

    private Integer actualAttendance;

    @Column(length = 500)
    private String attendanceRecord = "无";

    @Column(length = 128)
    private String teachingStatus = "正常";

    @Column(length = 128)
    private String equipmentStatus = "正常";

    @Column(nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private ScheduleRegistrationStatusEnum recordStatus;

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
        if (attendanceRecord == null) {
            attendanceRecord = "无";
        }
        if (teachingStatus == null) {
            teachingStatus = "正常";
        }
        if (equipmentStatus == null) {
            equipmentStatus = "正常";
        }
        if (recordStatus == null) {
            recordStatus = ScheduleRegistrationStatusEnum.PENDING;
        }
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
