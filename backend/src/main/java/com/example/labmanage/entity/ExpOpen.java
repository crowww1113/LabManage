package com.example.labmanage.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "exp_open")
@Data
public class ExpOpen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "task_id")
    private Integer taskId;

    @Column(name = "item_id")
    private Integer itemId;

    @Column(name = "week")
    private Integer week;

    @Column(name = "day_of_week")
    private Integer dayOfWeek;

    @Column(name = "class_section")
    private Integer classSection;

    @Column(name = "group_count")
    private Integer groupCount;

    @Column(name = "students_per_group")
    private Integer studentsPerGroup;

    @Column(name = "cycle_count")
    private Integer cycleCount;

    @Column(name = "requirement")
    private String requirement;

    @Column(name = "building_name")
    private String buildingName;

    @Column(name = "room_number")
    private String roomNumber;

    @Column(name = "is_opened")
    private Boolean isOpend;

    @Column(name = "not_opened_reason")
    private String notOpendReason;

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