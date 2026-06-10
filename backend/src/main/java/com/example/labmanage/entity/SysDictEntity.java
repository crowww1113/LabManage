package com.example.labmanage.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "sys_dict")
public class SysDictEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dict_type", nullable = false, length = 64)
    private String dictType;

    @Column(name = "dict_key", nullable = false, length = 64)
    private String dictKey;

    @Column(name = "dict_value", nullable = false, length = 255)
    private String dictValue;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(length = 255)
    private String remark;

    @Column(length = 16)
    private String status = "启用";

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
