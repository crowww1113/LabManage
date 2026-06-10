package com.example.labmanage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UpdateScheduleRequest {
    @NotEmpty(message = "周次不能为空")
    private List<Integer> weeks;

    @NotNull(message = "星期不能为空")
    private Integer dayOfWeek;

    @NotEmpty(message = "节次不能为空")
    private List<Long> timeSlotIds;

    @NotBlank(message = "楼栋不能为空")
    private String buildingName;

    @NotBlank(message = "房间号不能为空")
    private String roomNumber;
}
