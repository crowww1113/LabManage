package com.example.labmanage.service;

import com.example.labmanage.dto.ScheduleOperationLogDTO;
import com.example.labmanage.entity.ScheduleOperationLogEntity;
import com.example.labmanage.exception.NotFoundException;
import com.example.labmanage.repository.ScheduleOperationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleOperationLogService {
    private final ScheduleOperationLogRepository repository;

    public List<ScheduleOperationLogDTO> list(String bizType, Long bizId, Long operatorId) {
        List<ScheduleOperationLogEntity> list;
        if (bizType != null && bizId != null) {
            list = repository.findByBizTypeAndBizIdAndDeletedFalse(bizType, bizId);
        } else if (operatorId != null) {
            list = repository.findByOperatorIdAndDeletedFalse(operatorId);
        } else {
            list = repository.findAll().stream().filter(entity -> !Boolean.TRUE.equals(entity.getDeleted())).toList();
        }
        return list.stream().map(this::toDTO).toList();
    }

    public ScheduleOperationLogDTO getById(Long id) {
        ScheduleOperationLogEntity entity = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("操作日志不存在"));
        if (Boolean.TRUE.equals(entity.getDeleted())) {
            throw new NotFoundException("操作日志不存在");
        }
        return toDTO(entity);
    }

    @Transactional
    public void createLog(String bizType, Long bizId, String operationType, Long operatorId,
                          String operatorRoleCode, String beforeStatus, String afterStatus,
                          String operationContent, String operationResult) {
        ScheduleOperationLogEntity entity = new ScheduleOperationLogEntity();
        entity.setBizType(bizType);
        entity.setBizId(bizId);
        entity.setOperationType(operationType);
        entity.setOperatorId(operatorId == null ? 0L : operatorId);
        entity.setOperatorRoleCode(operatorRoleCode == null ? "SYSTEM" : operatorRoleCode);
        entity.setBeforeStatus(beforeStatus);
        entity.setAfterStatus(afterStatus);
        entity.setOperationContent(operationContent);
        entity.setOperationResult(operationResult);
        entity.setStatus("SUCCESS");
        entity.setDeleted(false);
        repository.save(entity);
    }

    private ScheduleOperationLogDTO toDTO(ScheduleOperationLogEntity entity) {
        ScheduleOperationLogDTO dto = new ScheduleOperationLogDTO();
        dto.setId(entity.getId());
        dto.setBizType(entity.getBizType());
        dto.setBizId(entity.getBizId());
        dto.setOperationType(entity.getOperationType());
        dto.setOperatorId(entity.getOperatorId());
        dto.setOperatorRoleCode(entity.getOperatorRoleCode());
        dto.setBeforeStatus(entity.getBeforeStatus());
        dto.setAfterStatus(entity.getAfterStatus());
        dto.setOperationContent(entity.getOperationContent());
        dto.setOperationResult(entity.getOperationResult());
        dto.setIpAddress(entity.getIpAddress());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
