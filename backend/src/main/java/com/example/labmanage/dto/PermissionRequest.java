package com.example.labmanage.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PermissionRequest {
    @NotBlank(message = "所属模块不能为空")
    private String module;

    @NotBlank(message = "权限名称不能为空")
    private String name;

    @NotBlank(message = "操作类型不能为空")
    private String action;

    // 可不传，后端自动生成
    private String code;

    @NotBlank(message = "状态不能为空")
    private String status;

    private String description;
}
