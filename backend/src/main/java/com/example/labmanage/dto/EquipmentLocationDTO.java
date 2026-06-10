package com.example.labmanage.dto;

import lombok.Data;

/**
 * 设备位置DTO
 */
@Data
public class EquipmentLocationDTO {

    private Long id;

    private String code;

    private String name;

    private Long buildingId;

    private String buildingName;

    private String roomNumber;

    private Integer floor;

    private String status;

    private String description;
}
