package net.lab1024.sa.common.auth.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.auth.manager.client.UserDirectoryClient;
import net.lab1024.sa.common.auth.manager.store.AuthRedisKeys;
import net.lab1024.sa.common.auth.manager.store.AuthRedisStore;
import net.lab1024.sa.common.auth.manager.store.OneTimeCodeVerifyResult;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.security.entity.UserEntity;
import net.lab1024.sa.common.auth.domain.dto.*;
import net.lab1024.sa.common.auth.domain.vo.*;
import net.lab1024.sa.common.auth.util.JwtTokenUtil;
import net.lab1024.sa.common.auth.util.PasswordUtil;
import net.lab1024.sa.common.auth.util.TotpUtil;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.SystemException;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一身份认证Manager
 * <p>
 * 企业级统一身份认证管理，支持多种认证方式
 * 严格遵循CLAUDE.md全局架构规范：
 * - microservices-common中的Manager类为纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖，保持为纯Java类
 * - 支持用户名密码、短信、邮件、生物识别、TOTP等多种认证方式
 * </p>
 * <p>
 * 企业级特性：
 * - BCrypt密码加密和验证
 * - JWT令牌生成和解析
 * - Redis会话管理和黑名单
 * - 多因素认证（MFA）支持
 * - 登录失败次数限制
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 2.0.0
 * @since 2025-01-30（企业级完善版）
 */
@Slf4j
public class UnifiedAuthenticationManager {

    private final GatewayServiceClient gatewayServiceClient;
    private final JwtTokenUtil jwtTokenUtil;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final AuthRedisStore authRedisStore;
    private final UserDirectoryClient userDirectoryClient;

    /**
     * 获取登录失败锁定时间（秒）
     *
     * @return 锁定时间（秒）
     */
    public static int getLoginRetryTimeout() {
        return AuthRedisKeys.LOGIN_RETRY_TIMEOUT_SECONDS;
    }

    /**
     * 获取短信验证码过期时间（秒）
     *
     * @return 过期时间（秒）
     */
    public static int getSmsCodeExpire() {
        return AuthRedisKeys.SMS_CODE_EXPIRE_SECONDS;
    }

    /**
     * 获取邮件验证码过期时间（秒）
     *
     * @return 过期时间（秒）
     */
    public static int getEmailCodeExpire() {
        return AuthRedisKeys.EMAIL_CODE_EXPIRE_SECONDS;
    }

    /**
     * 构造函数注入依赖
     * <p>
     * 符合CLAUDE.md规范：Manager类通过构造函数接收依赖
     * </p>
     *
     * @param gatewayServiceClient 网关服务客户端
     * @param jwtTokenUtil JWT令牌工具类
     * @param redisTemplate Redis模板
     * @param passwordEncoder 密码编码器（Spring Security BCryptPasswordEncoder，可选，如果为null则使用PasswordUtil作为fallback）
     */
    public UnifiedAuthenticationManager(
            GatewayServiceClient gatewayServiceClient,
            JwtTokenUtil jwtTokenUtil,
            StringRedisTemplate redisTemplate,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this(gatewayServiceClient, jwtTokenUtil, passwordEncoder,
                new UserDirectoryClient(gatewayServiceClient),
                new AuthRedisStore(redisTemplate));
    }

    /**
     * 构造函数注入依赖（可注入组件，便于复用与测试）
     */
    public UnifiedAuthenticationManager(
            GatewayServiceClient gatewayServiceClient,
            JwtTokenUtil jwtTokenUtil,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder,
            UserDirectoryClient userDirectoryClient,
            AuthRedisStore authRedisStore) {
        this.gatewayServiceClient = gatewayServiceClient;
        this.jwtTokenUtil = jwtTokenUtil;
        // 优先使用Spring Security的BCryptPasswordEncoder，如果没有则使用PasswordUtil作为fallback
        this.passwordEncoder = passwordEncoder;
        this.userDirectoryClient = userDirectoryClient;
        this.authRedisStore = authRedisStore;
        log.info("[统一认证] 统一身份认证管理器初始化完成");
    }

    /**
     * 构造函数注入依赖（向后兼容，不包含passwordEncoder）
     * <p>
     * 如果未提供passwordEncoder，将使用PasswordUtil作为fallback
     * </p>
     *
     * @param gatewayServiceClient 网关服务客户端
     * @param jwtTokenUtil JWT令牌工具类
     * @param redisTemplate Redis模板
     */
    public UnifiedAuthenticationManager(
            GatewayServiceClient gatewayServiceClient,
            JwtTokenUtil jwtTokenUtil,
            StringRedisTemplate redisTemplate) {
        this(gatewayServiceClient, jwtTokenUtil, redisTemplate, null);
    }

    /**
     * 用户名密码认证
     *
     * @param request 登录请求
     * @return 认证结果
     */
    public AuthenticationResult authenticateByUsernamePassword(LoginRequest request) {
        log.info("[统一认证] 开始用户名密码认证: {}", request.getLoginName());

        try {
            // 1. 参数验证
            validateLoginRequest(request);

            // 2. 通过网关调用用户服务获取用户信息
            UserEntity user = userDirectoryClient.getUserByLoginName(request.getLoginName());

            if (user == null) {
                return AuthenticationResult.builder()
                        .success(false)
                        .errorCode("USER_NOT_FOUND")
                        .errorMessage("用户不存在")
                        .build();
            }

            // 3. 验证用户状态
            if (!isActiveUser(user)) {
                return AuthenticationResult.builder()
                        .success(false)
                        .errorCode("USER_INACTIVE")
                        .errorMessage("用户状态异常")
                        .build();
            }

            // 4. 验证密码
            if (!verifyPassword(request.getPassword(), user.getPassword(), null)) {
                return AuthenticationResult.builder()
                        .success(false)
                        .errorCode("PASSWORD_ERROR")
                        .errorMessage("密码错误")
                        .build();
            }

            // 5. 检查登录失败次数
            if (isLoginExceeded(user.getId())) {
                return AuthenticationResult.builder()
                        .success(false)
                        .errorCode("LOGIN_EXCEEDED")
                        .errorMessage("登录失败次数过多，请稍后再试")
                        .build();
            }

            // 6. 更新登录信息
            userDirectoryClient.updateLoginInfo(user.getId(), request.getClientIp());

            // 7. 生成认证令牌
            AuthToken token = generateAuthToken(user);

            // 8. 获取用户权限
            List<String> permissions = userDirectoryClient.getUserPermissions(user.getId());

            return AuthenticationResult.builder()
                    .success(true)
                    .userId(user.getId())
                    .loginName(user.getUsername())
                    .userName(user.getRealName())
                    .userPhone(user.getPhone())
                    .userEmail(user.getEmail())
                    .token(token)
                    .permissions(permissions)
                    .build();

        } catch (Exception e) {
            log.error("[统一认证] 用户名密码认证异常: {}", e.getMessage(), e);
            return AuthenticationResult.builder()
                    .success(false)
                    .errorCode("SYSTEM_ERROR")
                    .errorMessage("系统异常")
                    .build();
        }
    }

    /**
     * 短信验证码认证
     *
     * @param request 短信认证请求
     * @return 认证结果
     */
    public AuthenticationResult authenticateBySmsCode(SmsLoginRequest request) {
        log.info("[统一认证] 开始短信验证码认证: {}", request.getPhone());

        try {
            // 1. 参数验证
            validateSmsRequest(request);

            // 2. 通过手机号获取用户信息
            UserEntity user = userDirectoryClient.getUserByPhone(request.getPhone());

            if (user == null) {
                return AuthenticationResult.builder()
                        .success(false)
                        .errorCode("USER_NOT_FOUND")
                        .errorMessage("手机号未注册")
                        .build();
            }

            // 3. 验证短信验证码
            if (authRedisStore.verifyAndConsumeSmsCode(user.getId(), request.getSmsCode()) != OneTimeCodeVerifyResult.SUCCESS) {
                return AuthenticationResult.builder()
                        .success(false)
                        .errorCode("SMS_CODE_ERROR")
                        .errorMessage("短信验证码错误")
                        .build();
            }

            // 4. 验证用户状态
            if (!isActiveUser(user)) {
                return AuthenticationResult.builder()
                        .success(false)
                        .errorCode("USER_INACTIVE")
                        .errorMessage("用户状态异常")
                        .build();
            }

            // 5. 生成认证令牌
            AuthToken token = generateAuthToken(user);

            // 6. 获取用户权限
            List<String> permissions = userDirectoryClient.getUserPermissions(user.getId());

            return AuthenticationResult.builder()
                    .success(true)
                    .userId(user.getId())
                    .loginName(user.getUsername())
                    .userName(user.getRealName())
                    .userPhone(user.getPhone())
                    .userEmail(user.getEmail())
                    .token(token)
                    .permissions(permissions)
                    .build();

        } catch (Exception e) {
            log.error("[统一认证] 短信验证码认证异常: {}", e.getMessage(), e);
            return AuthenticationResult.builder()
                    .success(false)
                    .errorCode("SYSTEM_ERROR")
                    .errorMessage("系统异常")
                    .build();
        }
    }

    /**
     * 多因素认证
     *
     * @param primaryResult 主认证结果
     * @param mfaRequest 多因素认证请求
     * @return 认证结果
     */
    public AuthenticationResult multiFactorAuthentication(AuthenticationResult primaryResult, MfaRequest mfaRequest) {
        log.info("[统一认证] 开始多因素认证: userId={}", primaryResult.getUserId());

        try {
            // 1. 检查主认证结果
            if (primaryResult.getSuccess() == null || !primaryResult.getSuccess()) {
                return primaryResult;
            }

            // 2. 获取用户的多因素认证配置
            UserEntity user = userDirectoryClient.getUserById(primaryResult.getUserId());
            if (user == null) {
                return AuthenticationResult.builder()
                        .success(false)
                        .errorCode("USER_NOT_FOUND")
                        .errorMessage("用户不存在")
                        .build();
            }

            // 从用户偏好设置中获取MFA类型
            String mfaType = getUserMfaType(user.getId());

            if ("NONE".equals(mfaType)) {
                // 用户未配置多因素认证，直接通过
                return primaryResult;
            }

            // 3. 根据多因素认证类型进行验证
            switch (mfaType) {
                case "SMS":
                    // MFA请求需要转换为SMS登录请求
                    SmsLoginRequest smsRequest = new SmsLoginRequest();
                    smsRequest.setPhone(user.getPhone());
                    smsRequest.setSmsCode(mfaRequest.getVerifyCode() != null ? mfaRequest.getVerifyCode() : "");
                    smsRequest.setDeviceId(mfaRequest.getDeviceId() != null ? mfaRequest.getDeviceId() : "");
                    return authenticateBySmsCode(smsRequest);

                case "EMAIL":
                    return authenticateByEmail(mfaRequest);

                case "BIOMETRIC":
                    return authenticateByBiometric(mfaRequest);

                case "TOTP":
                    return authenticateByTotp(mfaRequest);

                default:
                    return AuthenticationResult.builder()
                            .success(false)
                            .errorCode("UNSUPPORTED_MFA_TYPE")
                            .errorMessage("不支持的多因素认证类型")
                            .build();
            }

        } catch (Exception e) {
            log.error("[统一认证] 多因素认证异常: {}", e.getMessage(), e);
            return AuthenticationResult.builder()
                    .success(false)
                    .errorCode("SYSTEM_ERROR")
                    .errorMessage("系统异常")
                    .build();
        }
    }

    /**
     * 验证令牌
     * <p>
     * 企业级令牌验证，包括：
     * - JWT签名验证
     * - 过期时间检查
     * - 黑名单检查
     * - 用户状态验证
     * </p>
     *
     * @param token 认证令牌
     * @return 验证结果
     */
    public TokenValidationResult validateToken(String token) {
        log.debug("[统一认证] 验证令牌: {}", token);

        try {
            // 1. 检查令牌是否在黑名单中
            if (authRedisStore.isTokenBlacklisted(token)) {
                return TokenValidationResult.builder()
                        .valid(false)
                        .errorCode("TOKEN_BLACKLISTED")
                        .errorMessage("令牌已被撤销")
                        .build();
            }

            // 2. 解析JWT令牌
            Map<String, Object> claims = parseJwtToken(token);

            if (claims == null) {
                return TokenValidationResult.builder()
                        .valid(false)
                        .errorCode("INVALID_TOKEN")
                        .errorMessage("无效的令牌")
                        .build();
            }

            // 3. 检查令牌过期时间
            Long expiration = (Long) claims.get("exp");
            if (expiration != null && expiration < System.currentTimeMillis() / 1000) {
                return TokenValidationResult.builder()
                        .valid(false)
                        .errorCode("TOKEN_EXPIRED")
                        .errorMessage("令牌已过期")
                        .build();
            }

            // 4. 获取用户信息并验证状态
            Long userId = Long.valueOf(claims.get("userId").toString());
            UserEntity user = userDirectoryClient.getUserById(userId);

            if (user == null || !isActiveUser(user)) {
                return TokenValidationResult.builder()
                        .valid(false)
                        .errorCode("USER_INVALID")
                        .errorMessage("用户不存在或状态异常")
                        .build();
            }

            return TokenValidationResult.builder()
                    .valid(true)
                    .userId(userId)
                    .loginName(user.getUsername())
                    .userName(user.getRealName())
                    .claims(claims)
                    .build();

        } catch (Exception e) {
            log.error("[统一认证] 令牌验证异常: {}", e.getMessage(), e);
            return TokenValidationResult.builder()
                    .valid(false)
                    .errorCode("SYSTEM_ERROR")
                    .errorMessage("令牌验证失败")
                    .build();
        }
    }

    /**
     * 刷新令牌
     *
     * @param refreshToken 刷新令牌
     * @return 刷新结果
     */
    public RefreshTokenResult refreshToken(String refreshToken) {
        log.info("[统一认证] 刷新令牌");

        try {
            // 1. 验证刷新令牌
            TokenValidationResult validation = validateToken(refreshToken);

            if (validation.getValid() == null || !validation.getValid()) {
                return RefreshTokenResult.builder()
                        .success(false)
                        .errorCode("INVALID_REFRESH_TOKEN")
                        .errorMessage("无效的刷新令牌")
                        .build();
            }

            // 2. 获取用户信息
            UserEntity user = userDirectoryClient.getUserById(validation.getUserId());

            // 3. 生成新的访问令牌
            AuthToken newToken = generateAuthToken(user);

            // 4. 更新用户最后登录时间
            userDirectoryClient.updateLastLoginTime(user.getId());

            return RefreshTokenResult.builder()
                    .success(true)
                    .accessToken(newToken.getAccessToken())
                    .tokenType(newToken.getTokenType())
                    .expiresIn(newToken.getExpiresIn())
                    .build();

        } catch (Exception e) {
            log.error("[统一认证] 刷新令牌异常: {}", e.getMessage(), e);
            return RefreshTokenResult.builder()
                    .success(false)
                    .errorCode("SYSTEM_ERROR")
                    .errorMessage("刷新令牌失败")
                    .build();
        }
    }

    /**
     * 用户登出
     *
     * @param userId 用户ID
     * @param token 令牌
     */
    public void logout(Long userId, String token) {
        log.info("[统一认证] 用户登出: userId={}", userId);

        try {
            // 1. 将令牌加入黑名单
            authRedisStore.blacklistToken(token);

            // 2. 更新用户最后登出时间
            userDirectoryClient.updateLastLogoutTime(userId);

            // 3. 清理用户会话
            clearUserSessions(userId);

        } catch (Exception e) {
            log.error("[统一认证] 用户登出异常: userId={}, error={}", userId, e.getMessage(), e);
        }
    }

    // ====== 私有辅助方法 ======

    private void validateLoginRequest(LoginRequest request) {
        if (request.getLoginName() == null || request.getLoginName().trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (request.getClientIp() == null || request.getClientIp().trim().isEmpty()) {
            throw new IllegalArgumentException("客户端IP不能为空");
        }
    }

    private void validateSmsRequest(SmsLoginRequest request) {
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("手机号不能为空");
        }
        if (request.getSmsCode() == null || request.getSmsCode().trim().isEmpty()) {
            throw new IllegalArgumentException("短信验证码不能为空");
        }
        if (request.getDeviceId() == null || request.getDeviceId().trim().isEmpty()) {
            throw new IllegalArgumentException("设备ID不能为空");
        }
    }

    private boolean isActiveUser(UserEntity user) {
        return user.getStatus() != null && user.getStatus() == 1 && (user.getDeletedFlag() == null || user.getDeletedFlag() == 0);
    }

    /**
     * 验证密码
     * <p>
     * 使用BCrypt算法验证密码
     * </p>
     *
     * @param inputPassword 用户输入的原始密码
     * @param storedPassword 数据库中存储的加密密码（BCrypt格式）
     * @param salt 盐值（可选，BCrypt已包含盐值）
     * @return true-密码匹配，false-密码不匹配
     */
    private boolean verifyPassword(String inputPassword, String storedPassword, String salt) {
        if (inputPassword == null || storedPassword == null) {
            log.warn("[统一认证] 密码验证失败：密码为空");
            return false;
        }

        try {
            // 优先使用Spring Security的BCryptPasswordEncoder，如果没有则使用PasswordUtil作为fallback
            boolean isValid;
            if (passwordEncoder != null) {
                // 使用Spring Security的BCryptPasswordEncoder
                isValid = passwordEncoder.matches(inputPassword, storedPassword);
            } else {
                // 使用PasswordUtil作为fallback（向后兼容）
                isValid = PasswordUtil.verifyPasswordWithSalt(inputPassword, storedPassword, salt);
            }
            if (isValid) {
                log.debug("[统一认证] 密码验证成功");
            } else {
                log.warn("[统一认证] 密码验证失败");
            }
            return isValid;
        } catch (Exception e) {
            log.error("[统一认证] 密码验证异常: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 加密密码
     * <p>
     * 使用BCrypt算法加密密码
     * 注意：此方法主要用于新用户注册或密码重置场景
     * </p>
     *
     * @param password 原始密码
     * @param salt 盐值（可选，BCrypt会自动生成，此参数保留用于兼容性）
     * @return 加密后的密码（BCrypt格式）
     */
    @SuppressWarnings("unused")
    private String encryptPassword(String password, String salt) {
        try {
            // 优先使用Spring Security的BCryptPasswordEncoder，如果没有则使用PasswordUtil作为fallback
            if (passwordEncoder != null) {
                // 使用Spring Security的BCryptPasswordEncoder
                return passwordEncoder.encode(password);
            } else {
                // 使用PasswordUtil作为fallback（向后兼容）
                // BCrypt会自动生成盐值，salt参数保留用于兼容性
                return PasswordUtil.encryptPassword(password);
            }
        } catch (Exception e) {
            log.error("[统一认证] 密码加密异常: {}", e.getMessage(), e);
            throw new SystemException("PASSWORD_ENCRYPT_ERROR", "密码加密失败", e);
        }
    }

    /**
     * 检查用户登录失败次数是否超过限制
     * <p>
     * 从Redis中查询用户登录失败次数
     * 达到阈值后锁定账户，防止暴力破解
     * </p>
     *
     * @param userId 用户ID
     * @return true-超过限制，false-未超过限制
     */
    private boolean isLoginExceeded(Long userId) {
        try {
            // 通过网关获取用户信息，检查账户锁定状态
            UserEntity user = userDirectoryClient.getUserById(userId);
            if (user == null) {
                return false;
            }

            // 检查账户是否被锁定
            if (user.getAccountLocked() != null && user.getAccountLocked() == 1) {
                // 检查锁定时间是否已过期
                if (user.getUnlockTime() != null && user.getUnlockTime().isAfter(LocalDateTime.now())) {
                    log.warn("[统一认证] 用户账户已锁定: userId={}, 解锁时间: {}", userId, user.getUnlockTime());
                    return true;
                } else if (user.getUnlockTime() != null && user.getUnlockTime().isBefore(LocalDateTime.now())) {
                    // 锁定时间已过期，自动解锁
                    log.info("[统一认证] 用户账户锁定时间已过期，自动解锁: userId={}", userId);
                    return false;
                }
            }

            int retryCount = authRedisStore.getLoginRetryCount(userId);
            if (retryCount >= AuthRedisKeys.MAX_LOGIN_RETRY) {
                log.warn("[统一认证] 用户登录失败次数过多: userId={}, 失败次数: {}", userId, retryCount);
                return true;
            }

            return false;
        } catch (Exception e) {
            log.error("[统一认证] 检查登录失败次数异常: userId={}, error={}", userId, e.getMessage(), e);
            // 异常情况下不阻止登录，避免影响正常用户
            return false;
        }
    }

    /**
     * 生成认证令牌
     * <p>
     * 使用JWT工具类生成访问令牌和刷新令牌
     * 包含用户ID、用户名、角色、权限等Claims信息
     * </p>
     *
     * @param user 用户实体
     * @return 认证令牌对象
     */
    private AuthToken generateAuthToken(UserEntity user) {
        try {
            // 获取用户角色和权限
            List<String> roles = userDirectoryClient.getUserRoles(user.getId());
            List<String> permissions = userDirectoryClient.getUserPermissions(user.getId());

            // 生成访问令牌（包含完整用户信息）
            String accessToken = jwtTokenUtil.generateAccessToken(
                    user.getId(),
                    user.getUsername(),
                    roles,
                    permissions
            );

            // 生成刷新令牌（仅包含用户ID和用户名）
            String refreshToken = jwtTokenUtil.generateRefreshToken(
                    user.getId(),
                    user.getUsername()
            );

            // 获取访问令牌过期时间（秒）
            Long expiresIn = jwtTokenUtil.getRemainingExpiration(accessToken);
            if (expiresIn == null || expiresIn <= 0) {
                expiresIn = 3600L; // 默认1小时
            }

            log.info("[统一认证] 生成认证令牌成功: userId={}, username={}", user.getId(), user.getUsername());

            return AuthToken.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(expiresIn.intValue())
                    .build();

        } catch (Exception e) {
            log.error("[统一认证] 生成认证令牌异常: userId={}, error={}", user.getId(), e.getMessage(), e);
            throw new SystemException("GENERATE_AUTH_TOKEN_ERROR", "生成认证令牌失败", e);
        }
    }

    /**
     * 解析JWT令牌
     * <p>
     * 使用JWT工具类解析令牌，验证签名和过期时间
     * 返回Claims Map供后续使用
     * </p>
     *
     * @param token JWT令牌字符串
     * @return Claims Map，解析失败返回null
     */
    @SuppressWarnings("PMD.ReturnEmptyCollectionRatherThanNull")
    private Map<String, Object> parseJwtToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.warn("[统一认证] JWT令牌为空");
            return null;
        }

        try {
            // 验证令牌有效性
            if (!jwtTokenUtil.validateToken(token)) {
                log.warn("[统一认证] JWT令牌验证失败");
                return null;
            }

            // 解析令牌为Map
            Map<String, Object> claims = jwtTokenUtil.parseJwtTokenToMap(token);
            if (claims == null) {
                log.warn("[统一认证] JWT令牌解析失败");
                return null;
            }

            log.debug("[统一认证] JWT令牌解析成功: userId={}", claims.get("userId"));
            return claims;

        } catch (Exception e) {
            log.error("[统一认证] 解析JWT令牌异常: error={}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 邮件认证
     * <p>
     * 通过网关调用邮件服务验证邮件验证码
     * 支持邮件验证码认证流程
     * </p>
     *
     * @param request MFA认证请求
     * @return 认证结果
     */
    private AuthenticationResult authenticateByEmail(MfaRequest request) {
        log.info("[统一认证] 开始邮件认证: userId={}", request.getUserId());

        try {
            // 1. 获取用户信息
            UserEntity user = userDirectoryClient.getUserById(request.getUserId());
            if (user == null) {
                return AuthenticationResult.builder()
                        .success(false)
                        .errorCode("USER_NOT_FOUND")
                        .errorMessage("用户不存在")
                        .build();
            }

            // 2. 验证用户邮箱
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                return AuthenticationResult.builder()
                        .success(false)
                        .errorCode("EMAIL_NOT_BOUND")
                        .errorMessage("用户未绑定邮箱")
                        .build();
            }

            // 3. 验证邮件验证码
            String emailCode = request.getEmailCode();
            if (emailCode == null || emailCode.trim().isEmpty()) {
                return AuthenticationResult.builder()
                        .success(false)
                        .errorCode("EMAIL_CODE_EMPTY")
                        .errorMessage("邮件验证码不能为空")
                        .build();
            }

            OneTimeCodeVerifyResult verifyResult = authRedisStore.verifyAndConsumeEmailCode(user.getEmail(), emailCode);
            if (verifyResult == OneTimeCodeVerifyResult.CODE_NOT_FOUND) {
                return AuthenticationResult.builder()
                        .success(false)
                        .errorCode("EMAIL_CODE_EXPIRED")
                        .errorMessage("邮件验证码已过期")
                        .build();
            }

            if (verifyResult != OneTimeCodeVerifyResult.SUCCESS) {
                return AuthenticationResult.builder()
                        .success(false)
                        .errorCode("EMAIL_CODE_ERROR")
                        .errorMessage("邮件验证码错误")
                        .build();
            }

            // 5. 生成认证令牌
            AuthToken token = generateAuthToken(user);

            // 6. 获取用户权限
            List<String> permissions = userDirectoryClient.getUserPermissions(user.getId());

            log.info("[统一认证] 邮件认证成功: userId={}, email={}", user.getId(), user.getEmail());

            return AuthenticationResult.builder()
                    .success(true)
                    .userId(user.getId())
                    .loginName(user.getUsername())
                    .userName(user.getRealName())
                    .userPhone(user.getPhone())
                    .userEmail(user.getEmail())
                    .token(token)
                    .permissions(permissions)
                    .build();

        } catch (Exception e) {
            log.error("[统一认证] 邮件认证异常: userId={}, error={}", request.getUserId(), e.getMessage(), e);
            return AuthenticationResult.builder()
                    .success(false)
                    .errorCode("SYSTEM_ERROR")
                    .errorMessage("邮件认证失败")
                    .build();
        }
    }

    /**
     * 生物识别认证
     * <p>
     * 通过网关调用生物识别服务进行认证
     * 支持人脸、指纹等生物特征验证
     * </p>
     *
     * @param request MFA认证请求
     * @return 认证结果
     */
    @SuppressWarnings("null")
    private AuthenticationResult authenticateByBiometric(MfaRequest request) {
        log.info("[统一认证] 开始生物识别认证: userId={}", request.getUserId());

        try {
            // 1. 获取用户信息
            UserEntity user = userDirectoryClient.getUserById(request.getUserId());
            if (user == null) {
                return AuthenticationResult.builder()
                        .success(false)
                        .errorCode("USER_NOT_FOUND")
                        .errorMessage("用户不存在")
                        .build();
            }

            // 2. 验证生物特征数据
            String biometricData = request.getBiometricData();
            if (biometricData == null || biometricData.trim().isEmpty()) {
                return AuthenticationResult.builder()
                        .success(false)
                        .errorCode("BIOMETRIC_DATA_EMPTY")
                        .errorMessage("生物特征数据不能为空")
                        .build();
            }

            // 3. 通过网关调用生物识别服务验证
            Map<String, Object> verifyRequest = new HashMap<>();
            verifyRequest.put("userId", user.getId());
            verifyRequest.put("biometricData", biometricData);
            verifyRequest.put("biometricType", "FACE"); // 默认人脸识别，可根据实际情况扩展

            try {
                @SuppressWarnings("rawtypes")
                ResponseDTO<Map> response = gatewayServiceClient.callCommonService(
                        "/api/v1/biometric/verify",
                        org.springframework.http.HttpMethod.POST,
                        verifyRequest,
                        Map.class
                );

                @SuppressWarnings("unchecked")
                Map<String, Object> verifyResult = response != null && response.getOk() ? (Map<String, Object>) response.getData() : null;

                // 检查验证结果（实际运行时getData()可能返回null）
                if (verifyResult == null) {
                    return AuthenticationResult.builder()
                            .success(false)
                            .errorCode("BIOMETRIC_VERIFY_FAILED")
                            .errorMessage("生物识别验证失败")
                            .build();
                }

                Boolean verifySuccess = (Boolean) verifyResult.get("success");
                if (verifySuccess == null || !verifySuccess) {
                    String errorMessage = (String) verifyResult.get("message");
                    return AuthenticationResult.builder()
                            .success(false)
                            .errorCode("BIOMETRIC_VERIFY_FAILED")
                            .errorMessage(errorMessage != null ? errorMessage : "生物识别验证失败")
                            .build();
                }

            } catch (Exception e) {
                log.warn("[统一认证] 生物识别服务调用失败: userId={}, error={}", user.getId(), e.getMessage());
                return AuthenticationResult.builder()
                        .success(false)
                        .errorCode("BIOMETRIC_SERVICE_ERROR")
                        .errorMessage("生物识别服务异常")
                        .build();
            }

            // 4. 验证成功，生成认证令牌
            AuthToken token = generateAuthToken(user);

            // 5. 获取用户权限
            List<String> permissions = userDirectoryClient.getUserPermissions(user.getId());

            log.info("[统一认证] 生物识别认证成功: userId={}", user.getId());

            return AuthenticationResult.builder()
                    .success(true)
                    .userId(user.getId())
                    .loginName(user.getUsername())
                    .userName(user.getRealName())
                    .userPhone(user.getPhone())
                    .userEmail(user.getEmail())
                    .token(token)
                    .permissions(permissions)
                    .build();

        } catch (Exception e) {
            log.error("[统一认证] 生物识别认证异常: userId={}, error={}", request.getUserId(), e.getMessage(), e);
            return AuthenticationResult.builder()
                    .success(false)
                    .errorCode("SYSTEM_ERROR")
                    .errorMessage("生物识别认证失败")
                    .build();
        }
    }

    /**
     * TOTP认证
     * <p>
     * 使用TotpUtil验证TOTP代码
     * 支持Google Authenticator等标准TOTP应用
     * </p>
     *
     * @param request MFA认证请求
     * @return 认证结果
     */
    private AuthenticationResult authenticateByTotp(MfaRequest request) {
        log.info("[统一认证] 开始TOTP认证: userId={}", request.getUserId());

        try {
            // 1. 获取用户信息
            UserEntity user = userDirectoryClient.getUserById(request.getUserId());
            if (user == null) {
                return AuthenticationResult.builder()
                        .success(false)
                        .errorCode("USER_NOT_FOUND")
                        .errorMessage("用户不存在")
                        .build();
            }

            // 2. 验证TOTP代码
            String totpCode = request.getTotpCode();
            if (totpCode == null || totpCode.trim().isEmpty()) {
                return AuthenticationResult.builder()
                        .success(false)
                        .errorCode("TOTP_CODE_EMPTY")
                        .errorMessage("TOTP代码不能为空")
                        .build();
            }

            // 3. 从Redis获取用户的TOTP密钥
            String secretKey = getTotpSecretKey(user.getId());
            if (secretKey == null || secretKey.trim().isEmpty()) {
                return AuthenticationResult.builder()
                        .success(false)
                        .errorCode("TOTP_NOT_BOUND")
                        .errorMessage("用户未绑定TOTP")
                        .build();
            }

            // 4. 使用TotpUtil验证TOTP代码
            boolean isValid = TotpUtil.verifyTotp(secretKey, totpCode.trim());
            if (!isValid) {
                return AuthenticationResult.builder()
                        .success(false)
                        .errorCode("TOTP_CODE_ERROR")
                        .errorMessage("TOTP代码错误")
                        .build();
            }

            // 5. 验证成功，生成认证令牌
            AuthToken token = generateAuthToken(user);

            // 6. 获取用户权限
            List<String> permissions = userDirectoryClient.getUserPermissions(user.getId());

            log.info("[统一认证] TOTP认证成功: userId={}", user.getId());

            return AuthenticationResult.builder()
                    .success(true)
                    .userId(user.getId())
                    .loginName(user.getUsername())
                    .userName(user.getRealName())
                    .userPhone(user.getPhone())
                    .userEmail(user.getEmail())
                    .token(token)
                    .permissions(permissions)
                    .build();

        } catch (Exception e) {
            log.error("[统一认证] TOTP认证异常: userId={}, error={}", request.getUserId(), e.getMessage(), e);
            return AuthenticationResult.builder()
                    .success(false)
                    .errorCode("SYSTEM_ERROR")
                    .errorMessage("TOTP认证失败")
                    .build();
        }
    }

    /**
     * 清理用户所有会话
     * <p>
     * 清理Redis中的用户会话和数据库会话记录
     * 用于用户登出或强制下线场景
     * </p>
     *
     * @param userId 用户ID
     */
    private void clearUserSessions(Long userId) {
        if (userId == null) {
            log.warn("[统一认证] 用户ID为空，无法清理会话");
            return;
        }

        try {
            // 1. 获取所有会话令牌
            java.util.Set<String> tokens = authRedisStore.getUserSessionTokens(userId);

            if (!tokens.isEmpty()) {
                // 2. 将所有令牌加入黑名单
                for (String token : tokens) {
                    authRedisStore.blacklistToken(token);
                }

                // 3. 删除Redis会话集合
                authRedisStore.clearUserSessionTokens(userId);

                log.info("[统一认证] 已清理用户会话: userId={}, 会话数: {}", userId, tokens.size());
            } else {
                log.debug("[统一认证] 用户无活跃会话: userId={}", userId);
            }

            // 4. 通过网关清理数据库会话记录
            userDirectoryClient.clearUserSessionsInDatabase(userId);

        } catch (Exception e) {
            log.error("[统一认证] 清理用户会话异常: userId={}, error={}", userId, e.getMessage(), e);
            // 不抛出异常，避免影响登出流程
        }
    }

    /**
     * 获取用户MFA类型
     * <p>
     * 从用户偏好设置中获取多因素认证类型
     * 支持SMS、EMAIL、BIOMETRIC、TOTP等类型
     * </p>
     *
     * @param userId 用户ID
     * @return MFA类型（SMS/EMAIL/BIOMETRIC/TOTP/NONE）
     */
    private String getUserMfaType(Long userId) {
        try {
            String mfaType = userDirectoryClient.getUserPreferenceValue(userId, "security", "mfaType");
            if (mfaType != null && !mfaType.trim().isEmpty()) {
                log.debug("[统一认证] 获取用户MFA类型: userId={}, mfaType={}", userId, mfaType);
                return mfaType.trim().toUpperCase();
            }

            String defaultMfaType = userDirectoryClient.getSystemConfigValue("mfa/defaultType");
            if (defaultMfaType != null && !defaultMfaType.trim().isEmpty() && !"NONE".equalsIgnoreCase(defaultMfaType)) {
                log.debug("[统一认证] 使用系统默认MFA类型: userId={}, mfaType={}", userId, defaultMfaType);
                return defaultMfaType.trim().toUpperCase();
            }

            // 默认返回NONE
            log.debug("[统一认证] 用户未配置MFA: userId={}", userId);
            return "NONE";

        } catch (Exception e) {
            log.warn("[统一认证] 获取用户MFA类型失败: userId={}, error={}", userId, e.getMessage());
            return "NONE";
        }
    }

    /**
     * 获取用户TOTP密钥
     * <p>
     * 从Redis中获取用户的TOTP密钥
     * 密钥以Base32格式存储
     * </p>
     *
     * @param userId 用户ID
     * @return TOTP密钥（Base32格式），未绑定返回null
     */
    private String getTotpSecretKey(Long userId) {
        try {
            String cachedSecret = authRedisStore.getTotpSecret(userId);
            if (cachedSecret != null && !cachedSecret.trim().isEmpty()) {
                return cachedSecret.trim();
            }

            String preferenceSecret = userDirectoryClient.getUserPreferenceValue(userId, "security", "totpSecret");
            if (preferenceSecret == null || preferenceSecret.trim().isEmpty()) {
                return null;
            }

            authRedisStore.cacheTotpSecret(userId, preferenceSecret.trim());
            return preferenceSecret.trim();
        } catch (Exception e) {
            log.error("[统一认证] 获取TOTP密钥异常: userId={}, error={}", userId, e.getMessage(), e);
            return null;
        }
    }
}
