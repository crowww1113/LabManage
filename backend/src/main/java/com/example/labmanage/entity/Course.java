package com.example.labmanage.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "sys_course")
@Data
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64, unique = true)
    private String code;

    @Column(nullable = false, length = 100)
    private String cnName;

    @Column(length = 100)
    private String enName;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(nullable = false)
    private Double credit;

    @Column(nullable = false)
    private Integer totalHour;

    private Integer teachHour;
    private Integer practiceHour;
    private Integer labHour;
    private Integer netHour;

    @Column(nullable = false, length = 50)
    private String term;
}
