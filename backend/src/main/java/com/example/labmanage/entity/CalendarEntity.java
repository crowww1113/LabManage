package com.example.labmanage.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "sys_calendar")
public class CalendarEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "term_id", nullable = false)
    private Long termId; // 学期ID

    @Column(nullable = false)
    private LocalDate date; // 日期

    @Column(nullable = false)
    private Integer week; // 教学周

    @Column(name = "is_holiday")
    private Boolean isHoliday; // 是否节假日

    @Column(name = "holiday_name", length = 64)
    private String holidayName; // 节假日名称

    @Column(name = "is_lab_open")
    private Boolean isLabOpen; // 是否实验室开放日

    @Column(length = 256)
    private String schedule; // 当日安排

    @Column(name = "created_at", updatable = false)
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