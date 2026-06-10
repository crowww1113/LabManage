package com.example.labmanage.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class OperationLogDTO {
    private Long id;
    private Long operatorId;
    private String operatorName;
    private String module;
    private String operationType;
    private String content;
    private String ipAddress;
    private LocalDateTime createTime;
}
