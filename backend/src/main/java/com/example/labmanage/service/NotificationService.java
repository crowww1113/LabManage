package com.example.labmanage.service;

import com.example.labmanage.dto.NotificationDTO;
import com.example.labmanage.entity.NotificationEntity;
import com.example.labmanage.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public List<NotificationDTO> listAll() {
        return notificationRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<NotificationDTO> listByReceiver(Long receiverId) {
        return notificationRepository.findByReceiverIdOrderByCreateTimeDesc(receiverId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<NotificationDTO> listUnreadByReceiver(Long receiverId) {
        return notificationRepository.findByReceiverIdAndIsReadOrderByCreateTimeDesc(receiverId, false).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<NotificationDTO> listByType(String type) {
        return notificationRepository.findByTypeOrderByCreateTimeDesc(type).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public NotificationDTO createNotification(Long receiverId, String receiverName, String type, String title, String content) {
        NotificationEntity entity = new NotificationEntity();
        entity.setReceiverId(receiverId);
        entity.setReceiverName(receiverName);
        entity.setType(type);
        entity.setTitle(title);
        entity.setContent(content);
        entity.setIsRead(false);
        entity.setCreateTime(LocalDateTime.now());
        notificationRepository.save(entity);
        return toDTO(entity);
    }

    public NotificationDTO markAsRead(Long id) {
        NotificationEntity entity = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("通知不存在"));
        entity.setIsRead(true);
        notificationRepository.save(entity);
        return toDTO(entity);
    }

    public int markAllAsRead(Long receiverId) {
        List<NotificationEntity> unread = notificationRepository.findByReceiverIdAndIsReadOrderByCreateTimeDesc(receiverId, false);
        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread);
        return unread.size();
    }

    private NotificationDTO toDTO(NotificationEntity entity) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(entity.getId());
        dto.setReceiverId(entity.getReceiverId());
        dto.setReceiverName(entity.getReceiverName());
        dto.setType(entity.getType());
        dto.setTitle(entity.getTitle());
        dto.setContent(entity.getContent());
        dto.setIsRead(entity.getIsRead());
        dto.setCreateTime(entity.getCreateTime());
        return dto;
    }
}
