package com.example.labmanage.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 设备实体类
 */
@Getter
@Setter
@Entity
@Table(name = "equipment")
public class EquipmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 资产编号（唯一） */
    @Column(name = "asset_no", nullable = false, length = 64, unique = true)
    private String assetNo;

    /** 设备名称 */
    @Column(name = "name", nullable = false, length = 128)
    private String name;

    /** 型号 */
    @Column(name = "model", length = 64)
    private String model;

    /** 类别ID */
    @Column(name = "category_id")
    private Long categoryId;

    /** 计量单位 */
    @Column(name = "unit", length = 16)
    private String unit;

    /** 品牌 */
    @Column(name = "brand", length = 64)
    private String brand;

    /** 序列号 */
    @Column(name = "serial_no", length = 128)
    private String serialNo;

    /** 规格 */
    @Column(name = "spec", length = 256)
    private String spec;

    /** 购入价格 */
    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    /** 经费来源 */
    @Column(name = "fund_source", length = 128)
    private String fundSource;

    /** 购入日期 */
    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    /** 使用年限 */
    @Column(name = "use_years")
    private Integer useYears;

    /** 供应商 */
    @Column(name = "supplier", length = 128)
    private String supplier;

    /** 保修期（月） */
    @Column(name = "warranty_months")
    private Integer warrantyMonths;

    /** 存放位置ID */
    @Column(name = "location_id")
    private Long locationId;

    /** 责任人ID */
    @Column(name = "responsible_id")
    private Long responsibleId;

    /** 状态: 在库-可用/在库-待维修/在库-已预约/借出/送修/报废/丢失 */
    @Column(name = "status", nullable = false, length = 32)
    private String status = "在库-可用";

    /** 维修频次（累计维修次数） */
    @Column(name = "repair_count")
    private Integer repairCount = 0;

    /** 累计维修时长（天） */
    @Column(name = "total_repair_days")
    private Integer totalRepairDays = 0;

    /** 所属部门ID */
    @Column(name = "dept_id")
    private Long deptId;

    /** 是否重要设备 */
    @Column(name = "is_important")
    private Boolean isImportant = false;

    /** 标签 */
    @Column(name = "tags", length = 256)
    private String tags;

    /** 备注 */
    @Column(name = "remark", length = 500)
    private String remark;

    /** 附件路径（JSON格式存储多个文件路径） */
    @Column(name = "attachments", length = 1000)
    private String attachments;

    /** 检定周期（月） */
    @Column(name = "calibration_period_months")
    private Integer calibrationPeriodMonths;

    /** 上次检定日期 */
    @Column(name = "last_calibration_date")
    private LocalDate lastCalibrationDate;

    /** 下次检定日期 */
    @Column(name = "next_calibration_date")
    private LocalDate nextCalibrationDate;

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
