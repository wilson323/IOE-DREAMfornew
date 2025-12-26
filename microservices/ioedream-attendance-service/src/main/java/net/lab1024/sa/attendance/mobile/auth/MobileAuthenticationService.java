package net.lab1024.sa.attendance.mobile.auth;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;

import net.lab1024.sa.attendance.mobile.model.MobileLoginRequest;
import net.lab1024.sa.attendance.mobile.model.MobileLoginResult;
import net.lab1024.sa.attendance.mobile.model.MobileLogoutResult;
import net.lab1024.sa.attendance.mobile.model.MobileTokenRefreshRequest;
import net.lab1024.sa.attendance.mobile.model.MobileTokenRefreshResult;
import net.lab1024.sa.attendance.mobile.model.MobileUserSession;
import net.lab1024.sa.common.auth.dao.UserDao;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.gateway.domain.response.EmployeeResponse;
import net.lab1024.sa.common.organization.entity.UserEntity;

/**
 * 移动端认证服务
 * <p>
 * 负责移动端用户认证相关的所有功能，包括：
 * - 用户登录/登出
 * - 访问令牌生成和刷新
 * - 密码验证
 * - 会话管理
 * </p>
 * <p>
 * 从AttendanceMobileServiceImpl中抽取，遵循单一职责原则
 * </p>
 *
 * @author IOE-DREAM Refactoring Team
 * @since 2025-12-26
 */
@Slf4j
@Service
public class MobileAuthenticationService {

    // JWT密钥（生产环境应从配置中心读取）
    private static final String JWT_SECRET_KEY = "ioedream-attendance-jwt-secret-key-2025-hmac-sha256";
    private static final long ACCESS_TOKEN_EXPIRATION = 8 * 60 * 60 * 1000; // 8小时
    private static final long REFRESH_TOKEN_EXPIRATION = 30 * 24 * 60 * 60 * 1000L; // 30天

    @Resource
    private UserDao userDao;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    // 移动端会话缓存 - 内存优化
    private final Map<String, MobileUserSession> userSessionCache = new ConcurrentHashMap<>();

    /**
     * 用户登录
     * <p>
     * 验证用户名密码，生成访问令牌和刷新令牌
     * </p>
     *
     * @param request 登录请求（用户名、密码、设备信息）
     * @return 登录结果（令牌、用户信息、权限等）
     */
    public ResponseDTO<MobileLoginResult> login(MobileLoginRequest request) {
        try {
            // 验证用户名密码（使用User实体进行认证）
            UserEntity user = userDao.selectByUsername(request.getUsername());
            if (user == null || !verifyPassword(request.getPassword(), user.getPassword())) {
                return ResponseDTO.error("INVALID_CREDENTIALS", "用户名或密码错误");
            }

            if (user.getStatus() != 1) {
                return ResponseDTO.error("ACCOUNT_DISABLED", "账户已禁用");
            }

            // TODO: 根据userId查询员工信息（需要修复EmployeeResponse缺少userId字段的问题）
            EmployeeResponse employee = null;

            // 生成访问令牌
            String accessToken = generateAccessToken(user, employee);
            String refreshToken = generateRefreshToken(user, employee);

            // 创建用户会话
            MobileUserSession session = MobileUserSession.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .employeeId(employee != null ? employee.getEmployeeId() : null)
                    .username(user.getUsername())
                    .employeeName(employee != null ? employee.getEmployeeName() : user.getRealName())
                    .loginTime(LocalDateTime.now())
                    .expiresTime(LocalDateTime.now().plusHours(24))
                    .deviceInfo(request.getDeviceInfo())
                    .build();

            // 缓存会话
            userSessionCache.put(accessToken, session);

            // 构造登录结果
            MobileLoginResult loginResult = MobileLoginResult.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .employeeId(employee != null ? employee.getEmployeeId() : null)
                    .employeeName(employee != null ? employee.getEmployeeName() : user.getRealName())
                    .departmentName(null) // TODO: 通过departmentId查询DepartmentEntity获取部门名称
                    .position(employee != null ? employee.getPosition() : null)
                    .avatarUrl(user.getAvatar() != null ? user.getAvatar() : null)
                    .permissions(getEmployeePermissions(
                            employee != null && employee.getUserId() != null ? employee.getUserId() : user.getUserId()))
                    .settings(getDefaultSettings())
                    .build();

            // 记录登录日志
            recordLoginEvent(user, employee, request);

            return ResponseDTO.ok(loginResult);

        } catch (Exception e) {
            log.error("[移动端登录] 失败: username={}, error={}", request.getUsername(), e.getMessage(), e);
            return ResponseDTO.error("LOGIN_FAILED", "登录失败，请重试: " + e.getMessage());
        }
    }

    /**
     * 用户登出
     * <p>
     * 清除用户会话和设备信息
     * </p>
     *
     * @param token 访问令牌
     * @return 登出结果
     */
    public ResponseDTO<MobileLogoutResult> logout(String token) {
        try {
            // 验证并清除会话
            MobileUserSession session = userSessionCache.get(token);
            if (session != null) {
                userSessionCache.remove(token);
                recordLogoutEvent(session);
            }

            // 清除设备信息（在原服务中处理）
            if (session != null && session.getEmployeeId() != null) {
                // deviceInfoCache.remove("device:" + session.getEmployeeId());
            }

            MobileLogoutResult logoutResult = MobileLogoutResult.builder()
                    .success(true)
                    .message("登出成功")
                    .build();

            return ResponseDTO.ok(logoutResult);

        } catch (Exception e) {
            log.error("[移动端登出] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("LOGOUT_FAILED", "登出失败，请重试: " + e.getMessage());
        }
    }

    /**
     * 刷新访问令牌
     * <p>
     * 使用刷新令牌获取新的访问令牌
     * </p>
     *
     * @param request 刷新令牌请求
     * @return 新的访问令牌
     */
    public ResponseDTO<MobileTokenRefreshResult> refreshToken(MobileTokenRefreshRequest request) {
        try {
            // 验证刷新令牌
            MobileUserSession session = validateRefreshToken(request.getRefreshToken());
            if (session == null) {
                return ResponseDTO.error("INVALID_REFRESH_TOKEN", "刷新令牌无效");
            }

            // 根据employeeId查询员工信息
            EmployeeResponse employee = null;
            if (session.getEmployeeId() != null) {
                try {
                    ResponseDTO<EmployeeResponse> employeeResponse = gatewayServiceClient.callCommonService(
                        "/api/employee/" + session.getEmployeeId(),
                        HttpMethod.GET,
                        null,
                        new TypeReference<ResponseDTO<EmployeeResponse>>() {}
                    );
                    if (employeeResponse.getCode() == 200) {
                        employee = employeeResponse.getData();
                    }
                } catch (Exception e) {
                    log.warn("获取员工信息失败: {}", e.getMessage());
                }
            }

            // 查询用户信息（通过EmployeeResponse的userId字段）
            UserEntity user = null;
            if (employee != null && employee.getUserId() != null) {
                user = userDao.selectById(employee.getUserId());
            } else if (session.getUsername() != null) {
                // 降级方案：通过username查询
                user = userDao.selectByUsername(session.getUsername());
            }

            if (user == null) {
                return ResponseDTO.error("USER_NOT_FOUND", "用户不存在");
            }

            // 生成新的访问令牌
            String newAccessToken = generateAccessToken(user, employee);

            // 更新会话
            session.setAccessToken(newAccessToken);
            session.setExpiresTime(LocalDateTime.now().plusHours(24));
            userSessionCache.put(newAccessToken, session);

            // 构造结果
            MobileTokenRefreshResult refreshResult = MobileTokenRefreshResult.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(request.getRefreshToken()) // 刷新令牌保持不变
                    .employeeId(session.getEmployeeId())
                    .build();

            return ResponseDTO.ok(refreshResult);

        } catch (Exception e) {
            log.error("[刷新令牌] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("TOKEN_REFRESH_FAILED", "刷新令牌失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户会话
     *
     * @param token 访问令牌
     * @return 用户会话信息
     */
    public MobileUserSession getSession(String token) {
        return userSessionCache.get(token);
    }

    /**
     * 验证刷新令牌
     *
     * @param refreshToken 刷新令牌
     * @return 用户会话信息
     */
    private MobileUserSession validateRefreshToken(String refreshToken) {
        return userSessionCache.values().stream()
                .filter(session -> refreshToken.equals(session.getRefreshToken()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 验证密码
     * <p>
     * 使用Spring Security BCryptPasswordEncoder进行密码验证
     * </p>
     *
     * @param rawPassword     原始密码
     * @param encodedPassword 加密后的密码
     * @return 验证结果
     */
    private boolean verifyPassword(String rawPassword, String encodedPassword) {
        try {
            org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder passwordEncoder =
                    new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
            return passwordEncoder.matches(rawPassword, encodedPassword);
        } catch (Exception e) {
            log.error("[密码验证] 异常: error={}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 生成访问令牌
     * <p>
     * 使用JWT生成访问令牌，有效期8小时
     * </p>
     *
     * @param user     用户实体
     * @param employee 员工信息
     * @return JWT访问令牌
     */
    private String generateAccessToken(UserEntity user, EmployeeResponse employee) {
        try {
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", user.getUserId());
            claims.put("username", user.getUsername());
            claims.put("employeeId", employee != null ? employee.getEmployeeId() : null);
            claims.put("employeeName", employee != null ? employee.getEmployeeName() : user.getRealName());
            claims.put("tokenType", "ACCESS");

            Date expiration = new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION);

            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(String.valueOf(user.getUserId()))
                    .setIssuedAt(new Date())
                    .setExpiration(expiration)
                    .signWith(Keys.hmacShaKeyFor(JWT_SECRET_KEY.getBytes()), SignatureAlgorithm.HS256)
                    .compact();

        } catch (Exception e) {
            log.error("[生成访问令牌] 失败: userId={}, error={}", user.getUserId(), e.getMessage(), e);
            // 降级处理：返回UUID
            return UUID.randomUUID().toString().replace("-", "");
        }
    }

    /**
     * 生成刷新令牌
     * <p>
     * 使用JWT生成刷新令牌，有效期30天
     * </p>
     *
     * @param user     用户实体
     * @param employee 员工信息
     * @return JWT刷新令牌
     */
    private String generateRefreshToken(UserEntity user, EmployeeResponse employee) {
        try {
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", user.getUserId());
            claims.put("username", user.getUsername());
            claims.put("employeeId", employee != null ? employee.getEmployeeId() : null);
            claims.put("tokenType", "REFRESH");

            Date expiration = new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION);

            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(String.valueOf(user.getUserId()))
                    .setIssuedAt(new Date())
                    .setExpiration(expiration)
                    .signWith(Keys.hmacShaKeyFor(JWT_SECRET_KEY.getBytes()), SignatureAlgorithm.HS256)
                    .compact();

        } catch (Exception e) {
            log.error("[生成刷新令牌] 失败: userId={}, error={}", user.getUserId(), e.getMessage(), e);
            // 降级处理：返回UUID
            return UUID.randomUUID().toString().replace("-", "");
        }
    }

    /**
     * 获取员工权限
     * <p>
     * TODO: 实际应从权限系统查询
     * </p>
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    private List<String> getEmployeePermissions(Long userId) {
        return java.util.Arrays.asList("attendance:clockin", "attendance:clockout", "attendance:view");
    }

    /**
     * 获取默认设置
     *
     * @return 默认设置Map
     */
    private Map<String, Object> getDefaultSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("biometricEnabled", true);
        settings.put("locationVerificationEnabled", true);
        settings.put("offlineSyncEnabled", true);
        settings.put("notificationEnabled", true);
        return settings;
    }

    /**
     * 记录登录事件
     *
     * @param user     用户实体
     * @param employee 员工信息
     * @param request  登录请求
     */
    private void recordLoginEvent(UserEntity user, EmployeeResponse employee, MobileLoginRequest request) {
        log.info("[移动端登录] 用户ID={}, 用户名={}, 员工ID={}, 设备={}",
                user.getUserId(), user.getUsername(),
                employee != null ? employee.getEmployeeId() : null,
                request.getDeviceInfo());
    }

    /**
     * 记录登出事件
     *
     * @param session 用户会话
     */
    private void recordLogoutEvent(MobileUserSession session) {
        log.info("[移动端登出] 员工ID={}, 用户名={}",
                session.getEmployeeId(), session.getUsername());
    }
}
