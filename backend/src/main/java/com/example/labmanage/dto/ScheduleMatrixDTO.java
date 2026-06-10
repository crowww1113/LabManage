package com.example.labmanage.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class ScheduleMatrixDTO {
    private String buildingName;
    private String roomNumber;
    private Integer weekNo;
    /** matrix[dayOfWeek-1][slotIndex] = 已排课节数，dayOfWeek: 1-7 */
    private Integer[][] matrix;
    private List<TimeSlotDTO> timeSlots;
}
