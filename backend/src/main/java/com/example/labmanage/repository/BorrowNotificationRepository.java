package com.example.labmanage.repository;

import com.example.labmanage.entity.BorrowNotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BorrowNotificationRepository extends JpaRepository<BorrowNotificationEntity, Long> {

    List<BorrowNotificationEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<BorrowNotificationEntity> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    @Modifying
    @Query("UPDATE BorrowNotificationEntity n SET n.isRead = true WHERE n.userId = :userId")
    void markAllReadByUserId(@Param("userId") Long userId);

    long countByUserIdAndIsReadFalse(Long userId);
}
