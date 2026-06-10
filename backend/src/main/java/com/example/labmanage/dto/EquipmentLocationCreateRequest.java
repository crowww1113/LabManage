package com.example.labmanage.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 设备位置创建请求
 */
@Data
public class EquipmentLocationCreateRequest {

    @NotBlank(message = "位置编码不能为空")
    private String code;

    @NotBlank(message = "位置名称不能为空")
    private String name;

    private Long buildingId;

    private String roomNumber;

    private Integer floor;

    private String status = "启用";

    private String description;
}
