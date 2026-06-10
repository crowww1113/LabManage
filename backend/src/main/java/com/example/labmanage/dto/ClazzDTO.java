package com.example.labmanage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClazzDTO {
    private Long id;

    @NotBlank(message = "班级编号不能为空")
    private String clazzCode;

    @NotBlank(message = "班级名称不能为空")
    private String clazzName;

    @NotNull(message = "专业ID不能为空")
    private Long majorId;

    @NotNull(message = "部门ID不能为空")
    private Long deptId;

    @NotBlank(message = "年级不能为空")
    private String grade;

    private Long managerId;
    private Long headTeacherId;
    private String status;
}