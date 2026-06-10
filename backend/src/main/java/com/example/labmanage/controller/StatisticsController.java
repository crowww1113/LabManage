package com.example.labmanage.controller;

import com.example.labmanage.dto.StatisticsDTOs.*;
import com.example.labmanage.service.StatisticsService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/room-weekly")
    public List<RoomWeeklyStats> roomWeekly(@RequestParam Long termId) {
        return statisticsService.getRoomWeeklyStats(termId);
    }

    @GetMapping("/headcount")
    public List<HeadcountStats> headcount(@RequestParam Long termId) {
        return statisticsService.getHeadcountStats(termId);
    }

    @GetMapping("/major")
    public List<MajorUsageStats> major(@RequestParam Long termId) {
        return statisticsService.getMajorStats(termId);
    }

    @GetMapping("/class")
    public List<ClassUsageStats> clazz(@RequestParam Long termId) {
        return statisticsService.getClassStats(termId);
    }

    @GetMapping("/grade")
    public List<GradeUsageStats> grade(@RequestParam Long termId) {
        return statisticsService.getGradeStats(termId);
    }

    @GetMapping("/course")
    public List<CourseUsageStats> course(@RequestParam Long termId) {
        return statisticsService.getCourseStats(termId);
    }

    @GetMapping("/reservation")
    public List<ReservationStats> reservation(@RequestParam Long termId) {
        return statisticsService.getReservationStats(termId);
    }

    @GetMapping("/registration-rate")
    public List<RegistrationRateStats> registrationRate(@RequestParam Long termId) {
        return statisticsService.getRegistrationRateStats(termId);
    }

    // ========== Dashboard 大屏 ==========

    @GetMapping("/dashboard")
    public DashboardData dashboard(
            @RequestParam(required = false) Long termId,
            @RequestParam(required = false) String mode,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return statisticsService.getDashboard(termId, mode, startDate, endDate);
    }

    // ========== Excel 导出 ==========

    @GetMapping("/room-weekly/export")
    public void exportRoomWeekly(@RequestParam Long termId, HttpServletResponse response) throws Exception {
        statisticsService.exportRoomWeekly(termId, response);
    }

    @GetMapping("/headcount/export")
    public void exportHeadcount(@RequestParam Long termId, HttpServletResponse response) throws Exception {
        statisticsService.exportHeadcount(termId, response);
    }

    @GetMapping("/major/export")
    public void exportMajor(@RequestParam Long termId, HttpServletResponse response) throws Exception {
        statisticsService.exportMajor(termId, response);
    }

    @GetMapping("/class/export")
    public void exportClass(@RequestParam Long termId, HttpServletResponse response) throws Exception {
        statisticsService.exportClass(termId, response);
    }

    @GetMapping("/grade/export")
    public void exportGrade(@RequestParam Long termId, HttpServletResponse response) throws Exception {
        statisticsService.exportGrade(termId, response);
    }

    @GetMapping("/course/export")
    public void exportCourse(@RequestParam Long termId, HttpServletResponse response) throws Exception {
        statisticsService.exportCourse(termId, response);
    }

    @GetMapping("/reservation/export")
    public void exportReservation(@RequestParam Long termId, HttpServletResponse response) throws Exception {
        statisticsService.exportReservation(termId, response);
    }

    @GetMapping("/registration-rate/export")
    public void exportRegistrationRate(@RequestParam Long termId, HttpServletResponse response) throws Exception {
        statisticsService.exportRegistrationRate(termId, response);
    }
}
