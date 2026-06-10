package com.example.labmanage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ScheduleReservationDTO {
    private Long id;

    private String reservationNo;

    @NotNull(message = "学期ID不能为空")
    private Long termId;

    private Long teachingTaskId;

    private Long applicationId;

    private Long bookingId;

    private Long courseId;

    @NotNull(message = "班级ID不能为空")
    private Long clazzId;

    private Long teacherId;

    @NotBlank(message = "楼栋名称不能为空")
    private String buildingName;

    @NotBlank(message = "房间号不能为空")
    private String roomNumber;

    @NotNull(message = "使用日期不能为空")
    private LocalDate useDate;

    @NotNull(message = "周次不能为空")
    private Integer weekNo;

    @NotNull(message = "星期不能为空")
    private Integer dayOfWeek;

    @NotNull(message = "节次ID不能为空")
    private Long timeSlotId;

    @NotNull(message = "开始时间不能为空")
    private LocalTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalTime endTime;

    @NotNull(message = "学生人数不能为空")
    private Integer studentCount;

    private Integer groupCount;

    private Integer studentsPerGroup;

    @NotBlank(message = "实验内容不能为空")
    private String experimentContent;

    private String remark;

    private String status;

    // 新增人性化字段
    private String projectName;

    private String projectCategory;

    private String projectLeader;

    private String contactPhone;

    private String grade;

    private String className;

    private Integer participantCount;

    private BigDecimal duration;
}
