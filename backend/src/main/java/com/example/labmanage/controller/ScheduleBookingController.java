package com.example.labmanage.controller;

import com.example.labmanage.dto.*;
import com.example.labmanage.repository.UserRepository;
import com.example.labmanage.service.ScheduleBookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedule-bookings")
@RequiredArgsConstructor
public class ScheduleBookingController {
    private final ScheduleBookingService service;
    private final UserRepository userRepository;

    @GetMapping
    public List<ScheduleBookingDTO> list(
            @RequestParam(required = false) Long termId,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) String status
    ) {
        return service.list(termId, teacherId, status);
    }

    @GetMapping("/{id}")
    public ScheduleBookingDTO get(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','TEACHER')")
    public BookingBatchScheduleResponse batchCreate(@Valid @RequestBody BatchScheduleRequest req) {
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_LAB_ADMIN"));
        return service.batchCreate(req, getCurrentUserId(), isAdmin);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('LAB_ADMIN')")
    public ScheduleBookingDTO approve(@PathVariable Long id) {
        return service.approve(id, getCurrentUserId());
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('LAB_ADMIN')")
    public ScheduleBookingDTO reject(@PathVariable Long id, @RequestBody BookingApprovalRequest req) {
        return service.reject(id, getCurrentUserId(), req.getReason());
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('LAB_ADMIN')")
    public ScheduleBookingDTO cancel(@PathVariable Long id, @RequestBody BookingApprovalRequest req) {
        return service.cancel(id, getCurrentUserId(), req.getReason());
    }

    private Long getCurrentUserId() {
        org.springframework.security.core.Authentication auth =
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || "anonymousUser".equals(auth.getName())) {
            System.err.println("⚠️ 警告：检测到匿名访问或非法 Token！已启用开发兜底模式，默认操作人 ID 设为 1L");
            return 1L;
        }
        return userRepository.findByUsername(auth.getName())
                .map(com.example.labmanage.entity.UserEntity::getId)
                .orElseGet(() -> {
                    System.err.println("⚠️ 警告：Token 中的用户名 " + auth.getName() + " 在数据库查无此人！兜底返回 1L");
                    return 1L;
                });
    }
}
