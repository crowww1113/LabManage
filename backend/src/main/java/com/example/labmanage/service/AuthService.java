package com.example.labmanage.service;

import com.example.labmanage.dto.LoginRequest;
import com.example.labmanage.dto.LoginResponse;
import com.example.labmanage.dto.RegisterRequest;
import com.example.labmanage.dto.UserResponse;
import com.example.labmanage.entity.RoleEntity;
import com.example.labmanage.entity.UserEntity;
import com.example.labmanage.entity.UserRoleEntity;
import com.example.labmanage.repository.RoleRepository;
import com.example.labmanage.repository.UserRepository;
import com.example.labmanage.repository.UserRoleRepository;
import com.example.labmanage.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * 登录：校验账号密码，返回 JWT Token
     */
    public LoginResponse login(LoginRequest req) {
        UserEntity user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("账号或密码错误"));

        if (!"启用".equals(user.getStatus())) {
            throw new IllegalArgumentException("账号已被禁用，请联系管理员");
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("账号或密码错误");
        }

        List<String> roleCodes = getUserRoleCodes(requireId(user.getId(), "用户ID为空"));
        String token = jwtUtil.generateToken(user.getUsername(), roleCodes);

        // 获取角色名称
        String roleCode = roleCodes.isEmpty() ? "STUDENT" : roleCodes.get(0);
        String roleName = getRoleNameByCode(roleCode);

        return new LoginResponse(token, user.getUsername(), user.getRealName(), roleCodes, roleName, roleCode);
    }

    private String getRoleNameByCode(String code) {
        return switch (code) {
            case "LAB_ADMIN" -> "实验管理员";
            case "TEACHER" -> "授课教师";
            case "STUDENT" -> "学生";
            default -> "用户";
        };
    }

    /**
     * 注册：按所选角色分配账号
     */
    @Transactional
    public UserResponse register(RegisterRequest req) {
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            throw new IllegalArgumentException("账号已存在，请更换账号");
        }

        UserEntity user = new UserEntity();
        user.setUsername(req.getUsername().trim());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRealName(req.getRealName().trim());
        user.setOrgId(req.getOrgId());
        user.setPosition(req.getPosition());
        user.setJobNo(req.getJobNo());
        user.setStatus("启用");

        UserEntity saved = userRepository.save(user);

        RoleEntity role = roleRepository.findByCode(req.getRoleCode())
                .orElseThrow(() -> new IllegalArgumentException("角色不存在"));
        UserRoleEntity rel = new UserRoleEntity();
        rel.setUserId(requireId(saved.getId(), "用户ID为空"));
        rel.setRoleId(requireId(role.getId(), "角色ID为空"));
        userRoleRepository.save(rel);

        UserResponse resp = new UserResponse();
        resp.setId(requireId(saved.getId(), "用户ID为空"));
        resp.setUsername(saved.getUsername());
        resp.setRealName(saved.getRealName());
        resp.setOrgId(saved.getOrgId());
        resp.setPosition(saved.getPosition());
        resp.setJobNo(saved.getJobNo());
        resp.setStatus(saved.getStatus());
        resp.setCreatedAt(saved.getCreatedAt());
        resp.setUpdatedAt(saved.getUpdatedAt());
        resp.setRoleIds(userRoleRepository.findByUserId(requireId(saved.getId(), "用户ID为空"))
                .stream().map(UserRoleEntity::getRoleId).toList());
        return resp;
    }

    /**
     * 获取当前登录用户信息（通过 Token 中的用户名）
     */
    public LoginResponse currentUser(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        List<String> roleCodes = getUserRoleCodes(requireId(user.getId(), "用户ID为空"));
        // 不重新生成 token，只返回用户信息
        String roleCode = roleCodes.isEmpty() ? "STUDENT" : roleCodes.get(0);
        String roleName = getRoleNameByCode(roleCode);
        return new LoginResponse(null, user.getUsername(), user.getRealName(), roleCodes, roleName, roleCode);
    }

    private List<String> getUserRoleCodes(Long userId) {
        return userRoleRepository.findByUserId(userId).stream()
                .map(UserRoleEntity::getRoleId)
                .map(roleId -> roleRepository.findById(requireId(roleId, "角色ID为空"))
                        .map(RoleEntity::getCode)
                        .orElse(null))
                .filter(code -> code != null)
                .toList();
    }

    private long requireId(Long id, String message) {
        return Objects.requireNonNull(id, message);
    }
}
