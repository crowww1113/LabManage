package com.example.labmanage.repository;

import com.example.labmanage.entity.OperationLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OperationLogRepository extends JpaRepository<OperationLogEntity, Long> {
    List<OperationLogEntity> findByOperatorIdOrderByCreateTimeDesc(Long operatorId);
    List<OperationLogEntity> findByModuleOrderByCreateTimeDesc(String module);
    List<OperationLogEntity> findByOperationTypeOrderByCreateTimeDesc(String operationType);
}
