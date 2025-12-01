package net.lab1024.sa.auth.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.auth.domain.vo.LoginRequest;
import net.lab1024.sa.auth.domain.vo.LoginResponse;
import net.lab1024.sa.auth.domain.vo.RefreshTokenRequest;
import net.lab1024.sa.auth.domain.vo.RegisterRequest;
import net.lab1024.sa.auth.service.AuthenticationService;
import net.lab1024.sa.auth.service.UserService;
import net.lab1024.sa.common.domain.ResponseDTO;

/**
 * 认证控制器
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户认证相关接口")
@Validated
public class AuthController {

    private final AuthenticationService authenticationService;
    private final UserService userService;

    /**
     * 用户登录
     *
     * @param request     登录请求
     * @param httpRequest HTTP请求对象
     * @return 登录响应
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户名密码登录获取访问令牌")
    public ResponseDTO<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        // 获取客户端IP
        String clientIp = getClientIpAddress(httpRequest);
        request.setLoginIp(clientIp);

        return authenticationService.login(request);
    }

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 响应结果
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册")
    public ResponseDTO<Void> register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    /**
     * 刷新令牌
     *
     * @param request 刷新令牌请求
     * @return 登录响应
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
    public ResponseDTO<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return authenticationService.refreshToken(request);
    }

    /**
     * 用户登出
     *
     * @param authorization 访问令牌
     * @return 响应结果
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户退出登录，清除会话")
    public ResponseDTO<Void> logout(
            @Parameter(description = "访问令牌", required = true) @RequestHeader("Authorization") String authorization) {

        try {
            // 从Authorization头中提取token
            String token = extractTokenFromAuthorization(authorization);
            Claims claims = authenticationService.validateAccessToken(token);
            Long userId = Long.parseLong(claims.getSubject());

            return authenticationService.logout(userId);

        } catch (Exception e) {
            log.warn("登出失败: {}", e.getMessage());
            return ResponseDTO.error("登出失败");
        }
    }

    /**
     * 验证令牌
     *
     * @param authorization 访问令牌
     * @return 验证结果
     */
    @GetMapping("/validate")
    @Operation(summary = "验证令牌", description = "验证访问令牌的有效性")
    public ResponseDTO<Map<String, Object>> validateToken(
            @Parameter(description = "访问令牌", required = true) @RequestHeader("Authorization") String authorization) {

        try {
            String token = extractTokenFromAuthorization(authorization);
            Claims claims = authenticationService.validateAccessToken(token);

            Map<String, Object> result = new HashMap<>();
            result.put("valid", true);
            result.put("userId", claims.getSubject());
            result.put("username", claims.get("username"));
            result.put("expiration", claims.getExpiration());

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.warn("令牌验证失败: {}", e.getMessage());
            return ResponseDTO.error("令牌无效");
        }
    }

    /**
     * 获取用户信息
     *
     * @param authorization 访问令牌
     * @return 用户信息
     */
    @GetMapping("/user-info")
    @Operation(summary = "获取用户信息", description = "根据访问令牌获取当前用户信息")
    public ResponseDTO<Map<String, Object>> getUserInfo(
            @Parameter(description = "访问令牌", required = true) @RequestHeader("Authorization") String authorization) {

        try {
            String token = extractTokenFromAuthorization(authorization);
            net.lab1024.sa.auth.domain.entity.UserEntity user = authenticationService.getUserByToken(token);

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", user.getUserId());
            userInfo.put("username", user.getUsername());
            userInfo.put("realName", user.getRealName());
            userInfo.put("email", user.getEmail());
            userInfo.put("phone", user.getPhone());
            userInfo.put("avatarUrl", user.getAvatarUrl());
            userInfo.put("status", user.getStatus());
            userInfo.put("lastLoginTime", user.getLastLoginTime());

            return ResponseDTO.ok(userInfo);

        } catch (Exception e) {
            log.warn("获取用户信息失败: {}", e.getMessage());
            return ResponseDTO.error("获取用户信息失败");
        }
    }

    /**
     * 检查权限
     *
     * @param permission    权限标识
     * @param authorization 访问令牌
     * @return 权限检查结果
     */
    @GetMapping("/check-permission")
    @Operation(summary = "检查权限", description = "检查当前用户是否具有指定权限")
    public ResponseDTO<Map<String, Object>> checkPermission(
            @Parameter(description = "权限标识", required = true) @RequestParam String permission,
            @Parameter(description = "访问令牌", required = true) @RequestHeader("Authorization") String authorization) {

        try {
            String token = extractTokenFromAuthorization(authorization);
            Claims claims = authenticationService.validateAccessToken(token);
            Long userId = Long.parseLong(claims.getSubject());

            boolean hasPermission = authenticationService.hasPermission(userId, permission);

            Map<String, Object> result = new HashMap<>();
            result.put("hasPermission", hasPermission);
            result.put("permission", permission);

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.warn("检查权限失败: {}", e.getMessage());
            return ResponseDTO.error("检查权限失败");
        }
    }

    /**
     * 检查角色
     *
     * @param role          角色标识
     * @param authorization 访问令牌
     * @return 角色检查结果
     */
    @GetMapping("/check-role")
    @Operation(summary = "检查角色", description = "检查当前用户是否具有指定角色")
    public ResponseDTO<Map<String, Object>> checkRole(
            @Parameter(description = "角色标识", required = true) @RequestParam String role,
            @Parameter(description = "访问令牌", required = true) @RequestHeader("Authorization") String authorization) {

        try {
            String token = extractTokenFromAuthorization(authorization);
            Claims claims = authenticationService.validateAccessToken(token);
            Long userId = Long.parseLong(claims.getSubject());

            boolean hasRole = authenticationService.hasRole(userId, role);

            Map<String, Object> result = new HashMap<>();
            result.put("hasRole", hasRole);
            result.put("role", role);

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.warn("检查角色失败: {}", e.getMessage());
            return ResponseDTO.error("检查角色失败");
        }
    }

    /**
     * 获取用户权限列表
     *
     * @param authorization 访问令牌
     * @return 权限列表
     */
    @GetMapping("/permissions")
    @Operation(summary = "获取用户权限", description = "获取当前用户的所有权限列表")
    public ResponseDTO<Map<String, Object>> getUserPermissions(
            @Parameter(description = "访问令牌", required = true) @RequestHeader("Authorization") String authorization) {

        try {
            String token = extractTokenFromAuthorization(authorization);
            Claims claims = authenticationService.validateAccessToken(token);
            Long userId = Long.parseLong(claims.getSubject());

            java.util.Set<String> permissions = authenticationService.getUserPermissions(userId);

            Map<String, Object> result = new HashMap<>();
            result.put("permissions", permissions);
            result.put("count", permissions.size());

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.warn("获取用户权限失败: {}", e.getMessage());
            return ResponseDTO.error("获取用户权限失败");
        }
    }

    /**
     * 修改密码
     *
     * @param oldPassword     当前密码
     * @param newPassword     新密码
     * @param confirmPassword 确认密码
     * @param authorization   访问令牌
     * @return 响应结果
     */
    @PostMapping("/change-password")
    @Operation(summary = "修改密码", description = "用户修改登录密码")
    public ResponseDTO<Void> changePassword(
            @Parameter(description = "当前密码", required = true) @RequestParam String oldPassword,
            @Parameter(description = "新密码", required = true) @RequestParam String newPassword,
            @Parameter(description = "确认密码", required = true) @RequestParam String confirmPassword,
            @Parameter(description = "访问令牌", required = true) @RequestHeader("Authorization") String authorization) {

        try {
            // 验证新密码和确认密码是否一致
            if (!newPassword.equals(confirmPassword)) {
                return ResponseDTO.error("新密码和确认密码不一致");
            }

            String token = extractTokenFromAuthorization(authorization);
            Claims claims = authenticationService.validateAccessToken(token);
            Long userId = Long.parseLong(claims.getSubject());

            return userService.changePassword(userId, oldPassword, newPassword);

        } catch (Exception e) {
            log.warn("修改密码失败: {}", e.getMessage());
            return ResponseDTO.error("修改密码失败");
        }
    }

    // 私有辅助方法

    /**
     * 从Authorization头中提取token
     *
     * @param authorization Authorization头内容
     * @return 提取的token
     */
    private String extractTokenFromAuthorization(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException("无效的Authorization头格式");
        }
        return authorization.substring(7);
    }

    /**
     * 获取客户端真实IP地址
     *
     * @param request HTTP请求对象
     * @return 客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
