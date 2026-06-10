package com.example.labmanage.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class TimetableItemDTO {
    private Long id;
    /** 来源类型：APPLICATION(排课申请) / RESERVATION(实验室预约) */
    private String sourceType;
    /** 来源编号（申请编号或预约编号） */
    private String sourceNo;
    private Long courseId;
    private String courseName;
    private Long teacherId;
    private String teacherName;
    private Long clazzId;
    private String clazzName;
    private String buildingName;
    private String roomNumber;
    private Integer dayOfWeek;
    private Long timeSlotId;
    private String timeSlotName;
    private LocalTime startTime;
    private LocalTime endTime;
    private String experimentContent;
    private Integer studentCount;
    private String status;
    /** 申请类型：COURSE(授课申请) / TEMPORARY(临时预约) */
    private String applicationType;
}
