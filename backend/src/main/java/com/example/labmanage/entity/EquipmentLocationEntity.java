package com.example.labmanage.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 设备存放位置实体类
 */
@Getter
@Setter
@Entity
@Table(name = "equipment_location")
public class EquipmentLocationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 位置编码 */
    @Column(name = "code", nullable = false, length = 32, unique = true)
    private String code;

    /** 位置名称 */
    @Column(name = "name", nullable = false, length = 128)
    private String name;

    /** 所属楼栋ID */
    @Column(name = "building_id")
    private Long buildingId;

    /** 房间号 */
    @Column(name = "room_number", length = 32)
    private String roomNumber;

    /** 楼层 */
    @Column(name = "floor")
    private Integer floor;

    /** 状态 */
    @Column(name = "status", length = 16)
    private String status = "启用";

    /** 描述 */
    @Column(name = "description", length = 256)
    private String description;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
