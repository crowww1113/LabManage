package com.example.labmanage.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String realName;
    private Long orgId;
    private String position;
    private String jobNo;
    private String status;
    private List<Long> roleIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
