package com.example.labmanage.controller;

import com.example.labmanage.service.InventoryMonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryMonitorController {

    private final InventoryMonitorService inventoryService;

    /** 实时库存查询 */
    @GetMapping("/query")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','EQUIPMENT_ADMIN','TEACHER')")
    public List<Map<String, Object>> queryInventory(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String status) {
        return inventoryService.queryInventory(category, location, brand, status);
    }

    /** 设备总账与分类统计 */
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','EQUIPMENT_ADMIN')")
    public Map<String, Object> getEquipmentSummary() {
        return inventoryService.getEquipmentSummary();
    }

    /** 状态分布表 */
    @GetMapping("/status-distribution")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','EQUIPMENT_ADMIN')")
    public List<Map<String, Object>> getStatusDistribution() {
        return inventoryService.getStatusDistribution();
    }

    /** 使用率统计 */
    @GetMapping("/usage-rate")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','EQUIPMENT_ADMIN')")
    public List<Map<String, Object>> getUsageRate() {
        return inventoryService.getUsageRate();
    }

    /** 借还流水账 */
    @GetMapping("/ledger")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','EQUIPMENT_ADMIN')")
    public List<Map<String, Object>> getBorrowLedger() {
        return inventoryService.getBorrowLedger();
    }

    /** 逾期清单 */
    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','EQUIPMENT_ADMIN')")
    public List<Map<String, Object>> getOverdueList() {
        return inventoryService.getOverdueList();
    }

    /** 故障维修统计 */
    @GetMapping("/repair-stats")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','EQUIPMENT_ADMIN')")
    public List<Map<String, Object>> getRepairStats() {
        return inventoryService.getRepairStats();
    }

    /** 检定到期清单 */
    @GetMapping("/calibration-due")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','EQUIPMENT_ADMIN')")
    public List<Map<String, Object>> getCalibrationDueList() {
        return inventoryService.getCalibrationDueList();
    }

    /** 触发检定到期检查与通知 */
    @PostMapping("/check-calibration")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','EQUIPMENT_ADMIN')")
    public Map<String, Object> checkCalibration() {
        inventoryService.checkAndNotifyCalibrationDue();
        return Map.of("success", true, "message", "检定到期检查完成，已发送相关通知");
    }
}
