package com.example.labmanage.controller;

import com.example.labmanage.dto.ScheduleOperationLogDTO;
import com.example.labmanage.service.ScheduleOperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedule-operation-logs")
@RequiredArgsConstructor
public class ScheduleOperationLogController {
    private final ScheduleOperationLogService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','LAB_ADMIN')")
    public List<ScheduleOperationLogDTO> list(
            @RequestParam(required = false) String bizType,
            @RequestParam(required = false) Long bizId,
            @RequestParam(required = false) Long operatorId
    ) {
        return service.list(bizType, bizId, operatorId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','LAB_ADMIN')")
    public ScheduleOperationLogDTO get(@PathVariable Long id) {
        return service.getById(id);
    }
}
