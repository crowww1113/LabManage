package com.example.labmanage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UserRequest {
    @NotBlank(message = "登录账号不能为空")
    private String username;

    @NotBlank(message = "姓名不能为空")
    private String realName;

    @NotNull(message = "所属组织不能为空")
    private Long orgId;

    private String position;
    private String jobNo;

    @NotBlank(message = "账号状态不能为空")
    private String status;

    private List<Long> roleIds;
}
