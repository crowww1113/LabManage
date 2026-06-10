package com.example.labmanage.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "sys_permission")
public class PermissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "module_name", nullable = false, length = 64)
    private String module;

    @Column(name = "permission_name", nullable = false, length = 128)
    private String name;

    @Column(name = "action_type", nullable = false, length = 20)
    private String action;

    @Column(name = "permission_code", nullable = false, length = 128, unique = true)
    private String code;

    @Column(nullable = false, length = 10)
    private String status;

    @Column(length = 500)
    private String description;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
