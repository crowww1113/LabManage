package com.example.labmanage.service;

import com.example.labmanage.dto.OperationLogDTO;
import com.example.labmanage.entity.OperationLogEntity;
import com.example.labmanage.repository.OperationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogRepository logRepository;

    public List<OperationLogDTO> listAll() {
        return logRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<OperationLogDTO> listByModule(String module) {
        return logRepository.findByModuleOrderByCreateTimeDesc(module).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<OperationLogDTO> listByType(String type) {
        return logRepository.findByOperationTypeOrderByCreateTimeDesc(type).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public OperationLogDTO logOperation(Long operatorId, String operatorName, String module, String operationType, String content, String ipAddress) {
        OperationLogEntity entity = new OperationLogEntity();
        entity.setOperatorId(operatorId);
        entity.setOperatorName(operatorName);
        entity.setModule(module);
        entity.setOperationType(operationType);
        entity.setContent(content);
        entity.setIpAddress(ipAddress);
        entity.setCreateTime(LocalDateTime.now());
        logRepository.save(entity);
        return toDTO(entity);
    }

    private OperationLogDTO toDTO(OperationLogEntity entity) {
        OperationLogDTO dto = new OperationLogDTO();
        dto.setId(entity.getId());
        dto.setOperatorId(entity.getOperatorId());
        dto.setOperatorName(entity.getOperatorName());
        dto.setModule(entity.getModule());
        dto.setOperationType(entity.getOperationType());
        dto.setContent(entity.getContent());
        dto.setIpAddress(entity.getIpAddress());
        dto.setCreateTime(entity.getCreateTime());
        return dto;
    }
}
