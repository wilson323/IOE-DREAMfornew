package net.lab1024.sa.common.openapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.openapi.domain.request.*;
import net.lab1024.sa.common.openapi.domain.response.*;
import net.lab1024.sa.common.openapi.service.UserOpenApiService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 开放平台用户管理API控制器
 * 提供用户认证、用户信息查询、权限管理等开放接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@RestController
@RequestMapping("/open/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "开放平台用户管理API", description = "提供用户认证、用户信息查询、权限管理等功能")
@Validated
public class UserOpenApiController {

    private final UserOpenApiService userOpenApiService;

    /**
     * 用户登录认证
     */
    @PostMapping("/auth/login")
    @Operation(summary = "用户登录", description = "通过用户名密码进行身份认证")
    public ResponseDTO<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIpAddress(httpRequest);
        log.info("[开放API] 用户登录请求: username={}, clientIp={}", request.getUsername(), clientIp);

        LoginResponse response = userOpenApiService.authenticate(request, clientIp);
        return ResponseDTO.ok(response);
    }

    /**
     * 刷新访问令牌
     */
    @PostMapping("/auth/refresh")
    @Operation(summary = "刷新访问令牌", description = "使用刷新令牌获取新的访问令牌")
    public ResponseDTO<RefreshTokenResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        log.info("[开放API] 刷新令牌请求");
        RefreshTokenResponse response = userOpenApiService.refreshToken(request.getRefreshToken());
        return ResponseDTO.ok(response);
    }

    /**
     * 用户退出登录
     */
    @PostMapping("/auth/logout")
    @Operation(summary = "用户退出", description = "退出登录并使访问令牌失效")
    public ResponseDTO<Void> logout(
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {

        String token = extractTokenFromAuthorization(authorization);
        String clientIp = getClientIpAddress(httpRequest);

        log.info("[开放API] 用户退出登录, clientIp={}", clientIp);
        userOpenApiService.logout(token, clientIp);
        return ResponseDTO.ok();
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/profile")
    @Operation(summary = "获取用户信息", description = "获取当前认证用户的详细信息")
    public ResponseDTO<UserProfileResponse> getUserProfile(
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);
        UserProfileResponse userProfile = userOpenApiService.getUserProfile(token);
        return ResponseDTO.ok(userProfile);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/profile")
    @Operation(summary = "更新用户信息", description = "更新当前认证用户的基本信息")
    public ResponseDTO<UserProfileResponse> updateUserProfile(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody UpdateUserProfileRequest request,
            HttpServletRequest httpRequest) {

        String token = extractTokenFromAuthorization(authorization);
        String clientIp = getClientIpAddress(httpRequest);

        log.info("[开放API] 更新用户信息, clientIp={}", clientIp);
        UserProfileResponse response = userOpenApiService.updateUserProfile(token, request, clientIp);
        return ResponseDTO.ok(response);
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    @Operation(summary = "修改密码", description = "修改当前用户的登录密码")
    public ResponseDTO<Void> changePassword(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody ChangePasswordRequest request,
            HttpServletRequest httpRequest) {

        String token = extractTokenFromAuthorization(authorization);
        String clientIp = getClientIpAddress(httpRequest);

        log.info("[开放API] 用户修改密码, clientIp={}", clientIp);
        userOpenApiService.changePassword(token, request, clientIp);
        return ResponseDTO.ok();
    }

    /**
     * 根据ID获取用户信息
     */
    @GetMapping("/{userId}")
    @Operation(summary = "获取指定用户信息", description = "根据用户ID获取用户基本信息（需要相应权限）")
    public ResponseDTO<UserInfoResponse> getUserById(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);
        log.info("[开放API] 查询用户信息: userId={}", userId);

        UserInfoResponse userInfo = userOpenApiService.getUserById(userId, token);
        return ResponseDTO.ok(userInfo);
    }

    /**
     * 获取用户列表
     */
    @GetMapping
    @Operation(summary = "获取用户列表", description = "分页获取用户列表（需要管理员权限）")
    public ResponseDTO<PageResult<UserInfoResponse>> getUserList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "用户状态") @RequestParam(required = false) Integer status,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);
        log.info("[开放API] 查询用户列表: pageNum={}, pageSize={}, keyword={}", pageNum, pageSize, keyword);

        UserQueryRequest queryRequest = UserQueryRequest.builder()
                .pageNum(pageNum)
                .pageSize(pageSize)
                .keyword(keyword)
                .status(status)
                .build();

        PageResult<UserInfoResponse> result = userOpenApiService.getUserList(queryRequest, token);
        return ResponseDTO.ok(result);
    }

    /**
     * 获取用户权限
     */
    @GetMapping("/{userId}/permissions")
    @Operation(summary = "获取用户权限", description = "获取指定用户的权限列表")
    public ResponseDTO<UserPermissionResponse> getUserPermissions(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);
        log.info("[开放API] 查询用户权限: userId={}", userId);

        UserPermissionResponse permissions = userOpenApiService.getUserPermissions(userId, token);
        return ResponseDTO.ok(permissions);
    }

    /**
     * 获取用户角色
     */
    @GetMapping("/{userId}/roles")
    @Operation(summary = "获取用户角色", description = "获取指定用户的角色列表")
    public ResponseDTO<List<String>> getUserRoles(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);
        log.info("[开放API] 查询用户角色: userId={}", userId);

        List<String> roles = userOpenApiService.getUserRoles(userId, token);
        return ResponseDTO.ok(roles);
    }

    /**
     * 检查用户权限
     */
    @GetMapping("/check-permission")
    @Operation(summary = "检查用户权限", description = "检查当前用户是否具有指定权限")
    public ResponseDTO<Boolean> checkPermission(
            @Parameter(description = "权限编码") @RequestParam String permission,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);
        log.info("[开放API] 检查用户权限: permission={}", permission);

        boolean hasPermission = userOpenApiService.checkPermission(token, permission);
        return ResponseDTO.ok(hasPermission);
    }

    /**
     * 验证访问令牌
     */
    @PostMapping("/auth/validate")
    @Operation(summary = "验证访问令牌", description = "验证访问令牌是否有效")
    public ResponseDTO<TokenValidationResponse> validateToken(
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);
        log.info("[开放API] 验证访问令牌");

        TokenValidationResponse response = userOpenApiService.validateToken(token);
        return ResponseDTO.ok(response);
    }

    /**
     * 从Authorization头中提取访问令牌
     */
    private String extractTokenFromAuthorization(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        throw new IllegalArgumentException("Invalid Authorization header format");
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
