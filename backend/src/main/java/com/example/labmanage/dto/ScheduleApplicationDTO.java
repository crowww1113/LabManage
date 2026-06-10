package com.example.labmanage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ScheduleApplicationDTO {
    private Long id;

    private String applicationNo;

    private Long termId;

    private Long teacherId; // 后端通过Token解析填入，不由前端传递

    private String preferredBuildingName;

    private String preferredRoomNumber;

    private Integer preferredWeekNo;

    private Integer preferredDayOfWeek;

    private LocalDate preferredDate;

    private Long preferredTimeSlotId;

    @NotBlank(message = "申请类型不能为空")
    private String applicationType;

    @NotNull(message = "教学任务ID不能为空")
    private Long teachingTaskId;

    private String expectedLab;

    private String targetWeeks;

    private Integer targetDayOfWeek;

    private String targetTimeSlot;

    // 新增人性化字段
    private String projectName;

    private String projectCategory;

    private String projectLeader;

    private String contactPhone;

    private String grade;

    private String className;

    private Integer participantCount;

    private BigDecimal duration;

    private Integer studentCount;

    private Integer groupCount;

    private Integer studentsPerGroup;

    private String experimentContent;

    private String remark;

    private String experimentRequirement;

    private String equipmentRequirement;

    private String reviewComment;

    private Long reviewedBy;

    private String status;
}
