package com.example.labmanage.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "schedule_operation_log")
public class ScheduleOperationLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String bizType;

    @Column(nullable = false)
    private Long bizId;

    @Column(nullable = false, length = 32)
    private String operationType;

    @Column(nullable = false)
    private Long operatorId;

    @Column(nullable = false, length = 64)
    private String operatorRoleCode;

    @Column(length = 32)
    private String beforeStatus;

    @Column(length = 32)
    private String afterStatus;

    @Column(nullable = false, length = 1000)
    private String operationContent;

    @Column(length = 500)
    private String operationResult;

    @Column(length = 64)
    private String ipAddress;

    @Column(nullable = false, length = 16)
    private String status;

    @Column(nullable = false)
    private Boolean deleted;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (deleted == null) {
            deleted = false;
        }
        createdAt = LocalDateTime.now();
    }
}
