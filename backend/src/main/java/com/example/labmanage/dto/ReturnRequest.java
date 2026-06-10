package com.example.labmanage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 归还验收请求
 */
@Data
public class ReturnRequest {

    /** 验收结果: 完好/损坏/缺件 */
    @NotBlank(message = "验收结果不能为空")
    private String returnResult;

    /** 配件清单/缺件说明 */
    private String accessoriesInfo;

    /** 损坏说明 */
    private String damageDescription;
}
