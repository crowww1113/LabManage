package com.example.labmanage.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 审批请求
 */
@Data
public class EquipmentApprovalRequest {

    @NotBlank(message = "审批备注不能为空")
    private String remark;
}
