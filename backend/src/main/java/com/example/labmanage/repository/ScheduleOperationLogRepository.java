package com.example.labmanage.repository;

import com.example.labmanage.entity.ScheduleOperationLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleOperationLogRepository extends JpaRepository<ScheduleOperationLogEntity, Long> {
    List<ScheduleOperationLogEntity> findByBizTypeAndBizIdAndDeletedFalse(String bizType, Long bizId);

    List<ScheduleOperationLogEntity> findByOperatorIdAndDeletedFalse(Long operatorId);
}
