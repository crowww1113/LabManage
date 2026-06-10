package com.example.labmanage.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScheduleOperationLogDTO {
    private Long id;
    private String bizType;
    private Long bizId;
    private String operationType;
    private Long operatorId;
    private String operatorRoleCode;
    private String beforeStatus;
    private String afterStatus;
    private String operationContent;
    private String operationResult;
    private String ipAddress;
    private String status;
    private LocalDateTime createdAt;
}
