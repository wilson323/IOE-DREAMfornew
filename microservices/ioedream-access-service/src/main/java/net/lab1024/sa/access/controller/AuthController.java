package net.lab1024.sa.access.controller;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import net.lab1024.sa.common.domain.ResponseDTO;

/**
 * 认证Controller
 *
 * @author SmartAdmin Team
 * @date 2025-11-15
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "用户认证", description = "用户登录认证相关接口")
@Slf4j
public class AuthController {

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录接口")
    public ResponseDTO<Map<String, Object>> login(
            @Valid @RequestBody Map<String, Object> loginRequest) {
        Map<String, Object> result = new HashMap<>();
        result.put("token", "mock-jwt-token-" + System.currentTimeMillis());
        result.put("userId", 1L);
        result.put("userName", "admin");
        result.put("realName", "管理员");
        result.put("expireTime", System.currentTimeMillis() + 24 * 60 * 60 * 1000); // 24小时

        return ResponseDTO.ok(result);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户登出接口")
    public ResponseDTO<Void> logout() {
        return ResponseDTO.ok();
    }

    @GetMapping("/info")
    @Operation(summary = "获取用户信息", description = "获取当前登录用户信息")
    public ResponseDTO<Map<String, Object>> getUserInfo() {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", 1L);
        userInfo.put("userName", "admin");
        userInfo.put("realName", "管理员");
        userInfo.put("gender", 1);
        userInfo.put("email", "admin@smartadmin.com");
        userInfo.put("phone", "13800138000");
        userInfo.put("avatar", "");
        userInfo.put("status", 1);
        userInfo.put("createTime", "2025-01-01 00:00:00");

        Map<String, Object> roles = new HashMap<>();
        roles.put("roleName", "管理员");
        roles.put("roleCode", "ADMIN");
        userInfo.put("roles", roles);

        return ResponseDTO.ok(userInfo);
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新Token", description = "刷新用户访问令牌")
    public ResponseDTO<Map<String, Object>> refreshToken() {
        Map<String, Object> result = new HashMap<>();
        result.put("token", "mock-jwt-token-refresh-" + System.currentTimeMillis());
        result.put("expireTime", System.currentTimeMillis() + 24 * 60 * 60 * 1000); // 24小时

        return ResponseDTO.ok(result);
    }
}