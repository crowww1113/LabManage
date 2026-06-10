package com.example.labmanage.service;

import com.example.labmanage.dto.RoleRequest;
import com.example.labmanage.dto.RoleResponse;
import com.example.labmanage.entity.RoleEntity;
import com.example.labmanage.entity.RolePermissionEntity;
import com.example.labmanage.exception.NotFoundException;
import com.example.labmanage.repository.RolePermissionRepository;
import com.example.labmanage.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;

    public List<RoleResponse> list() {
        return roleRepository.findAll().stream().map(this::toResponse).toList();
    }

    public RoleResponse getById(long id) {
        return toResponse(roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("角色不存在: " + id)));
    }

    @Transactional
    public RoleResponse create(RoleRequest req) {
        if (roleRepository.findByCode(req.getCode()).isPresent()) {
            throw new IllegalArgumentException("角色编码已存在");
        }
        RoleEntity entity = new RoleEntity();
        copy(req, entity);
        RoleEntity saved = roleRepository.save(Objects.requireNonNull(entity, "角色实体不能为空"));
        savePermissions(requireId(saved.getId(), "角色ID为空"), req.getPermissions());
        return toResponse(saved);
    }

    @Transactional
    public RoleResponse update(long id, RoleRequest req) {
        RoleEntity entity = roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("角色不存在: " + id));
        if (roleRepository.existsByCodeAndIdNot(req.getCode(), id)) {
            throw new IllegalArgumentException("角色编码已存在");
        }
        copy(req, entity);
        RoleEntity saved = roleRepository.save(Objects.requireNonNull(entity, "角色实体不能为空"));
        savePermissions(requireId(saved.getId(), "角色ID为空"), req.getPermissions());
        return toResponse(saved);
    }

    @Transactional
    public void delete(long id) {
        if (!roleRepository.existsById(id)) {
            throw new NotFoundException("角色不存在: " + id);
        }
        rolePermissionRepository.deleteByRoleId(id);
        roleRepository.deleteById(id);
    }

    private void copy(RoleRequest req, RoleEntity entity) {
        entity.setCode(req.getCode().trim());
        entity.setName(req.getName().trim());
        entity.setParentRoleId(req.getParentRoleId());
        entity.setDataScope(req.getDataScope());
        entity.setOrgLimit(req.getOrgLimit());

        // 计算层级
        if (req.getParentRoleId() != null) {
            long parentRoleId = req.getParentRoleId();
            roleRepository.findById(parentRoleId).ifPresent(parent ->
                    entity.setLevel((parent.getLevel() == null ? 1 : parent.getLevel()) + 1));
        } else {
            entity.setLevel(1);
        }
    }

    private void savePermissions(long roleId, List<String> permissionCodes) {
        rolePermissionRepository.deleteByRoleId(roleId);
        rolePermissionRepository.flush();

        if (permissionCodes == null || permissionCodes.isEmpty()) return;

        for (String code : permissionCodes.stream().filter(Objects::nonNull).distinct().toList()) {
            RolePermissionEntity rp = new RolePermissionEntity();
            rp.setRoleId(roleId);
            rp.setPermissionCode(code);
            rolePermissionRepository.save(rp);
        }
    }

    private RoleResponse toResponse(RoleEntity entity) {
        RoleResponse resp = new RoleResponse();
        resp.setId(entity.getId());
        resp.setCode(entity.getCode());
        resp.setName(entity.getName());
        resp.setParentRoleId(entity.getParentRoleId());
        resp.setLevel(entity.getLevel());
        resp.setDataScope(entity.getDataScope());
        resp.setOrgLimit(entity.getOrgLimit());
        resp.setCreatedAt(entity.getCreatedAt());
        resp.setUpdatedAt(entity.getUpdatedAt());

        List<String> perms = rolePermissionRepository.findByRoleId(requireId(entity.getId(), "角色ID为空"))
                .stream().map(RolePermissionEntity::getPermissionCode).toList();
        resp.setPermissions(perms.isEmpty() ? Collections.emptyList() : perms);

        return resp;
    }

    private long requireId(Long id, String message) {
        return Objects.requireNonNull(id, message);
    }
}
