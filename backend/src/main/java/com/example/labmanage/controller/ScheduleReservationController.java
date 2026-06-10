package com.example.labmanage.controller;

import com.example.labmanage.dto.BatchScheduleRequest;
import com.example.labmanage.dto.BatchScheduleResponse;
import com.example.labmanage.dto.IdOperatorRequest;
import com.example.labmanage.dto.LabAvailabilityItem;
import com.example.labmanage.dto.ReasonActionRequest;
import com.example.labmanage.dto.ScheduleReservationDTO;
import com.example.labmanage.dto.UpdateScheduleRequest;
import com.example.labmanage.repository.UserRepository;
import com.example.labmanage.service.ScheduleReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedule-reservations")
@RequiredArgsConstructor
public class ScheduleReservationController {
    private final ScheduleReservationService service;
    private final UserRepository userRepository;

    @GetMapping
    public List<ScheduleReservationDTO> list(
            @RequestParam(required = false) Long termId,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) Long clazzId
    ) {
        return service.list(termId, teacherId, clazzId);
    }

    @GetMapping("/{id}")
    public ScheduleReservationDTO get(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('LAB_ADMIN','TEACHER','STUDENT')")
    public ScheduleReservationDTO create(@Valid @RequestBody ScheduleReservationDTO dto) {
        // 自动填充申请人ID（前端不传，后端通过Token解析）
        if (dto.getTeacherId() == null) {
            dto.setTeacherId(getCurrentUserId());
        }
        return service.create(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('LAB_ADMIN')")
    public ScheduleReservationDTO updateReservation(@PathVariable Long id, @Valid @RequestBody UpdateScheduleRequest request) {
        return service.updateReservation(id, request, getCurrentUserId());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('LAB_ADMIN')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/labs/availability")
    public List<LabAvailabilityItem> getLabAvailability(
            @RequestParam Long termId,
            @RequestParam List<Integer> weeks,
            @RequestParam Integer dayOfWeek,
            @RequestParam List<Long> timeSlotIds,
            @RequestParam(required = false) String buildingName,
            @RequestParam(required = false) Long excludeReservationId
    ) {
        return service.getLabAvailability(termId, weeks, dayOfWeek, timeSlotIds, buildingName, excludeReservationId);
    }

    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','TEACHER')")
    public BatchScheduleResponse batchCreate(@Valid @RequestBody BatchScheduleRequest req) {
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_LAB_ADMIN"));
        return service.batchCreate(req, getCurrentUserId(), isAdmin);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('LAB_ADMIN')")
    public ScheduleReservationDTO approve(@PathVariable Long id, @Valid @RequestBody IdOperatorRequest req) {
        return service.approve(id, getCurrentUserId());
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('LAB_ADMIN')")
    public ScheduleReservationDTO reject(@PathVariable Long id, @Valid @RequestBody ReasonActionRequest req) {
        return service.reject(id, getCurrentUserId(), req.getReason());
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('LAB_ADMIN')")
    public ScheduleReservationDTO cancel(@PathVariable Long id, @Valid @RequestBody ReasonActionRequest req) {
        return service.cancel(id, getCurrentUserId(), req.getReason());
    }

    @PostMapping("/{id}/start-use")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','TEACHER')")
    public ScheduleReservationDTO startUse(@PathVariable Long id, @Valid @RequestBody IdOperatorRequest req) {
        return service.startUse(id, getCurrentUserId());
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','TEACHER')")
    public ScheduleReservationDTO complete(@PathVariable Long id, @Valid @RequestBody IdOperatorRequest req) {
        return service.complete(id, getCurrentUserId());
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
