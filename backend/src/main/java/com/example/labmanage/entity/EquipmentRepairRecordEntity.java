package com.example.labmanage.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "equipment_repair_record")
public class EquipmentRepairRecordEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "equipment_id", nullable = false)
    private Long equipmentId;

    /** 故障描述 */
    @Column(name = "fault_description", length = 1000)
    private String faultDescription;

    /** 维修状态：待维修/维修中/已完成 */
    @Column(name = "repair_status", nullable = false)
    private String repairStatus;

    /** 报修日期 */
    @Column(name = "report_date")
    private LocalDate reportDate;

    /** 维修开始日期 */
    @Column(name = "repair_start_date")
    private LocalDate repairStartDate;

    /** 维修完成日期 */
    @Column(name = "repair_end_date")
    private LocalDate repairEndDate;

    /** 维修时长（天） */
    @Column(name = "repair_duration_days")
    private Integer repairDurationDays;

    /** 维修费用 */
    @Column(name = "repair_cost", precision = 10, scale = 2)
    private java.math.BigDecimal repairCost;

    /** 维修人员 */
    @Column(name = "repair_person", length = 64)
    private String repairPerson;

    /** 维修备注 */
    @Column(name = "repair_remark", length = 1000)
    private String repairRemark;

    @Column(name = "create_time")
    private LocalDate createTime;

    @Column(name = "update_time")
    private LocalDate updateTime;
}
