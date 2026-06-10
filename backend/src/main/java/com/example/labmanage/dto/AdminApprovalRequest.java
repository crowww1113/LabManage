package com.example.labmanage.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 管理员审批请求
 */
@Data
public class AdminApprovalRequest {

    /** 修改后的预计归还日期 */
    private LocalDate newReturnDate;

    /** 审批备注 */
    private String remark;
}
