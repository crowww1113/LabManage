package com.example.labmanage.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 借还通知实体类
 */
@Getter
@Setter
@Entity
@Table(name = "borrow_notification")
public class BorrowNotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 借还记录ID */
    @Column(name = "borrow_record_id", nullable = false)
    private Long borrowRecordId;

    /** 接收人ID */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 通知类型: 归还提醒/逾期提醒/续借审批/归还验收通知 */
    @Column(name = "type", nullable = false, length = 32)
    private String type;

    /** 通知标题 */
    @Column(name = "title", nullable = false, length = 128)
    private String title;

    /** 通知内容 */
    @Column(name = "content", nullable = false, length = 500)
    private String content;

    /** 是否已读 */
    @Column(name = "is_read")
    private Boolean isRead = false;

    /** 发送时间 */
    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (sentAt == null) {
            sentAt = LocalDateTime.now();
        }
    }
}
