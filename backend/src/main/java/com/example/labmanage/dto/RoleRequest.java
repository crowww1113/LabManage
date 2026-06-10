package com.example.labmanage.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class RoleRequest {

    @NotBlank(message = "角色编码不能为空")
    private String code;

    @NotBlank(message = "角色名称不能为空")
    private String name;

    private Long parentRoleId;

    @NotBlank(message = "数据权限范围不能为空")
    private String dataScope;

    private Long orgLimit;

    private List<String> permissions;
}
