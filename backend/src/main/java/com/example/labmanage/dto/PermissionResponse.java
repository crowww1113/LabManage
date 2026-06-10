package com.example.labmanage.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PermissionResponse {
    private Long id;
    private String module;
    private String name;
    private String action;
    private String code;
    private String status;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
