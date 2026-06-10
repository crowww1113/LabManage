package com.example.labmanage.dto;

import lombok.Data;

/**
 * 设备统计DTO
 */
@Data
public class EquipmentStatisticsDTO {

    /** 设备总数 */
    private Long totalEquipment;

    /** 在库可用 */
    private Long available;

    /** 在库-待维修 */
    private Long pendingRepair;

    /** 在库-已预约 */
    private Long reserved;

    /** 借出中 */
    private Long borrowed;

    /** 送修 */
    private Long repairing;

    /** 报废 */
    private Long scrapped;

    /** 丢失 */
    private Long lost;

    /** 待审批借出申请 */
    private Long pendingBorrow;

    /** 分类统计 */
    private java.util.List<CategoryCount> categoryCounts;

    @Data
    public static class CategoryCount {
        private String categoryName;
        private Long count;
    }
}
