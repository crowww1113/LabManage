package com.example.labmanage.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 设备分类实体类
 */
@Getter
@Setter
@Entity
@Table(name = "equipment_category")
public class EquipmentCategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 分类编码 */
    @Column(name = "code", nullable = false, length = 32, unique = true)
    private String code;

    /** 分类名称 */
    @Column(name = "name", nullable = false, length = 64)
    private String name;

    /** 父分类ID */
    @Column(name = "parent_id")
    private Long parentId;

    /** 排序 */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    /** 状态 */
    @Column(name = "status", length = 16)
    private String status = "启用";

    /** 描述 */
    @Column(name = "description", length = 256)
    private String description;

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
