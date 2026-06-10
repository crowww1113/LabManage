package com.example.labmanage.dto;

import lombok.Data;
import java.util.List;

@Data
public class LabAvailabilityItem {
    private String buildingName;
    private String roomNumber;
    private boolean isAvailable;
    private List<ConflictInfo> conflicts;

    @Data
    public static class ConflictInfo {
        private Integer weekNo;
        private String timeSlotName;
        private String courseName;
        private String teacherName;
        private String clazzName;
        private String sourceType; // RESERVATION / APPLICATION
    }
}
