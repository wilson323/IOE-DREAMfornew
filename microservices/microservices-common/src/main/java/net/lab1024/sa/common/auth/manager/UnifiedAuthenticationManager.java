package net.lab1024.sa.common.auth.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.security.entity.UserEntity;
import net.lab1024.sa.common.auth.domain.dto.*;
import net.lab1024.sa.common.auth.domain.vo.*;
import net.lab1024.sa.common.auth.util.JwtTokenUtil;
import net.lab1024.sa.common.auth.util.PasswordUtil;
import net.lab1024.sa.common.auth.util.TotpUtil;
import net.lab1024.sa.common.dto.ResponseDTO;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
    private final StringRedisTemplate redisTemplate;

    // Redis Key前缀
    private static final String TOKEN_BLACKLIST_PREFIX = "auth:token:blacklist:";
    private static final String USER_SESSION_PREFIX = "auth:user:session:";
    private static final String LOGIN_RETRY_PREFIX = "auth:login:retry:";
    private static final String SMS_CODE_PREFIX = "auth:sms:code:";
    private static final String EMAIL_CODE_PREFIX = "auth:email:code:";
    private static final String TOTP_SECRET_PREFIX = "auth:totp:secret:";

    // 配置参数
    private static final int MAX_LOGIN_RETRY = 5; // 最大登录失败次数
    private static final int LOGIN_RETRY_TIMEOUT = 900; // 登录失败锁定时间（秒，15分钟）
    private static final int SMS_CODE_EXPIRE = 300; // 短信验证码过期时间（秒，5分钟）
    private static final int EMAIL_CODE_EXPIRE = 600; // 邮件验证码过期时间（秒，10分钟）
    private static final int TOKEN_BLACKLIST_EXPIRE = 604800; // 令牌黑名单过期时间（秒，7天）

    /**
     * 获取登录失败锁定时间（秒）
     *
     * @return 锁定时间（秒）
     */
    public static int getLoginRetryTimeout() {
        return LOGIN_RETRY_TIMEOUT;
    }

    /**
     * 获取短信验证码过期时间（秒）
     *
     * @return 过期时间（秒）
     */
    public static int getSmsCodeExpire() {
        return SMS_CODE_EXPIRE;
    }

    /**
     * 获取邮件验证码过期时间（秒）
     *
     * @return 过期时间（秒）
     */
    public static int getEmailCodeExpire() {
        return EMAIL_CODE_EXPIRE;
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
     */
    public UnifiedAuthenticationManager(
            GatewayServiceClient gatewayServiceClient,
            JwtTokenUtil jwtTokenUtil,
            StringRedisTemplate redisTemplate) {
        this.gatewayServiceClient = gatewayServiceClient;
        this.jwtTokenUtil = jwtTokenUtil;
        this.redisTemplate = redisTemplate;
        log.info("[统一认证] 统一身份认证管理器初始化完成");
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
            UserEntity user = getUserByLoginName(request.getLoginName());

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
            updateLoginInfo(user.getId(), request.getClientIp());

            // 7. 生成认证令牌
            AuthToken token = generateAuthToken(user);

            // 8. 获取用户权限
            List<String> permissions = getUserPermissions(user.getId());

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
            UserEntity user = getUserByPhone(request.getPhone());

            if (user == null) {
                return AuthenticationResult.builder()
                        .success(false)
                        .errorCode("USER_NOT_FOUND")
                        .errorMessage("手机号未注册")
                        .build();
            }

            // 3. 验证短信验证码
            if (!verifySmsCode(user.getId(), request.getSmsCode())) {
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
            List<String> permissions = getUserPermissions(user.getId());

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
            UserEntity user = getUserById(primaryResult.getUserId());
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
            if (isTokenBlacklisted(token)) {
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
            UserEntity user = getUserById(userId);

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
            UserEntity user = getUserById(validation.getUserId());

            // 3. 生成新的访问令牌
            AuthToken newToken = generateAuthToken(user);

            // 4. 更新用户最后登录时间
            updateLastLoginTime(user.getId());

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
            addToTokenBlacklist(token);

            // 2. 更新用户最后登出时间
            updateLastLogoutTime(userId);

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

    private UserEntity getUserByLoginName(String loginName) {
        try {
            return gatewayServiceClient.callCommonService(
                    "/api/v1/user/username/" + loginName,
                    org.springframework.http.HttpMethod.GET,
                    null,
                    UserEntity.class
            ).getData();
        } catch (Exception e) {
            log.warn("[统一认证] 获取用户信息失败: loginName={}, error={}", loginName, e.getMessage());
            return null;
        }
    }

    private UserEntity getUserByPhone(String phone) {
        try {
            ResponseDTO<UserEntity> response = gatewayServiceClient.callCommonService(
                    "/api/v1/user/phone/" + phone,
                    org.springframework.http.HttpMethod.GET,
                    null,
                    UserEntity.class
            );
            return response != null && response.getOk() ? response.getData() : null;
        } catch (Exception e) {
            log.warn("[统一认证] 通过手机号获取用户失败: phone={}, error={}", phone, e.getMessage());
            return null;
        }
    }

    private UserEntity getUserById(Long userId) {
        try {
            ResponseDTO<UserEntity> response = gatewayServiceClient.callCommonService(
                    "/api/v1/user/" + userId,
                    org.springframework.http.HttpMethod.GET,
                    null,
                    UserEntity.class
            );
            return response != null && response.getOk() ? response.getData() : null;
        } catch (Exception e) {
            log.warn("[统一认证] 获取用户信息失败: userId={}, error={}", userId, e.getMessage());
            return null;
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
            // 使用PasswordUtil进行BCrypt密码验证
            boolean isValid = PasswordUtil.verifyPasswordWithSalt(inputPassword, storedPassword, salt);
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
            // 使用PasswordUtil进行BCrypt密码加密
            // BCrypt会自动生成盐值，salt参数保留用于兼容性
            return PasswordUtil.encryptPassword(password);
        } catch (Exception e) {
            log.error("[统一认证] 密码加密异常: {}", e.getMessage(), e);
            throw new RuntimeException("密码加密失败", e);
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
            UserEntity user = getUserById(userId);
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

            // 检查Redis中的登录失败次数
            String retryKey = LOGIN_RETRY_PREFIX + userId;
            String retryCountStr = redisTemplate.opsForValue().get(retryKey);

            if (retryCountStr != null) {
                int retryCount = Integer.parseInt(retryCountStr);
                if (retryCount >= MAX_LOGIN_RETRY) {
                    log.warn("[统一认证] 用户登录失败次数过多: userId={}, 失败次数: {}", userId, retryCount);
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            log.error("[统一认证] 检查登录失败次数异常: userId={}, error={}", userId, e.getMessage(), e);
            // 异常情况下不阻止登录，避免影响正常用户
            return false;
        }
    }

    private void updateLoginInfo(Long userId, String clientIp) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);
            params.put("clientIp", clientIp);
            params.put("loginTime", LocalDateTime.now());

            gatewayServiceClient.callCommonService(
                    "/api/v1/user/update-login-info",
                    org.springframework.http.HttpMethod.POST,
                    params,
                    Void.class
            );
        } catch (Exception e) {
            log.warn("[统一认证] 更新登录信息失败: userId={}, error={}", userId, e.getMessage());
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
            List<String> roles = getUserRoles(user.getId());
            List<String> permissions = getUserPermissions(user.getId());

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
            throw new RuntimeException("生成认证令牌失败", e);
        }
    }

    /**
     * 获取用户角色列表
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    @SuppressWarnings("unchecked")
    private List<String> getUserRoles(Long userId) {
        try {
            ResponseDTO<?> response = gatewayServiceClient.callCommonService(
                    "/api/v1/user/roles/" + userId,
                    org.springframework.http.HttpMethod.GET,
                    null,
                    List.class
            );
            if (response != null && response.getOk() && response.getData() != null) {
                Object data = response.getData();
                if (data instanceof List) {
                    return (List<String>) data;
                }
            }
            return List.of();
        } catch (Exception e) {
            log.warn("[统一认证] 获取用户角色失败: userId={}, error={}", userId, e.getMessage());
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> getUserPermissions(Long userId) {
        try {
            // 通过网关获取用户权限
            return gatewayServiceClient.callCommonService(
                    "/api/v1/user/permissions/" + userId,
                    org.springframework.http.HttpMethod.GET,
                    null,
                    List.class
            ).getData();
        } catch (Exception e) {
            log.warn("[统一认证] 获取用户权限失败: userId={}, error={}", userId, e.getMessage());
            return List.of();
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
     * 验证短信验证码
     * <p>
     * 从Redis中获取存储的验证码进行验证
     * 验证成功后清除验证码，防止重复使用
     * </p>
     *
     * @param userId 用户ID
     * @param smsCode 用户输入的短信验证码
     * @return true-验证通过，false-验证失败
     */
    private boolean verifySmsCode(Long userId, String smsCode) {
        if (userId == null || smsCode == null || smsCode.trim().isEmpty()) {
            log.warn("[统一认证] 短信验证码参数为空");
            return false;
        }

        try {
            String codeKey = SMS_CODE_PREFIX + userId;
            String storedCode = redisTemplate.opsForValue().get(codeKey);

            if (storedCode == null) {
                log.warn("[统一认证] 短信验证码不存在或已过期: userId={}", userId);
                return false;
            }

            // 验证验证码
            if (smsCode.trim().equals(storedCode)) {
                // 验证成功，清除验证码
                redisTemplate.delete(codeKey);
                log.info("[统一认证] 短信验证码验证成功: userId={}", userId);
                return true;
            } else {
                log.warn("[统一认证] 短信验证码错误: userId={}", userId);
                return false;
            }

        } catch (Exception e) {
            log.error("[统一认证] 验证短信验证码异常: userId={}, error={}", userId, e.getMessage(), e);
            return false;
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
            UserEntity user = getUserById(request.getUserId());
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

            // 从Redis验证邮件验证码
            String codeKey = EMAIL_CODE_PREFIX + user.getEmail();
            String storedCode = redisTemplate.opsForValue().get(codeKey);

            // 设置验证码过期时间（如果不存在）
            if (storedCode == null) {
                // 验证码不存在，可能已过期
                redisTemplate.expire(codeKey, EMAIL_CODE_EXPIRE, TimeUnit.SECONDS);
            }

            if (storedCode == null) {
                return AuthenticationResult.builder()
                        .success(false)
                        .errorCode("EMAIL_CODE_EXPIRED")
                        .errorMessage("邮件验证码已过期")
                        .build();
            }

            if (!emailCode.trim().equals(storedCode)) {
                return AuthenticationResult.builder()
                        .success(false)
                        .errorCode("EMAIL_CODE_ERROR")
                        .errorMessage("邮件验证码错误")
                        .build();
            }

            // 4. 验证成功，清除验证码
            redisTemplate.delete(codeKey);

            // 5. 生成认证令牌
            AuthToken token = generateAuthToken(user);

            // 6. 获取用户权限
            List<String> permissions = getUserPermissions(user.getId());

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
    private AuthenticationResult authenticateByBiometric(MfaRequest request) {
        log.info("[统一认证] 开始生物识别认证: userId={}", request.getUserId());

        try {
            // 1. 获取用户信息
            UserEntity user = getUserById(request.getUserId());
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
                @SuppressWarnings("unchecked")
                Map<String, Object> verifyResult = gatewayServiceClient.callCommonService(
                        "/api/v1/biometric/verify",
                        org.springframework.http.HttpMethod.POST,
                        verifyRequest,
                        Map.class
                ).getData();

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
            List<String> permissions = getUserPermissions(user.getId());

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
            UserEntity user = getUserById(request.getUserId());
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
            List<String> permissions = getUserPermissions(user.getId());

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
     * 将令牌加入黑名单
     * <p>
     * 使用Redis存储黑名单，支持令牌撤销
     * 黑名单保留7天，防止令牌重放攻击
     * </p>
     *
     * @param token 要加入黑名单的令牌
     */
    private void addToTokenBlacklist(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.warn("[统一认证] 令牌为空，无法加入黑名单");
            return;
        }

        try {
            String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;

            // 将令牌加入黑名单，设置7天过期时间
            redisTemplate.opsForValue().set(blacklistKey, "true", TOKEN_BLACKLIST_EXPIRE, TimeUnit.SECONDS);

            log.info("[统一认证] 令牌已加入黑名单: {}", token.substring(0, Math.min(20, token.length())) + "...");
        } catch (Exception e) {
            log.error("[统一认证] 将令牌加入黑名单失败: error={}", e.getMessage(), e);
            // 不抛出异常，避免影响登出流程
        }
    }

    /**
     * 检查令牌是否在黑名单中
     *
     * @param token 令牌
     * @return true-在黑名单中，false-不在黑名单中
     */
    private boolean isTokenBlacklisted(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        try {
            String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
            return Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey));
        } catch (Exception e) {
            log.error("[统一认证] 检查令牌黑名单失败: error={}", e.getMessage(), e);
            return false;
        }
    }

    private void updateLastLogoutTime(Long userId) {
        try {
            gatewayServiceClient.callCommonService(
                    "/api/v1/user/update-logout-time",
                    org.springframework.http.HttpMethod.POST,
                    Map.of("userId", userId, "logoutTime", LocalDateTime.now()),
                    Void.class
            );
        } catch (Exception e) {
            log.warn("[统一认证] 更新登出时间失败: userId={}, error={}", userId, e.getMessage());
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
            String sessionKey = USER_SESSION_PREFIX + userId;

            // 1. 获取所有会话令牌
            java.util.Set<String> tokens = redisTemplate.opsForSet().members(sessionKey);

            if (tokens != null && !tokens.isEmpty()) {
                // 2. 将所有令牌加入黑名单
                for (String token : tokens) {
                    addToTokenBlacklist(token);
                }

                // 3. 删除Redis会话集合
                redisTemplate.delete(sessionKey);

                log.info("[统一认证] 已清理用户会话: userId={}, 会话数: {}", userId, tokens.size());
            } else {
                log.debug("[统一认证] 用户无活跃会话: userId={}", userId);
            }

            // 4. 通过网关清理数据库会话记录
            try {
                gatewayServiceClient.callCommonService(
                        "/api/v1/user/session/clear/" + userId,
                        org.springframework.http.HttpMethod.DELETE,
                        null,
                        Void.class
                );
            } catch (Exception e) {
                log.warn("[统一认证] 清理数据库会话记录失败: userId={}, error={}", userId, e.getMessage());
            }

        } catch (Exception e) {
            log.error("[统一认证] 清理用户会话异常: userId={}, error={}", userId, e.getMessage(), e);
            // 不抛出异常，避免影响登出流程
        }
    }

    private void updateLastLoginTime(Long userId) {
        try {
            gatewayServiceClient.callCommonService(
                    "/api/v1/user/update-last-login-time",
                    org.springframework.http.HttpMethod.POST,
                    Map.of("userId", userId, "lastLoginTime", LocalDateTime.now()),
                    Void.class
            );
        } catch (Exception e) {
            log.warn("[统一认证] 更新最后登录时间失败: userId={}, error={}", userId, e.getMessage());
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
            // 通过网关调用用户偏好服务获取MFA配置
            // 偏好键：security.mfaType
            @SuppressWarnings("unchecked")
            Map<String, Object> preference = (Map<String, Object>) gatewayServiceClient.callCommonService(
                    "/api/v1/user/preference/" + userId + "/security/mfaType",
                    org.springframework.http.HttpMethod.GET,
                    null,
                    Map.class
            ).getData();

            if (preference != null && preference.containsKey("preferenceValue")) {
                Object mfaTypeObj = preference.get("preferenceValue");
                if (mfaTypeObj != null) {
                    String mfaType = mfaTypeObj.toString();
                    if (!mfaType.trim().isEmpty()) {
                        log.debug("[统一认证] 获取用户MFA类型: userId={}, mfaType={}", userId, mfaType);
                        return mfaType.trim().toUpperCase();
                    }
                }
            }

            // 如果偏好设置中没有，尝试从系统配置获取默认MFA类型
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> systemConfig = (Map<String, Object>) gatewayServiceClient.callCommonService(
                        "/api/v1/config/system/mfa/defaultType",
                        org.springframework.http.HttpMethod.GET,
                        null,
                        Map.class
                ).getData();

                if (systemConfig != null && systemConfig.containsKey("configValue")) {
                    Object defaultMfaTypeObj = systemConfig.get("configValue");
                    if (defaultMfaTypeObj != null) {
                        String defaultMfaType = defaultMfaTypeObj.toString();
                        if (!defaultMfaType.trim().isEmpty() && !"NONE".equalsIgnoreCase(defaultMfaType)) {
                            log.debug("[统一认证] 使用系统默认MFA类型: userId={}, mfaType={}", userId, defaultMfaType);
                            return defaultMfaType.trim().toUpperCase();
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("[统一认证] 获取系统默认MFA类型失败: userId={}, error={}", userId, e.getMessage());
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
            String secretKey = TOTP_SECRET_PREFIX + userId;
            String totpSecret = redisTemplate.opsForValue().get(secretKey);

            if (totpSecret == null || totpSecret.trim().isEmpty()) {
                // 如果Redis中没有，尝试从数据库获取
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> preference = (Map<String, Object>) gatewayServiceClient.callCommonService(
                            "/api/v1/user/preference/" + userId + "/security/totpSecret",
                            org.springframework.http.HttpMethod.GET,
                            null,
                            Map.class
                    ).getData();

                    if (preference != null && preference.containsKey("preferenceValue")) {
                        totpSecret = (String) preference.get("preferenceValue");
                        if (totpSecret != null && !totpSecret.trim().isEmpty()) {
                            // 缓存到Redis
                            redisTemplate.opsForValue().set(secretKey, totpSecret, 30, TimeUnit.DAYS);
                            return totpSecret.trim();
                        }
                    }
                } catch (Exception e) {
                    log.warn("[统一认证] 从数据库获取TOTP密钥失败: userId={}, error={}", userId, e.getMessage());
                }

                return null;
            }

            return totpSecret.trim();
        } catch (Exception e) {
            log.error("[统一认证] 获取TOTP密钥异常: userId={}, error={}", userId, e.getMessage(), e);
            return null;
        }
    }
}
