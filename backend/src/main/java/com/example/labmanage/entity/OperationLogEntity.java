package com.example.labmanage.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 操作日志实体类
 */
@Getter
@Setter
@Entity
@Table(name = "operation_log")
public class OperationLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 操作人ID */
    @Column(name = "operator_id")
    private Long operatorId;

    /** 操作人姓名 */
    @Column(name = "operator_name", length = 64)
    private String operatorName;

    /** 操作模块 */
    @Column(name = "module", length = 64)
    private String module;

    /** 操作类型：新增/修改/删除/审批/归还/续借 */
    @Column(name = "operation_type", length = 32)
    private String operationType;

    /** 操作内容描述 */
    @Column(name = "content", length = 1000)
    private String content;

    /** 操作IP */
    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    /** 操作时间 */
    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    public void prePersist() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
    }
}
