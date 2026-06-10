package com.example.labmanage.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "schedule_notice")
public class ScheduleNoticeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String bizType;

    @Column(nullable = false)
    private Long bizId;

    @Column(nullable = false)
    private Long senderId;

    @Column(nullable = false)
    private Long receiverId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false, length = 32)
    private String noticeType;

    @Column(nullable = false, length = 16)
    private String readStatus;

    @Column(nullable = false)
    private LocalDateTime sendTime;

    private LocalDateTime readTime;

    @Column(nullable = false)
    private Boolean deleted;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (deleted == null) {
            deleted = false;
        }
        if (sendTime == null) {
            sendTime = LocalDateTime.now();
        }
        createdAt = LocalDateTime.now();
    }
}
