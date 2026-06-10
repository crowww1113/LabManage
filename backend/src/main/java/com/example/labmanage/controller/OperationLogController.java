package com.example.labmanage.controller;

import com.example.labmanage.dto.OperationLogDTO;
import com.example.labmanage.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/operation-logs")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogService logService;

    @GetMapping
    @PreAuthorize("hasAnyRole('LAB_ADMIN','EQUIPMENT_ADMIN')")
    public List<OperationLogDTO> listAll(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String type) {
        if (module != null && !module.isEmpty()) {
            return logService.listByModule(module);
        }
        if (type != null && !type.isEmpty()) {
            return logService.listByType(type);
        }
        return logService.listAll();
    }

    @PostMapping
    public OperationLogDTO logOperation(@RequestBody OperationLogDTO req) {
        return logService.logOperation(
                req.getOperatorId(),
                req.getOperatorName(),
                req.getModule(),
                req.getOperationType(),
                req.getContent(),
                req.getIpAddress()
        );
    }
}
