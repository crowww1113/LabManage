package com.example.labmanage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MajorDTO {
    private Long id;

    @NotBlank(message = "专业代码不能为空")
    private String majorCode;

    @NotBlank(message = "专业名称不能为空")
    private String majorName;

    @NotNull(message = "部门ID不能为空")
    private Long deptId;

    private String status;
}