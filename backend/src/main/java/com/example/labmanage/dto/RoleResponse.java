package com.example.labmanage.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RoleResponse {
    private Long id;
    private String code;
    private String name;
    private Long parentRoleId;
    private Integer level;
    private String dataScope;
    private Long orgLimit;
    private List<String> permissions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
