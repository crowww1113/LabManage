package com.example.labmanage.repository;

import com.example.labmanage.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    List<NotificationEntity> findByReceiverIdOrderByCreateTimeDesc(Long receiverId);
    List<NotificationEntity> findByReceiverIdAndIsReadOrderByCreateTimeDesc(Long receiverId, Boolean isRead);
    List<NotificationEntity> findByTypeOrderByCreateTimeDesc(String type);
}
