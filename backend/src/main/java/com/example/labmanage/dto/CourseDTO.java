package com.example.labmanage.dto;

import lombok.Data;

@Data
public class CourseDTO {
    private Long id;
    private String nameCn;
    private String nameEn;
    private String nature;
    private Double credit;
    private Integer totalHours;
    private Integer teachHours;
    private Integer practiceHours;
    private Integer experimentHours;
    private Integer onlineHours;
    private String semester;
}