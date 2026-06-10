package com.example.labmanage.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    @GetMapping("/login")
    public String login() {
        return "forward:/login.html";
    }

    @GetMapping("/teaching")
    public String teaching() {
        return "forward:/teaching-index.html";
    }

    @GetMapping("/course")
    public String course() {
        return "forward:/course-list.html";
    }

    @GetMapping("/term")
    public String term() {
        return "forward:/term-form.html";
    }

    @GetMapping("/calendar")
    public String calendar() {
        return "forward:/term-calendar.html";
    }

    @GetMapping("/time-slot")
    public String timeSlot() {
        return "forward:/time-slot.html";
    }

    @GetMapping("/schedule-application")
    public String scheduleApplication() {
        return "forward:/schedule-application.html";
    }

    @GetMapping("/statistics-reports")
    public String statisticsReports() {
        return "forward:/statistics-reports.html";
    }

    @GetMapping("/statistics-dashboard")
    public String statisticsDashboard() {
        return "forward:/statistics-dashboard.html";
    }
}
