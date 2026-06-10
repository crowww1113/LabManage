package com.example.labmanage.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "sys_term")
public class TermEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32, unique = true)
    private String termName; // 学期名称：2025-2026学年第一学期

    @Column(nullable = false)
    private LocalDate startDate; // 学期开始日期

    @Column(nullable = false)
    private LocalDate endDate; // 学期结束日期

    private Integer totalWeeks; // 总周数

    @Column(length = 1024)
    private String holidayDates; // 节假日日期，JSON格式存储：["2025-10-01","2025-10-02"]

    @Column(name = "lab_open_days", length = 1024)
    private String labOpenDays; // 实验室开放日，JSON格式存储：["2025-09-01","2025-09-02"]

    // ===== 教学节点 =====
    @Column(name = "course_select_start")
    private LocalDate courseSelectStart; // 开始选课

    @Column(name = "course_adjust_start")
    private LocalDate courseAdjustStart; // 开始退补选

    @Column(name = "midterm_start")
    private LocalDate midtermStart; // 期中考试开始

    @Column(name = "final_start")
    private LocalDate finalStart; // 期末考试开始

    @Column(name = "makeup_start")
    private LocalDate makeupStart; // 补考开始

    @Column(name = "thesis_deadline")
    private LocalDate thesisDeadline; // 论文答辩截止

    // ===== 重要节点 =====
    @Column(name = "military_start")
    private LocalDate militaryStart; // 军训开始

    @Column(name = "sports_day")
    private LocalDate sportsDay; // 运动会

    @Column(name = "anniversary")
    private LocalDate anniversary; // 校庆

    @Column(name = "graduation_start")
    private LocalDate graduationStart; // 毕业季开始

    @Column(name = "enrollment_start")
    private LocalDate enrollmentStart; // 招生季开始

    // ===== 管理服务 =====
    @Column(name = "staff_training_start")
    private LocalDate staffTrainingStart; // 教职工培训开始

    @Column(name = "physical_start")
    private LocalDate physicalStart; // 学生体检开始

    @Column(name = "safety_education")
    private LocalDate safetyEducation; // 安全教育

    // ===== 校园活动 =====
    @Column(name = "job_fair")
    private LocalDate jobFair; // 招聘会

    @Column(name = "campus_open_day")
    private LocalDate campusOpenDay; // 校园开放日

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