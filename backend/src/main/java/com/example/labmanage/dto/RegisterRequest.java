package com.example.labmanage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "账号不能为空")
    @Size(min = 3, max = 64, message = "账号长度为 3-64 个字符")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度为 6-64 个字符")
    private String password;

    @NotBlank(message = "姓名不能为空")
    private String realName;

    @NotNull(message = "所属组织不能为空")
    private Long orgId;

    private String position;

    private String jobNo;

    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "LAB_ADMIN|TEACHER|STUDENT", message = "角色不合法")
    private String roleCode;
}
