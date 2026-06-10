package com.example.labmanage.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 设备分类创建请求
 */
@Data
public class EquipmentCategoryCreateRequest {

    @NotBlank(message = "分类编码不能为空")
    private String code;

    @NotBlank(message = "分类名称不能为空")
    private String name;

    private Long parentId;

    private Integer sortOrder = 0;

    private String status = "启用";

    private String description;
}
