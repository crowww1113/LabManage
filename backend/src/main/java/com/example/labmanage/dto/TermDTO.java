// TermDTO.java
package com.example.labmanage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class TermDTO {
    private Long id;

    @NotBlank(message = "学期名称不能为空")
    private String termName;

    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;

    private Integer totalWeeks;
    private List<String> holidayDates;
    private List<String> labOpenDays;
    private String status;
}