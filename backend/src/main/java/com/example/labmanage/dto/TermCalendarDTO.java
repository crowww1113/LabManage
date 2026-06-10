package com.example.labmanage.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class TermCalendarDTO {
    private LocalDate date;
    private Integer week;
    private Integer dayOfWeek;
    private Boolean isHoliday;
    private String holidayName;
    private Boolean isLabOpen;
    private String schedule;
}