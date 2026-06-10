package com.example.labmanage.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class EquipmentBorrowRecordDTO {
    private Long id;
    private String recordNo;
    private Long equipmentId;
    private String equipmentName;
    private String equipmentAssetNo;
    private Long borrowerId;
    private String borrowerName;
    private String purpose;
    private Long courseId;
    private String courseName;
    private LocalDate expectedReturnDate;
    private LocalDate actualReturnDate;
    private String status;
    // ===== 导师审批相关字段 =====
    private Long mentorId;
    private String mentorName;
    private String mentorApproveRemark;
    private LocalDate mentorApproveTime;
    // ===== 管理员审批相关字段 =====
    private Long approverId;
    private String approverName;
    private String approveRemark;
    private LocalDate approveTime;
    private String remark;
    private LocalDate createTime;
    private LocalDate updateTime;
    private boolean isOverdue;
    private int overdueDays;

    // ===== 归还验收相关字段 =====
    private String returnLocation;
    private String returnResult;
    private String accessoriesInfo;
    private String damageDescription;
    private Long verifierId;
    private String verifierName;

    // ===== 借用申请相关字段 =====
    private String phone;
    private String useLocation;

    // ===== 领取确认相关字段 =====
    private Long pickupPersonId;
    private String pickupPersonName;
    private LocalDate pickupTime;
    private String pickupEquipmentStatus;
    private String pickupRemark;

    // ===== 归还申请相关字段 =====
    private String expectedReturnTime;
    private String expectedReturnLocation;
    private LocalDate returnApplyTime;
    private String actualReturnTime;
    private LocalDate returnConfirmTime;

    // ===== 续借审批相关字段 =====
    private String renewalStatus;
    private LocalDate renewalNewReturnDate;
    private String renewalRemark;
    private LocalDate renewalApplyTime;
    private Long renewalApproverId;
    private String renewalApproverName;
    private LocalDate renewalApproveTime;
    private String renewalApproveRemark;

    // ===== 逾期告警相关字段 =====
    private Boolean overdueWarning;
    private LocalDate overdueWarningTime;
    private String overdueWarningRemark;

    // ===== 逾期提醒记录 =====
    private Boolean remind1day;
    private Boolean remindToday;
    private Boolean remindOverdue;
}