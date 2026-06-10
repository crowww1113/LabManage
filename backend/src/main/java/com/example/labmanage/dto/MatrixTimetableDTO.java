package com.example.labmanage.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MatrixTimetableDTO {
    private Long termId;
    private Integer weekNo;
    private String buildingName;
    private String roomNumber;
    /** 列维度：星期一 ~ 星期日 */
    private List<String> days;
    /** 行维度：节次列表（按 sortOrder 排序） */
    private List<TimeSlotDTO> timeSlots;
    /**
     * 二维矩阵：matrix[dayIndex][slotIndex] = 该格子的所有课表条目
     * dayIndex: 0=周一, 1=周二, ..., 6=周日
     * slotIndex: 对应 timeSlots 列表的索引
     */
    private List<List<List<TimetableItemDTO>>> matrix;
}
