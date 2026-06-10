package com.example.labmanage.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "sys_clazz")
public class ClazzEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String clazzCode; // 班级编号

    @Column(nullable = false, length = 128)
    private String clazzName; // 班级名称：计算机2022级1班

    @Column(nullable = false)
    private Long majorId; // 对应专业ID

    @Column(nullable = false)
    private Long deptId; // 对应部门ID

    @Column(length = 16)
    private String grade; // 年级：2022级

    private Long managerId; // 班级管理员ID（绑定用户ID）

    private Long headTeacherId; // 班主任ID（绑定用户ID）

    @Column(length = 10)
    private String status; // 状态：启用/停用

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