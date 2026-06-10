package com.example.labmanage.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 消息通知实体类
 */
@Getter
@Setter
@Entity
@Table(name = "notification")
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 接收人ID */
    @Column(name = "receiver_id")
    private Long receiverId;

    /** 接收人姓名 */
    @Column(name = "receiver_name", length = 64)
    private String receiverName;

    /** 通知类型：借还申请/审批/完成/逾期提醒/检定到期 */
    @Column(name = "type", length = 32)
    private String type;

    /** 通知标题 */
    @Column(name = "title", length = 128)
    private String title;

    /** 通知内容 */
    @Column(name = "content", length = 1000)
    private String content;

    /** 是否已读 */
    @Column(name = "is_read")
    private Boolean isRead = false;

    /** 创建时间 */
    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    public void prePersist() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
        if (isRead == null) {
            isRead = false;
        }
    }
}
