package com.example.labmanage.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 设备响应DTO
 */
@Data
public class EquipmentDTO {

    private Long id;

    private String assetNo;

    private String name;

    private String model;

    private Long categoryId;

    private String categoryName;

    private String unit;

    private String brand;

    private String serialNo;

    private String spec;

    private BigDecimal price;

    private String fundSource;

    private LocalDate purchaseDate;

    private Integer useYears;

    private String supplier;

    private Integer warrantyMonths;

    private Long locationId;

    private String locationName;

    private Long responsibleId;

    private String responsibleName;

    private String status;

    private Long deptId;

    private String deptName;

    private Boolean isImportant;

    private String tags;

    private String remark;

    private String attachments;

    private Integer calibrationPeriodMonths;

    private LocalDate lastCalibrationDate;

    private LocalDate nextCalibrationDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
