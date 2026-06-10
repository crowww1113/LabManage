package com.example.labmanage.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrgResponse {
    private Long id;
    private String code;
    private String name;
    private String level;
    private Long parentId;
    private String leader;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
