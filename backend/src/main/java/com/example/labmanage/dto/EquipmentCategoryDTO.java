package com.example.labmanage.dto;

import lombok.Data;

/**
 * 设备分类DTO
 */
@Data
public class EquipmentCategoryDTO {

    private Long id;

    private String code;

    private String name;

    private Long parentId;

    private String parentName;

    private Integer sortOrder;

    private String status;

    private String description;
}
