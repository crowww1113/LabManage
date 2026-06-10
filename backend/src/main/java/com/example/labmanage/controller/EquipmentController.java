package com.example.labmanage.controller;

import com.example.labmanage.dto.EquipmentCreateRequest;
import com.example.labmanage.dto.EquipmentDTO;
import com.example.labmanage.dto.EquipmentStatisticsDTO;
import com.example.labmanage.entity.EquipmentEntity;
import com.example.labmanage.scheduler.CalibrationReminderScheduler;
import com.example.labmanage.service.EquipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/equipment")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;
    private final CalibrationReminderScheduler calibrationReminderScheduler;

    @GetMapping
    public List<EquipmentDTO> list(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String assetNo,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) String keyword
    ) {
        return equipmentService.list(id, assetNo, name, status, categoryId, locationId, keyword);
    }

    @GetMapping("/{id}")
    public EquipmentDTO get(@PathVariable Long id) {
        return equipmentService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('LAB_ADMIN','EQUIPMENT_ADMIN')")
    public EquipmentDTO create(@Valid @RequestBody EquipmentCreateRequest req) {
        return equipmentService.create(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','EQUIPMENT_ADMIN')")
    public EquipmentDTO update(@PathVariable Long id, @Valid @RequestBody EquipmentCreateRequest req) {
        return equipmentService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','EQUIPMENT_ADMIN')")
    public Map<String, Object> delete(@PathVariable Long id) {
        equipmentService.delete(id);
        return Map.of("code", 200, "message", "删除成功");
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','EQUIPMENT_ADMIN')")
    public EquipmentDTO updateStatus(@PathVariable Long id, @RequestBody Map<String, String> req) {
        return equipmentService.updateStatus(id, req.get("status"));
    }

    @PostMapping("/{id}/actions/borrow")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','EQUIPMENT_ADMIN')")
    public EquipmentDTO borrowEquipment(@PathVariable Long id) {
        return equipmentService.borrowEquipment(id);
    }

    @PostMapping("/{id}/actions/return")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','EQUIPMENT_ADMIN')")
    public EquipmentDTO returnEquipment(@PathVariable Long id) {
        return equipmentService.returnEquipment(id);
    }

    @PostMapping("/{id}/actions/mark-repair")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','EQUIPMENT_ADMIN')")
    public EquipmentDTO markForRepair(@PathVariable Long id) {
        return equipmentService.markForRepair(id);
    }

    @PostMapping("/{id}/actions/send-repair")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','EQUIPMENT_ADMIN')")
    public EquipmentDTO sendForRepair(@PathVariable Long id) {
        return equipmentService.sendForRepair(id);
    }

    @PostMapping("/{id}/actions/repair-complete")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','EQUIPMENT_ADMIN')")
    public EquipmentDTO repairComplete(@PathVariable Long id) {
        return equipmentService.repairComplete(id);
    }

    @PostMapping("/{id}/actions/scrap")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','EQUIPMENT_ADMIN')")
    public EquipmentDTO scrap(@PathVariable Long id) {
        return equipmentService.scrap(id);
    }

    @PostMapping("/{id}/actions/recover")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','EQUIPMENT_ADMIN')")
    public EquipmentDTO recoverLost(@PathVariable Long id) {
        return equipmentService.recoverLost(id);
    }

    @PostMapping("/{id}/actions/mark-lost")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','EQUIPMENT_ADMIN')")
    public EquipmentDTO markAsLost(@PathVariable Long id) {
        return equipmentService.markAsLost(id);
    }

    @GetMapping("/{id}/actions")
    public Map<String, Object> getAvailableActions(@PathVariable Long id) {
        return Map.of("code", 200, "actions", equipmentService.getAvailableActions(id));
    }

    @GetMapping("/statistics")
    public EquipmentStatisticsDTO statistics() {
        return equipmentService.getStatistics();
    }

    /**
     * 获取即将到期的检定设备列表
     */
    @GetMapping("/calibration-reminders")
    public List<EquipmentEntity> getCalibrationReminders() {
        return calibrationReminderScheduler.getUpcomingCalibrations();
    }
}
