package com.example.labmanage.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TimetableListDTO {
    private Long id;
    private String sourceType;
    private String sourceNo;
    private String courseName;
    private String clazzName;
    private String teacherName;
    private Integer dayOfWeek;
    private String dayOfWeekText;
    private String timeSlotName;
    private String buildingName;
    private String roomNumber;
    private String status;
    private Long teacherId;
    private String applicationType;
    private String experimentContent;
    private Integer studentCount;
}
