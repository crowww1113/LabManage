package com.example.labmanage.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "equipment_borrow_record")
public class EquipmentBorrowRecordEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "record_no", unique = true, nullable = false)
    private String recordNo;

    @Column(name = "equipment_id", nullable = false)
    private Long equipmentId;

    @Column(name = "borrower_id", nullable = false)
    private Long borrowerId;

    @Column(name = "purpose")
    private String purpose;

    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "expected_return_date", nullable = false)
    private LocalDate expectedReturnDate;

    @Column(name = "actual_return_date")
    private LocalDate actualReturnDate;

    @Column(name = "status", nullable = false)
    private String status; // 待导师审批、待管理员审批、已批准、已借出、已归还、已拒绝、逾期

    // ===== 导师审批相关字段 =====
    /** 导师ID */
    @Column(name = "mentor_id")
    private Long mentorId;

    /** 导师审批备注 */
    @Column(name = "mentor_approve_remark")
    private String mentorApproveRemark;

    /** 导师审批时间 */
    @Column(name = "mentor_approve_time")
    private LocalDate mentorApproveTime;

    // ===== 管理员审批相关字段 =====
    @Column(name = "approver_id")
    private Long approverId;

    @Column(name = "approve_remark")
    private String approveRemark;

    /** 管理员审批时间 */
    @Column(name = "approve_time")
    private LocalDate approveTime;

    @Column(name = "remark")
    private String remark;

    @Column(name = "create_time")
    private LocalDate createTime;

    @Column(name = "update_time")
    private LocalDate updateTime;

    // ===== 归还验收相关字段 =====

    /** 归还地点 */
    @Column(name = "return_location")
    private String returnLocation;

    /** 验收结果: 完好/损坏/缺件 */
    @Column(name = "return_result")
    private String returnResult;

    /** 配件核对信息 */
    @Column(name = "accessories_info", length = 1000)
    private String accessoriesInfo;

    /** 损坏/缺件说明 */
    @Column(name = "damage_description", length = 1000)
    private String damageDescription;

    /** 验收人ID */
    @Column(name = "verifier_id")
    private Long verifierId;

    /** 联系电话 */
    @Column(name = "phone")
    private String phone;

    /** 使用地点 */
    @Column(name = "use_location")
    private String useLocation;

    // ===== 领取确认相关字段 =====
    /** 领取人ID */
    @Column(name = "pickup_person_id")
    private Long pickupPersonId;

    /** 领取时间 */
    @Column(name = "pickup_time")
    private LocalDate pickupTime;

    /** 领取时设备状态 */
    @Column(name = "pickup_equipment_status")
    private String pickupEquipmentStatus;

    /** 领取备注 */
    @Column(name = "pickup_remark")
    private String pickupRemark;

    // ===== 归还申请相关字段 =====
    /** 预计归还时间 */
    @Column(name = "expected_return_time")
    private String expectedReturnTime;

    /** 预计归还地点 */
    @Column(name = "expected_return_location")
    private String expectedReturnLocation;

    /** 归还申请时间 */
    @Column(name = "return_apply_time")
    private LocalDate returnApplyTime;

    /** 实际归还时间 */
    @Column(name = "actual_return_time")
    private String actualReturnTime;

    /** 归还确认时间 */
    @Column(name = "return_confirm_time")
    private LocalDate returnConfirmTime;

    // ===== 续借审批相关字段 =====
    /** 续借状态：null-无续借, 续借待审批-申请中, 续借已批准-通过, 续借已拒绝-拒绝 */
    @Column(name = "renewal_status")
    private String renewalStatus;

    /** 续借申请的新归还日期 */
    @Column(name = "renewal_new_return_date")
    private LocalDate renewalNewReturnDate;

    /** 续借申请理由 */
    @Column(name = "renewal_remark", length = 1000)
    private String renewalRemark;

    /** 续借申请时间 */
    @Column(name = "renewal_apply_time")
    private LocalDate renewalApplyTime;

    /** 续借审批人ID */
    @Column(name = "renewal_approver_id")
    private Long renewalApproverId;

    /** 续借审批时间 */
    @Column(name = "renewal_approve_time")
    private LocalDate renewalApproveTime;

    /** 续借审批意见 */
    @Column(name = "renewal_approve_remark")
    private String renewalApproveRemark;

    // ===== 逾期告警相关字段 =====
    /** 逾期告警标记：管理员手动标记 */
    @Column(name = "overdue_warning")
    private Boolean overdueWarning;

    /** 逾期告警时间 */
    @Column(name = "overdue_warning_time")
    private LocalDate overdueWarningTime;

    /** 逾期告警备注 */
    @Column(name = "overdue_warning_remark")
    private String overdueWarningRemark;

    // ===== 逾期提醒记录 =====
    /** 提前1天提醒 */
    @Column(name = "remind_1day")
    private Boolean remind1day;

    /** 当天提醒 */
    @Column(name = "remind_today")
    private Boolean remindToday;

    /** 逾期后提醒 */
    @Column(name = "remind_overdue")
    private Boolean remindOverdue;
}