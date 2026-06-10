package com.example.labmanage.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BorrowNotificationDTO {
    private Long id;
    private Long borrowRecordId;
    private Long userId;
    private String type;
    private String title;
    private String content;
    private Boolean isRead;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
    // 关联信息
    private String equipmentName;
    private String recordNo;
}
