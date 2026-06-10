package com.example.labmanage.repository;

import com.example.labmanage.entity.ScheduleUsageRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.labmanage.enums.ScheduleRegistrationStatusEnum;

import java.util.List;

public interface ScheduleUsageRecordRepository extends JpaRepository<ScheduleUsageRecordEntity, Long> {
    List<ScheduleUsageRecordEntity> findByDeletedFalse();

    List<ScheduleUsageRecordEntity> findByReservationIdAndDeletedFalse(Long reservationId);

    List<ScheduleUsageRecordEntity> findByReporterIdAndDeletedFalse(Long reporterId);

    List<ScheduleUsageRecordEntity> findByRecordStatusAndDeletedFalse(ScheduleRegistrationStatusEnum recordStatus);
}
