package com.example.labmanage.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ScheduleUsageRecordDTO {
    private Long id;

    private Long reservationId;

    private Long courseId;

    private Long clazzId;

    private Long timeSlotId;

    private String department;

    private LocalDateTime fillTime;

    private String labName;

    private String buildingName;

    private String roomNumber;

    private LocalDate usageDate;

    private String courseOrProjectName;

    private Double plannedHours;

    private String className;

    private Integer expectedAttendance;

    private Long reporterId;

    private String reporterName;

    private String experimentItemName;

    private String experimentItemType;

    private Double actualHours;

    private Integer actualAttendance;

    private String attendanceRecord;

    private String teachingStatus;

    private String equipmentStatus;

    private String recordStatus;

    private String status;
}
