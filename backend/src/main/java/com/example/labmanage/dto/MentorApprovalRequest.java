package com.example.labmanage.dto;

import lombok.Data;

/**
 * 导师审批请求
 */
@Data
public class MentorApprovalRequest {

    /** 是否批准 */
    private Boolean approved;

    /** 审批备注 */
    private String remark;
}
