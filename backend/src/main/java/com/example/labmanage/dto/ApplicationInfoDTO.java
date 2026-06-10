package com.example.labmanage.dto;

import lombok.Data;

@Data
public class ApplicationInfoDTO {
    private String targetWeeks;
    private Integer targetDayOfWeek;
    private String targetTimeSlot;
    private String expectedLab;
}
