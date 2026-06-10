package com.example.labmanage.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrgRequest {

    @NotBlank(message = "组织编码不能为空")
    private String code;

    @NotBlank(message = "组织名称不能为空")
    private String name;

    @NotBlank(message = "组织层级不能为空")
    private String level;

    private Long parentId;

    private String leader;

    private String status = "启用";
}
