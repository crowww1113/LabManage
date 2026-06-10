package com.example.labmanage.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 续借申请请求
 */
@Data
public class RenewalRequest {

    /** 续借后的预计归还日期 */
    @NotNull(message = "续借归还日期不能为空")
    private LocalDate newReturnDate;

    /** 续借备注 */
    private String remark;
}
