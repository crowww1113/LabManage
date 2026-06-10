package com.example.labmanage.repository;

import com.example.labmanage.entity.ScheduleNoticeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleNoticeRepository extends JpaRepository<ScheduleNoticeEntity, Long> {
    List<ScheduleNoticeEntity> findByReceiverIdAndDeletedFalse(Long receiverId);

    List<ScheduleNoticeEntity> findByReceiverIdAndReadStatusAndDeletedFalse(Long receiverId, String readStatus);

    boolean existsByBizTypeAndBizIdAndNoticeTypeAndDeletedFalse(String bizType, Long bizId, String noticeType);
}
