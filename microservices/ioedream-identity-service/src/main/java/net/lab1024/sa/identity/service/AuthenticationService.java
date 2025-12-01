package net.lab1024.sa.identity.service;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.identity.domain.entity.UserEntity;
import net.lab1024.sa.identity.domain.vo.LoginRequest;
import net.lab1024.sa.identity.domain.vo.LoginResponse;
import net.lab1024.sa.identity.domain.vo.RefreshTokenRequest;
import net.lab1024.sa.identity.repository.UserRepository;

/**
 * 用户认证服务
 * 基于Spring Security + JWT实现身份认证
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Value("${auth.password.max-attempts}")
    private int maxPasswordAttempts;

    @Value("${auth.password.lock-duration}")
    private int lockDurationMinutes;

    private Key signingKey;

    @PostConstruct
    public void init() {
        // 初始化JWT签名密钥
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * 用户登录
     */
    @Transactional
    public ResponseDTO<LoginResponse> login(LoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();
        String loginIp = request.getLoginIp();

        log.info("用户登录尝试: username={}, ip={}", username, loginIp);

        try {
            // 查找用户
            LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserEntity::getUsername, username);
            UserEntity user = userRepository.selectOne(wrapper);

            if (user == null) {
                throw new RuntimeException("用户名或密码错误");
            }

            // 检查用户状态
            validateUserStatus(user);

            // 验证密码
            if (!validatePassword(user, password)) {
                handleLoginFailure(user);
                throw new BusinessException("用户名或密码错误");
            }

            // Spring Security认证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));

            // 登录成功，重置失败次数
            resetLoginFailure(user);

            // 生成JWT令牌
            String accessToken = generateAccessToken(user);
            String refreshToken = generateRefreshToken(user);

            // 更新最后登录信息
            updateLastLoginInfo(user, loginIp);

            // 缓存用户会话
            cacheUserSession(user, accessToken, refreshToken);

            // 获取用户权限
            List<String> authorities = getAuthorities(authentication);
            List<String> permissions = getUserPermissions(user.getUserId());
            List<String> roles = getUserRoles(user.getUserId());

            // 构建登录响应
            LoginResponse response = new LoginResponse();
            response.setAccessToken(accessToken);
            response.setRefreshToken(refreshToken);
            response.setTokenType("Bearer");
            response.setExpiresIn(jwtExpiration);
            response.setUser(buildUserInfo(user));
            response.setRoles(roles);
            response.setPermissions(permissions);
            response.setAuthorities(authorities);

            log.info("用户登录成功: userId={}, username={}", user.getUserId(), username);
            return ResponseDTO.ok(response);

        } catch (BusinessException e) {
            log.warn("用户登录失败: username={}, error={}", username, e.getMessage());
            return ResponseDTO.error(e.getMessage());
        } catch (Exception e) {
            log.error("用户登录异常: username={}", username, e);
            return ResponseDTO.error("登录失败，请稍后重试");
        }
    }

    /**
     * 刷新令牌
     */
    @Transactional
    public ResponseDTO<LoginResponse> refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        try {
            // 验证刷新令牌
            Claims claims = parseRefreshToken(refreshToken);
            Long userId = Long.parseLong(claims.getSubject());

            // 查找用户
            UserEntity user = userRepository.selectById(userId);

            if (user == null) {
                throw new BusinessException("用户不存在");
            }

            // 检查用户状态
            validateUserStatus(user);

            // 检查刷新令牌是否有效
            if (!isValidRefreshToken(userId, refreshToken)) {
                throw new BusinessException("刷新令牌无效");
            }

            // 生成新的访问令牌
            String newAccessToken = generateAccessToken(user);
            String newRefreshToken = generateRefreshToken(user);

            // 更新缓存的用户会话
            updateUserSession(user.getUserId(), newAccessToken, newRefreshToken);

            // 获取用户权限和角色
            Set<String> authoritiesSet = getUserAuthorities(user);
            List<String> authorities = new ArrayList<>(authoritiesSet);
            List<String> permissions = getUserPermissions(user.getUserId());
            List<String> roles = getUserRoles(user.getUserId());

            LoginResponse response = new LoginResponse();
            response.setAccessToken(newAccessToken);
            response.setRefreshToken(newRefreshToken);
            response.setTokenType("Bearer");
            response.setExpiresIn(jwtExpiration);
            response.setUser(buildUserInfo(user));
            response.setRoles(roles);
            response.setPermissions(permissions);
            response.setAuthorities(authorities);

            log.info("令牌刷新成功: userId={}", userId);
            return ResponseDTO.ok(response);

        } catch (BusinessException e) {
            log.warn("令牌刷新失败: error={}", e.getMessage());
            return ResponseDTO.error(e.getMessage());
        } catch (Exception e) {
            log.error("令牌刷新异常", e);
            return ResponseDTO.error("令牌刷新失败，请重新登录");
        }
    }

    /**
     * 用户登出
     */
    @Transactional
    public ResponseDTO<Void> logout(Long userId) {
        try {
            // 清除用户会话缓存
            clearUserSession(userId);

            log.info("用户登出成功: userId={}", userId);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("用户登出失败: userId={}", userId, e);
            return ResponseDTO.error("登出失败，请稍后重试");
        }
    }

    /**
     * 验证访问令牌
     */
    public Claims validateAccessToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.warn("访问令牌验证失败: token={}", token, e);
            throw new BusinessException("无效的访问令牌");
        }
    }

    /**
     * 根据令牌获取用户信息
     */
    public UserEntity getUserByToken(String token) {
        try {
            Claims claims = validateAccessToken(token);
            Long userId = Long.parseLong(claims.getSubject());

            UserEntity user = userRepository.selectById(userId);
            if (user == null) {
                throw new BusinessException("用户不存在");
            }
            return user;

        } catch (Exception e) {
            log.warn("根据令牌获取用户信息失败: token={}", token, e);
            throw new BusinessException("令牌无效");
        }
    }

    /**
     * 检查用户权限
     */
    public boolean hasPermission(Long userId, String permission) {
        try {
            List<String> permissions = getUserPermissions(userId);
            return permissions.contains(permission);
        } catch (Exception e) {
            log.error("检查用户权限失败: userId={}, permission={}", userId, permission, e);
            return false;
        }
    }

    /**
     * 检查用户角色
     */
    public boolean hasRole(Long userId, String roleCode) {
        try {
            List<String> roles = getUserRoles(userId);
            return roles.contains(roleCode);
        } catch (Exception e) {
            log.error("检查用户角色失败: userId={}, roleCode={}", userId, roleCode, e);
            return false;
        }
    }

    // 私有方法

    /**
     * 验证用户状态
     */
    private void validateUserStatus(UserEntity user) {
        if (user.isLocked()) {
            throw new BusinessException("账户已被锁定，请稍后重试");
        }

        if (!user.isEnabled()) {
            throw new BusinessException("账户已被禁用");
        }
    }

    /**
     * 验证密码
     */
    private boolean validatePassword(UserEntity user, String password) {
        return passwordEncoder.matches(password, user.getPassword());
    }

    /**
     * 处理登录失败
     */
    private void handleLoginFailure(UserEntity user) {
        user.incrementLoginFailedCount();

        if (user.getLoginFailedCount() >= maxPasswordAttempts) {
            user.lockAccount(lockDurationMinutes);
            log.warn("用户账户被锁定: userId={}, username={}, 失败次数={}",
                    user.getUserId(), user.getUsername(), user.getLoginFailedCount());
        }

        userRepository.updateById(user);
    }

    /**
     * 重置登录失败次数
     */
    private void resetLoginFailure(UserEntity user) {
        if (user.getLoginFailedCount() != null && user.getLoginFailedCount() > 0) {
            user.resetLoginFailedCount();
            userRepository.updateById(user);
        }
    }

    /**
     * 更新最后登录信息
     */
    private void updateLastLoginInfo(UserEntity user, String loginIp) {
        user.updateLastLogin(loginIp);
        userRepository.updateById(user);
    }

    /**
     * 生成访问令牌
     */
    private String generateAccessToken(UserEntity user) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiration = now.plus(jwtExpiration, ChronoUnit.SECONDS);

        return Jwts.builder()
                .setSubject(user.getUserId().toString())
                .setIssuedAt(java.sql.Timestamp.valueOf(now))
                .setExpiration(java.sql.Timestamp.valueOf(expiration))
                .claim("username", user.getUsername())
                .claim("userType", user.getUserType())
                .claim("authorities", getUserAuthorities(user))
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 生成刷新令牌
     */
    private String generateRefreshToken(UserEntity user) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiration = now.plus(refreshExpiration, ChronoUnit.SECONDS);

        return Jwts.builder()
                .setSubject(user.getUserId().toString())
                .setIssuedAt(java.sql.Timestamp.valueOf(now))
                .setExpiration(java.sql.Timestamp.valueOf(expiration))
                .claim("tokenType", "refresh")
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 解析刷新令牌
     */
    private Claims parseRefreshToken(String refreshToken) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(refreshToken)
                .getBody();

        if (!"refresh".equals(claims.get("tokenType"))) {
            throw new BusinessException("无效的刷新令牌");
        }

        return claims;
    }

    /**
     * 获取用户权限
     */
    private List<String> getUserPermissions(Long userId) {
        // 暂时返回空列表，后续集成权限系统
        return List.of();
    }

    /**
     * 获取用户角色
     */
    private List<String> getUserRoles(Long userId) {
        // 暂时返回默认角色，后续集成角色系统
        return List.of("USER");
    }

    /**
     * 获取Spring Security权限
     */
    private List<String> getAuthorities(Authentication authentication) {
        // 暂时返回空列表，后续集成Spring Security权限系统
        return List.of("ROLE_USER");
    }

    /**
     * 获取用户权限信息
     */
    private Set<String> getUserAuthorities(UserEntity user) {
        // 基于角色和权限构建权限集合
        Set<String> authorities = new HashSet<>();

        // 添加角色权限
        List<String> roles = getUserRoles(user.getUserId());
        roles.forEach(role -> authorities.add("ROLE_" + role));

        // 添加具体权限
        authorities.addAll(getUserPermissions(user.getUserId()));

        return authorities;
    }

    /**
     * 缓存用户会话
     */
    private void cacheUserSession(UserEntity user, String accessToken, String refreshToken) {
        String sessionKey = "user:session:" + user.getUserId();
        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put("user", user);
        sessionData.put("accessToken", accessToken);
        sessionData.put("refreshToken", refreshToken);
        sessionData.put("loginTime", LocalDateTime.now());

        redisTemplate.opsForValue().set(sessionKey, sessionData, jwtExpiration, TimeUnit.SECONDS);
    }

    /**
     * 更新用户会话
     */
    private void updateUserSession(Long userId, String newAccessToken, String newRefreshToken) {
        String sessionKey = "user:session:" + userId;
        @SuppressWarnings("unchecked")
        Map<String, Object> sessionData = (Map<String, Object>) redisTemplate.opsForValue().get(sessionKey);

        if (sessionData != null) {
            sessionData.put("accessToken", newAccessToken);
            sessionData.put("refreshToken", newRefreshToken);
            redisTemplate.opsForValue().set(sessionKey, sessionData, jwtExpiration, TimeUnit.SECONDS);
        }
    }

    /**
     * 验证刷新令牌是否有效
     */
    private boolean isValidRefreshToken(Long userId, String refreshToken) {
        String sessionKey = "user:session:" + userId;
        @SuppressWarnings("unchecked")
        Map<String, Object> sessionData = (Map<String, Object>) redisTemplate.opsForValue().get(sessionKey);

        return sessionData != null && refreshToken.equals(sessionData.get("refreshToken"));
    }

    /**
     * 清除用户会话
     */
    private void clearUserSession(Long userId) {
        String sessionKey = "user:session:" + userId;
        redisTemplate.delete(sessionKey);
    }

    /**
     * 构建用户信息
     */
    private LoginResponse.UserInfo buildUserInfo(UserEntity user) {
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setUserId(user.getUserId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRealName(user.getRealName());
        userInfo.setEmail(user.getEmail());
        userInfo.setPhone(user.getPhone());
        userInfo.setAvatarUrl(user.getAvatarUrl());
        userInfo.setStatus(user.getStatus());
        userInfo.setUserType(user.getUserType());
        userInfo.setDepartmentId(user.getDepartmentId());
        userInfo.setPosition(user.getPosition());
        userInfo.setLastLoginTime(user.getLastLoginTime());
        return userInfo;
    }
}
