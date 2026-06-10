package com.example.labmanage.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class WeeklyRoomUsageSummaryDTO {
    private String buildingName;
    private String roomNumber;
    private Integer weekNo;
    private Long totalSlots;
    /** dayOfWeek -> timeSlotId -> count */
    private Map<Integer, Map<Long, Long>> detailMap;
    private List<ScheduleMatrixDTO> matrixList;
}
