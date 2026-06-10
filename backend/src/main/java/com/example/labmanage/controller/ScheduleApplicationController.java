package com.example.labmanage.controller;

import com.example.labmanage.dto.IdOperatorRequest;
import com.example.labmanage.dto.ReviewActionRequest;
import com.example.labmanage.dto.ScheduleApplicationDTO;
import com.example.labmanage.repository.UserRepository;
import com.example.labmanage.service.ScheduleApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedule-applications")
@RequiredArgsConstructor
public class ScheduleApplicationController {
    private final ScheduleApplicationService service;
    private final UserRepository userRepository;

    @GetMapping
    public List<ScheduleApplicationDTO> list(
            @RequestParam(required = false) Long termId,
            @RequestParam(required = false) Long teacherId
    ) {
        return service.list(termId, teacherId);
    }

    @GetMapping("/{id}")
    public ScheduleApplicationDTO get(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER','LAB_ADMIN','SUPER_ADMIN')")
    public ScheduleApplicationDTO create(@Valid @RequestBody ScheduleApplicationDTO dto) {
        Long currentUserId = getCurrentUserId();
        return service.create(dto, currentUserId);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER','LAB_ADMIN','SUPER_ADMIN')")
    public ScheduleApplicationDTO update(@PathVariable Long id, @Valid @RequestBody ScheduleApplicationDTO dto) {
        return service.update(id, dto);
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('TEACHER')")
    public ScheduleApplicationDTO submit(@PathVariable Long id) {
        return service.submit(id);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('LAB_ADMIN')")
    public ScheduleApplicationDTO approve(@PathVariable Long id, @Valid @RequestBody ReviewActionRequest req) {
        return service.approve(id, req.getReviewComment(), getCurrentUserId());
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('LAB_ADMIN')")
    public ScheduleApplicationDTO reject(@PathVariable Long id, @Valid @RequestBody ReviewActionRequest req) {
        return service.reject(id, req.getReviewComment(), getCurrentUserId());
    }

    @PostMapping("/{id}/withdraw")
    @PreAuthorize("hasRole('TEACHER')")
    public ScheduleApplicationDTO withdraw(@PathVariable Long id, @Valid @RequestBody IdOperatorRequest req) {
        return service.withdraw(id, getCurrentUserId());
    }

    private Long getCurrentUserId() {
        org.springframework.security.core.Authentication auth =
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

        // 1. 匿名访问或非法 Token → 开发兜底模式
        if (auth == null || "anonymousUser".equals(auth.getName())) {
            System.err.println("⚠️ 警告：检测到匿名访问或非法 Token！已启用开发兜底模式，默认操作人 ID 设为 1L");
            return 1L;
        }

        // 2. 真 Token → 查库
        return userRepository.findByUsername(auth.getName())
                .map(com.example.labmanage.entity.UserEntity::getId)
                .orElseGet(() -> {
                    System.err.println("⚠️ 警告：Token 中的用户名 " + auth.getName() + " 在数据库查无此人！兜底返回 1L");
                    return 1L;
                });
    }
}
