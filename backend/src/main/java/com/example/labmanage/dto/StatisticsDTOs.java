package com.example.labmanage.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

public class StatisticsDTOs {

    @Data
    public static class RoomWeeklyStats {
        private String buildingName;
        private String roomNumber;
        private Integer weekNo;
        private Integer totalSlots;
        private Integer reservedSlots;
        private Integer freeSlots;
        private Double usageRate;
    }

    @Data
    public static class HeadcountStats {
        private String buildingName;
        private String roomNumber;
        private Integer expectedTotal;
        private Integer actualTotal;
    }

    @Data
    public static class MajorUsageStats {
        private String majorName;
        private Integer courseHours;
    }

    @Data
    public static class ClassUsageStats {
        private Long clazzId;
        private String className;
        private Integer courseHours;
    }

    @Data
    public static class GradeUsageStats {
        private String grade;
        private Integer courseHours;
    }

    @Data
    public static class CourseUsageStats {
        private Long courseId;
        private String courseName;
        private Integer courseHours;
    }

    @Data
    public static class ReservationStats {
        private String projectCategory;
        private Integer reservationCount;
        private BigDecimal totalDuration;
    }

    @Data
    public static class RegistrationRateStats {
        private Long teacherId;
        private String teacherName;
        private Integer expectedCount;
        private Integer registeredCount;
        private Double registrationRate;
    }

    // ========== Dashboard 大屏 DTO ==========

    @Data
    public static class DashboardData {
        private OccupancyModule occupancy;
        private DensityModule density;
        private TrendModule trend;
        private ProportionModule proportion;
        private RegistrationModule registration;
        private AlertModule alerts;
    }

    @Data
    public static class OccupancyModule {
        private Integer totalRooms;
        private Integer occupiedRooms;
        private Integer freeRooms;
        private List<RoomOccupancy> rooms;
    }

    @Data
    public static class RoomOccupancy {
        private String buildingName;
        private String roomNumber;
        private String status; // "occupied" | "free"
    }

    @Data
    public static class DensityModule {
        private List<BuildingDensity> buildings;
    }

    @Data
    public static class BuildingDensity {
        private String buildingName;
        private List<DateCount> dates;
    }

    @Data
    public static class DateCount {
        private String date; // yyyy-MM-dd
        private Integer count;
    }

    @Data
    public static class TrendModule {
        private List<WeeklyRate> weeks;
    }

    @Data
    public static class WeeklyRate {
        private Integer weekNo;
        private Integer reservedSlots;
        private Integer totalSlots;
        private Double usageRate;
    }

    @Data
    public static class ProportionModule {
        private Integer total;
        private List<CategoryCount> categories;
    }

    @Data
    public static class CategoryCount {
        private String category;
        private Integer count;
        private Double percentage;
    }

    @Data
    public static class RegistrationModule {
        private Integer totalCount;
        private Integer registeredCount;
        private Double overallRate;
        private List<DepartmentRegistration> departments;
    }

    @Data
    public static class DepartmentRegistration {
        private String department;
        private Integer totalCount;
        private Integer registeredCount;
        private Double registrationRate;
    }

    @Data
    public static class AlertModule {
        private List<AlertItem> overdueRecords;
        private List<AlertItem> equipmentAnomalies;
    }

    @Data
    public static class AlertItem {
        private String courseName;
        private String department;
        private String reporterName;
        private String usageDate;
        private String buildingName;
        private String roomNumber;
        private String status;
    }
}
