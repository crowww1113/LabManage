package com.example.labmanage.controller;

import com.example.labmanage.dto.ScheduleNoticeDTO;
import com.example.labmanage.service.ScheduleNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedule-notices")
@RequiredArgsConstructor
public class ScheduleNoticeController {
    private final ScheduleNoticeService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','LAB_ADMIN','TEACHER','STUDENT')")
    public List<ScheduleNoticeDTO> list(
            @RequestParam(required = false) Long receiverId,
            @RequestParam(required = false) String readStatus
    ) {
        return service.list(receiverId, readStatus);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','LAB_ADMIN','TEACHER','STUDENT')")
    public ScheduleNoticeDTO get(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','LAB_ADMIN','TEACHER','STUDENT')")
    public ScheduleNoticeDTO markAsRead(@PathVariable Long id) {
        return service.markAsRead(id);
    }
}
