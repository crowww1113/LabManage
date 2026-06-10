package com.example.labmanage.controller;

import com.example.labmanage.dto.ScheduleUsageRecordDTO;
import com.example.labmanage.repository.UserRepository;
import com.example.labmanage.service.ScheduleUsageRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedule-usage-records")
@RequiredArgsConstructor
public class ScheduleUsageRecordController {
    private final ScheduleUsageRecordService service;
    private final UserRepository userRepository;

    @GetMapping
    public List<ScheduleUsageRecordDTO> list() {
        return service.list();
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','TEACHER')")
    public List<ScheduleUsageRecordDTO> mine() {
        return service.listMine(getCurrentUserId());
    }

    @GetMapping("/init")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','TEACHER')")
    public ScheduleUsageRecordDTO initForm(@RequestParam Long reservationId) {
        return service.initForm(reservationId, getCurrentUserId());
    }

    @GetMapping("/{id}")
    public ScheduleUsageRecordDTO get(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','TEACHER')")
    public ScheduleUsageRecordDTO submit(@PathVariable Long id, @RequestBody ScheduleUsageRecordDTO dto) {
        return service.submit(id, dto, getCurrentUserId());
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || "anonymousUser".equals(auth.getName())) {
            return 1L;
        }
        return userRepository.findByUsername(auth.getName())
                .map(com.example.labmanage.entity.UserEntity::getId)
                .orElse(1L);
    }
}
