package com.example.labmanage.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class NotificationDTO {
    private Long id;
    private Long receiverId;
    private String receiverName;
    private String type;
    private String title;
    private String content;
    private Boolean isRead;
    private LocalDateTime createTime;
}
