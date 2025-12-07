package net.lab1024.sa.identity.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.identity.domain.vo.LoginRequest;
import net.lab1024.sa.identity.domain.vo.LoginResponse;
import net.lab1024.sa.identity.domain.vo.RefreshTokenRequest;
import net.lab1024.sa.identity.service.AuthenticationService;

/**
 * 认证控制器
 * 基于现有登录模式重构，支持微服务架构
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "认证管理", description = "用户认证相关接口")
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录获取访问令牌")
    public ResponseDTO<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        log.info("用户登录请求: username={}, ip={}", request.getUsername(), request.getLoginIp());
        return authenticationService.login(request);
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
    public ResponseDTO<LoginResponse> refreshToken(@RequestBody @Valid RefreshTokenRequest request) {
        log.info("开始刷新令牌");
        return authenticationService.refreshToken(request);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户登出清除会话")
    public ResponseDTO<Void> logout(@RequestParam Long userId) {
        log.info("用户登出请求: userId={}", userId);
        return authenticationService.logout(userId);
    }

    @PostMapping("/validate")
    @Operation(summary = "验证令牌", description = "验证访问令牌的有效性")
    public ResponseDTO<Boolean> validateToken(@RequestParam String token) {
        try {
            authenticationService.validateAccessToken(token);
            return ResponseDTO.ok(true);
        } catch (Exception e) {
            log.warn("令牌验证失败: {}", e.getMessage());
            return ResponseDTO.ok(false);
        }
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    @Operation(summary = "认证服务健康检查", description = "检查认证服务是否正常运行")
    public ResponseDTO<String> health() {
        return ResponseDTO.ok("Identity Service is running");
    }

    // 兼容性接口，保持与原有登录模式的兼容

    /**
     * 员工登录（兼容性接口）
     */
    @PostMapping("/employee/login")
    @Operation(summary = "员工登录", description = "员工登录（兼容性接口）")
    public ResponseDTO<LoginResponse> employeeLogin(@RequestBody @Valid LoginRequest request) {
        log.info("员工登录请求: username={}, ip={}", request.getUsername(), request.getLoginIp());
        return authenticationService.login(request);
    }
}
