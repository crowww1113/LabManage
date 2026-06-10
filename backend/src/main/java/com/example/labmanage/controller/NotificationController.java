package com.example.labmanage.controller;

import com.example.labmanage.dto.NotificationDTO;
import com.example.labmanage.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('LAB_ADMIN','EQUIPMENT_ADMIN','TEACHER')")
    public List<NotificationDTO> listAll(
            @RequestParam(required = false) Long receiverId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean unread) {
        if (receiverId != null) {
            if (Boolean.TRUE.equals(unread)) {
                return notificationService.listUnreadByReceiver(receiverId);
            }
            return notificationService.listByReceiver(receiverId);
        }
        if (type != null && !type.isEmpty()) {
            return notificationService.listByType(type);
        }
        return notificationService.listAll();
    }

    @PostMapping
    public NotificationDTO createNotification(@RequestBody NotificationDTO req) {
        return notificationService.createNotification(
                req.getReceiverId(),
                req.getReceiverName(),
                req.getType(),
                req.getTitle(),
                req.getContent()
        );
    }

    @PutMapping("/{id}/read")
    public NotificationDTO markAsRead(@PathVariable Long id) {
        return notificationService.markAsRead(id);
    }

    @PostMapping("/mark-all-read")
    public Map<String, Object> markAllAsRead(@RequestBody Map<String, Long> req) {
        Long receiverId = req.get("receiverId");
        int count = notificationService.markAllAsRead(receiverId);
        return Map.of("count", count);
    }
}
