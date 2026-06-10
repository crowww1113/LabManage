package com.example.labmanage.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 设备登记请求
 */
@Data
public class EquipmentCreateRequest {

    @NotBlank(message = "资产编号不能为空")
    private String assetNo;

    @NotBlank(message = "设备名称不能为空")
    private String name;

    private String model;

    private Long categoryId;

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

    private Long responsibleId;

    private Long deptId;

    private Boolean isImportant = false;

    private String tags;

    private String remark;

    /** 附件路径（JSON格式） */
    private String attachments;

    /** 检定周期（月） */
    private Integer calibrationPeriodMonths;

    /** 上次检定日期 */
    private LocalDate lastCalibrationDate;

    /** 下次检定日期 */
    private LocalDate nextCalibrationDate;
}
