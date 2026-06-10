package com.example.labmanage.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ScheduleBookingDTO {
    private Long id;
    private String bookingNo;
    private Long termId;
    private Long teachingTaskId;
    private Long courseId;
    private Long clazzId;
    private Long teacherId;
    private String buildingName;
    private String roomNumber;
    private Integer dayOfWeek;
    private String weekRange;
    private String timeSlotInfo;
    private Integer studentCount;
    private Integer groupCount;
    private Integer studentsPerGroup;
    private String experimentContent;
    private String remark;
    private String projectName;
    private String projectCategory;
    private String projectLeader;
    private String contactPhone;
    private String grade;
    private String className;
    private Integer participantCount;
    private BigDecimal duration;
    private String status;
    private String rejectReason;
    private LocalDateTime createdAt;

    // 展开字段
    private List<ScheduleReservationDTO> reservations;
    private List<Integer> weeks;
}
