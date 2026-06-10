package com.example.labmanage.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.example.labmanage.service.AuthService;
import com.example.labmanage.dto.LoginRequest;
import com.example.labmanage.dto.LoginResponse;
import com.example.labmanage.dto.RegisterRequest;
import com.example.labmanage.dto.UserResponse;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 登录接口
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    /**
     * 注册接口（自助注册，默认分配学生角色）
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public UserResponse register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    /**
     * 获取当前登录用户信息
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public LoginResponse me(@AuthenticationPrincipal String username) {
        return authService.currentUser(username);
    }

    /**
     * 登出（前端清除 Token 即可，后端返回成功提示）
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public Map<String, Object> logout() {
        return Map.of("code", 200, "message", "已退出登录");
    }
}
