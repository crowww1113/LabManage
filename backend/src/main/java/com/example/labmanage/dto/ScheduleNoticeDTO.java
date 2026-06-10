package com.example.labmanage.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScheduleNoticeDTO {
    private Long id;
    private String bizType;
    private Long bizId;
    private Long senderId;
    private Long receiverId;
    private String title;
    private String content;
    private String noticeType;
    private String readStatus;
    private LocalDateTime sendTime;
    private LocalDateTime readTime;
}
