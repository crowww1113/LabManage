package com.example.labmanage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BatchScheduleRequest {
    @NotNull(message = "教学任务ID不能为空")
    private Long taskId;

    @NotEmpty(message = "周次不能为空")
    private List<Integer> weeks;

    @NotNull(message = "星期不能为空")
    private Integer dayOfWeek;

    @NotEmpty(message = "节次不能为空")
    private List<Long> timeSlotIds;

    @NotBlank(message = "楼栋名称不能为空")
    private String buildingName;

    @NotBlank(message = "房间号不能为空")
    private String roomNumber;

    private boolean force;

    private String experimentContent;

    @NotNull(message = "学生人数不能为空")
    private Integer studentCount;

    private Integer groupCount;
    private Integer studentsPerGroup;
    private String remark;
}
