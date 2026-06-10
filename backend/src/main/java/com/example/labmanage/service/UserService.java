package com.example.labmanage.service;

import com.example.labmanage.dto.UserRequest;
import com.example.labmanage.dto.UserResponse;
import com.example.labmanage.entity.UserEntity;
import com.example.labmanage.entity.UserRoleEntity;
import com.example.labmanage.exception.NotFoundException;
import com.example.labmanage.repository.UserRepository;
import com.example.labmanage.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    public List<UserResponse> list(String keyword) {
        List<UserEntity> users;
        if (keyword == null || keyword.isBlank()) {
            users = userRepository.findAll();
        } else {
            users = userRepository.findByUsernameContainingOrRealNameContaining(keyword, keyword);
        }
        return users.stream().map(this::toResponse).toList();
    }

    public UserResponse getById(long id) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("用户不存在: " + id));
        return toResponse(entity);
    }

    @Transactional
    public UserResponse create(UserRequest req) {
        userRepository.findByUsername(req.getUsername()).ifPresent(u -> {
            throw new IllegalArgumentException("登录账号已存在");
        });

        UserEntity entity = new UserEntity();
        copy(req, entity);
        UserEntity saved = userRepository.save(Objects.requireNonNull(entity, "用户实体不能为空"));
        saveRoles(requireId(saved.getId(), "用户ID为空"), req.getRoleIds());
        return toResponse(saved);
    }

    @Transactional
    public UserResponse update(long id, UserRequest req) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("用户不存在: " + id));

        if (userRepository.existsByUsernameAndIdNot(req.getUsername(), id)) {
            throw new IllegalArgumentException("登录账号已存在");
        }

        copy(req, entity);
        UserEntity saved = userRepository.save(Objects.requireNonNull(entity, "用户实体不能为空"));

        // 更新用户角色：先删除旧关联，再保存新角色
        saveRoles(requireId(saved.getId(), "用户ID为空"), req.getRoleIds());
        return toResponse(saved);
    }

    @Transactional
    public void delete(long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("用户不存在: " + id);
        }
        userRoleRepository.deleteByUserId(id);
        userRepository.deleteById(id);
    }

    private void saveRoles(long userId, List<Long> roleIds) {
        userRoleRepository.deleteByUserId(userId);
        userRoleRepository.flush();

        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }

        // 单角色约束：只取第一个角色
        Long primaryRoleId = roleIds.stream().filter(Objects::nonNull).distinct()
                .findFirst().orElse(null);
        if (primaryRoleId == null) return;

        UserRoleEntity rel = new UserRoleEntity();
        rel.setUserId(userId);
        rel.setRoleId(primaryRoleId);
        userRoleRepository.save(rel);
    }

    private void copy(UserRequest req, UserEntity entity) {
        entity.setUsername(req.getUsername().trim());
        entity.setRealName(req.getRealName().trim());
        entity.setOrgId(req.getOrgId());
        entity.setPosition(req.getPosition());
        entity.setJobNo(req.getJobNo());
        entity.setStatus(req.getStatus());
    }

    private UserResponse toResponse(UserEntity entity) {
        UserResponse resp = new UserResponse();
        resp.setId(entity.getId());
        resp.setUsername(entity.getUsername());
        resp.setRealName(entity.getRealName());
        resp.setOrgId(entity.getOrgId());
        resp.setPosition(entity.getPosition());
        resp.setJobNo(entity.getJobNo());
        resp.setStatus(entity.getStatus());
        resp.setCreatedAt(entity.getCreatedAt());
        resp.setUpdatedAt(entity.getUpdatedAt());

        List<Long> roleIds = userRoleRepository.findByUserId(requireId(entity.getId(), "用户ID为空"))
                .stream().map(UserRoleEntity::getRoleId).toList();
        resp.setRoleIds(roleIds.isEmpty() ? Collections.emptyList() : roleIds);

        return resp;
    }

    private long requireId(Long id, String message) {
        return Objects.requireNonNull(id, message);
    }
}
