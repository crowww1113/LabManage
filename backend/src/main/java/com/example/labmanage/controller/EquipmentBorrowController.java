package com.example.labmanage.controller;

import com.example.labmanage.dto.EquipmentBorrowRecordDTO;
import com.example.labmanage.service.EquipmentBorrowService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/equipment-borrow")
@RequiredArgsConstructor
public class EquipmentBorrowController {

    private final EquipmentBorrowService borrowService;

    @GetMapping
    public List<EquipmentBorrowRecordDTO> list(@RequestParam(required = false) String status) {
        if (status != null && !status.isEmpty()) {
            return borrowService.listByStatus(status);
        }
        return borrowService.listAll();
    }

    @GetMapping("/overdue")
    public List<EquipmentBorrowRecordDTO> getOverdue() {
        return borrowService.getOverdueRecords();
    }

    @GetMapping("/due-soon")
    public List<EquipmentBorrowRecordDTO> getDueSoon() {
        return borrowService.getDueSoonRecords();
    }

    @GetMapping("/due-today")
    public List<EquipmentBorrowRecordDTO> getDueToday() {
        return borrowService.getDueTodayRecords();
    }

    @GetMapping("/my-records")
    public List<EquipmentBorrowRecordDTO> getMyRecords(@RequestParam Long borrowerId) {
        return borrowService.listByBorrower(borrowerId);
    }

    @GetMapping("/{id}")
    public EquipmentBorrowRecordDTO getById(@PathVariable Long id) {
        return borrowService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('LAB_ADMIN','EQUIPMENT_ADMIN','TEACHER')")
    public EquipmentBorrowRecordDTO createBorrowRequest(@RequestBody Map<String, Object> req) {
        Long equipmentId = Long.valueOf(req.get("equipmentId").toString());
        Long borrowerId = Long.valueOf(req.get("borrowerId").toString());
        String purpose = (String) req.get("purpose");
        LocalDate expectedReturnDate = LocalDate.parse((String) req.get("expectedReturnDate"));
        String remark = (String) req.get("remark");
        String phone = (String) req.get("phone");
        String useLocation = (String) req.get("useLocation");
        Long mentorId = req.get("mentorId") != null ? Long.valueOf(req.get("mentorId").toString()) : null;
        return borrowService.createBorrowRequest(equipmentId, borrowerId, purpose, expectedReturnDate, remark, phone, useLocation, mentorId);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','EQUIPMENT_ADMIN','TEACHER')")
    public EquipmentBorrowRecordDTO approve(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        Long approverId = Long.valueOf(req.get("approverId").toString());
        String approveRemark = (String) req.get("approveRemark");
        return borrowService.approveRecord(id, approverId, approveRemark);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','EQUIPMENT_ADMIN','TEACHER')")
    public EquipmentBorrowRecordDTO reject(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        Long approverId = Long.valueOf(req.get("approverId").toString());
        String approveRemark = (String) req.get("approveRemark");
        return borrowService.rejectRecord(id, approverId, approveRemark);
    }

    @PostMapping("/{id}/pickup")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','EQUIPMENT_ADMIN')")
    public EquipmentBorrowRecordDTO pickup(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        Long pickupPersonId = req.get("pickupPersonId") != null ? Long.valueOf(req.get("pickupPersonId").toString()) : null;
        String pickupRemark = (String) req.get("pickupRemark");
        return borrowService.pickupEquipment(id, pickupPersonId, pickupRemark);
    }

    @PostMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','EQUIPMENT_ADMIN')")
    public EquipmentBorrowRecordDTO returnEquipment(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        Long verifierId = Long.valueOf(req.get("verifierId").toString());
        String returnResult = (String) req.get("returnResult");
        String returnLocation = (String) req.get("returnLocation");
        String accessoriesInfo = (String) req.get("accessoriesInfo");
        String damageDescription = (String) req.get("damageDescription");
        return borrowService.returnEquipment(id, verifierId, returnResult, returnLocation, accessoriesInfo, damageDescription);
    }

    @PostMapping("/{id}/apply-return")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','EQUIPMENT_ADMIN','TEACHER','STUDENT')")
    public EquipmentBorrowRecordDTO applyReturn(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        String expectedReturnTime = (String) req.get("expectedReturnTime");
        String expectedReturnLocation = (String) req.get("expectedReturnLocation");
        return borrowService.applyReturn(id, expectedReturnTime, expectedReturnLocation);
    }

    @PostMapping("/{id}/renewal")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','EQUIPMENT_ADMIN','TEACHER')")
    public EquipmentBorrowRecordDTO applyRenewal(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        LocalDate newReturnDate = LocalDate.parse((String) req.get("newReturnDate"));
        String remark = (String) req.get("remark");
        return borrowService.applyRenewal(id, newReturnDate, remark);
    }

    @PostMapping("/{id}/approve-renewal")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','EQUIPMENT_ADMIN')")
    public EquipmentBorrowRecordDTO approveRenewal(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        Long approverId = Long.valueOf(req.get("approverId").toString());
        boolean approved = Boolean.parseBoolean(req.get("approved").toString());
        String approveRemark = (String) req.get("approveRemark");
        return borrowService.approveRenewal(id, approverId, approved, approveRemark);
    }

    @PostMapping("/{id}/overdue-warning")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','EQUIPMENT_ADMIN')")
    public EquipmentBorrowRecordDTO markOverdueWarning(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        String warningRemark = (String) req.get("warningRemark");
        return borrowService.markOverdueWarning(id, warningRemark);
    }

    @PostMapping("/{id}/update-return-date")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','EQUIPMENT_ADMIN')")
    public EquipmentBorrowRecordDTO updateReturnDate(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        LocalDate newReturnDate = LocalDate.parse((String) req.get("newReturnDate"));
        return borrowService.updateReturnDate(id, newReturnDate);
    }
}