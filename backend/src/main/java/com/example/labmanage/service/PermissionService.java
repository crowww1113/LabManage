package com.example.labmanage.service;

import com.example.labmanage.dto.PermissionRequest;
import com.example.labmanage.dto.PermissionResponse;
import com.example.labmanage.entity.PermissionEntity;
import com.example.labmanage.exception.NotFoundException;
import com.example.labmanage.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public List<PermissionResponse> list(String keyword) {
        List<PermissionEntity> list;
        if (keyword == null || keyword.isBlank()) {
            list = permissionRepository.findAll();
        } else {
            list = permissionRepository.findByModuleContainingOrNameContainingOrActionContaining(keyword, keyword, keyword);
        }
        return list.stream().map(this::toResponse).toList();
    }

    public PermissionResponse getById(long id) {
        PermissionEntity entity = permissionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("权限不存在: " + id));
        return toResponse(entity);
    }

    public PermissionResponse create(PermissionRequest req) {
        validateUnique(req, -1L);

        PermissionEntity entity = new PermissionEntity();
        copy(req, entity, true);
        return toResponse(permissionRepository.save(Objects.requireNonNull(entity, "权限实体不能为空")));
    }

    public PermissionResponse update(long id, PermissionRequest req) {
        PermissionEntity entity = permissionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("权限不存在: " + id));

        validateUnique(req, id);
        copy(req, entity, false);
        return toResponse(permissionRepository.save(Objects.requireNonNull(entity, "权限实体不能为空")));
    }

    public void delete(long id) {
        if (!permissionRepository.existsById(id)) {
            throw new NotFoundException("权限不存在: " + id);
        }
        permissionRepository.deleteById(id);
    }

    private void validateUnique(PermissionRequest req, long id) {
        boolean exists = permissionRepository.existsByModuleAndNameAndActionAndIdNot(
                req.getModule(), req.getName(), req.getAction(), id
        );
        if (exists) {
            throw new IllegalArgumentException("同一模块下已存在同名同操作类型权限");
        }
    }

    private void copy(PermissionRequest req, PermissionEntity entity, boolean createMode) {
        entity.setModule(req.getModule().trim());
        entity.setName(req.getName().trim());
        entity.setAction(req.getAction().trim());
        entity.setStatus(req.getStatus().trim());
        entity.setDescription(req.getDescription());

        if (createMode) {
            String code = (req.getCode() == null || req.getCode().isBlank())
                    ? "perm:" + UUID.randomUUID().toString().replace("-", "")
                    : req.getCode().trim();
            entity.setCode(code);
        } else if (req.getCode() != null && !req.getCode().isBlank()) {
            entity.setCode(req.getCode().trim());
        }
    }

    private PermissionResponse toResponse(PermissionEntity entity) {
        PermissionResponse resp = new PermissionResponse();
        resp.setId(entity.getId());
        resp.setModule(entity.getModule());
        resp.setName(entity.getName());
        resp.setAction(entity.getAction());
        resp.setCode(entity.getCode());
        resp.setStatus(entity.getStatus());
        resp.setDescription(entity.getDescription());
        resp.setCreatedAt(entity.getCreatedAt());
        resp.setUpdatedAt(entity.getUpdatedAt());
        return resp;
    }
}
