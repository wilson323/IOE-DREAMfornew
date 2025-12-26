package net.lab1024.sa.common.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 并发登录控制服务
 * <p>
 * 控制用户同一时间内的最大登录会话数
 * 支持按设备类型分组限制
 * 超出限制时踢出最早登录的会话
 * 使用Redis存储会话信息，支持分布式环境
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Service
@Slf4j
public class ConcurrentLoginControlService {

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    private static final String USER_SESSION_PREFIX = "concurrent:session:";
    private static final String USER_SESSION_INDEX_PREFIX = "concurrent:index:";

    // 默认最大并发登录数
    private static final int DEFAULT_MAX_CONCURRENT = 3;

    // 会话TTL（24小时）
    private static final long SESSION_TTL_HOURS = 24;

    /**
     * 检查是否允许新登录
     *
     * @param userId 用户ID
     * @return 是否允许
     */
    public boolean allowLogin(Long userId) {
        return allowLogin(userId, null);
    }

    /**
     * 检查是否允许新登录
     *
     * @param userId 用户ID
     * @param deviceType 设备类型（可选，用于分组限制）
     * @return 是否允许
     */
    public boolean allowLogin(Long userId, String deviceType) {
        log.debug("[并发登录] 检查登录许可: userId={}, deviceType={}", userId, deviceType);

        int currentCount = getActiveSessionCount(userId, deviceType);
        int maxAllowed = getMaxConcurrentSessions(deviceType);

        if (currentCount >= maxAllowed) {
            log.warn("[并发登录] 超过最大并发数: userId={}, current={}, max={}", userId, currentCount, maxAllowed);
            return false;
        }

        return true;
    }

    /**
     * 创建新会话
     *
     * @param userId 用户ID
     * @param token 访问令牌
     * @param deviceType 设备类型
     * @param deviceInfo 设备信息
     * @return 是否成功（false表示需要踢出旧会话）
     */
    public boolean createSession(Long userId, String token, String deviceType, String deviceInfo) {
        log.info("[并发登录] 创建会话: userId={}, deviceType={}, device={}", userId, deviceType, deviceInfo);

        // 检查是否需要踢出旧会话
        if (!allowLogin(userId, deviceType)) {
            log.warn("[并发登录] 需要踢出旧会话: userId={}", userId);
            return false;
        }

        // 保存会话信息
        String sessionKey = buildSessionKey(userId, token);
        String indexKey = buildIndexKey(userId, deviceType);

        // 会话数据：userId:token:deviceType:deviceInfo:loginTime:lastAccessTime
        long now = System.currentTimeMillis();
        String sessionData = String.format("%d:%s:%s:%s:%d:%d",
                userId, token, deviceType, deviceInfo, now, now);

        // 保存会话
        redisTemplate.opsForValue().set(sessionKey, sessionData, SESSION_TTL_HOURS, TimeUnit.HOURS);

        // 添加到索引
        redisTemplate.opsForSet().add(indexKey, token);
        redisTemplate.expire(indexKey, SESSION_TTL_HOURS, TimeUnit.HOURS);

        log.info("[并发登录] 会话创建成功: userId={}, token={}", userId, maskToken(token));
        return true;
    }

    /**
     * 创建会话（如超出限制则踢出最早会话）
     *
     * @param userId 用户ID
     * @param token 访问令牌
     * @param deviceType 设备类型
     * @param deviceInfo 设备信息
     * @return 被踢出的令牌（如果没有则返回null）
     */
    public String createSessionWithEviction(Long userId, String token, String deviceType, String deviceInfo) {
        log.info("[并发登录] 创建会话（带踢出）: userId={}, deviceType={}", userId, deviceType);

        // 尝试直接创建
        if (createSession(userId, token, deviceType, deviceInfo)) {
            return null;
        }

        // 超出限制，踢出最早会话
        String evictedToken = evictEarliestSession(userId, deviceType);
        log.info("[并发登录] 踢出最早会话: userId={}, evictedToken={}", userId, maskToken(evictedToken));

        // 再次创建新会话
        createSession(userId, token, deviceType, deviceInfo);

        return evictedToken;
    }

    /**
     * 移除会话
     *
     * @param userId 用户ID
     * @param token 访问令牌
     */
    public void removeSession(Long userId, String token) {
        log.debug("[并发登录] 移除会话: userId={}, token={}", userId, maskToken(token));

        // 从所有可能索引中移除
        for (String deviceType : Arrays.asList("pc", "mobile", "tablet", "unknown")) {
            String indexKey = buildIndexKey(userId, deviceType);
            redisTemplate.opsForSet().remove(indexKey, token);
        }

        // 删除会话
        String sessionKey = buildSessionKey(userId, token);
        redisTemplate.delete(sessionKey);

        log.debug("[并发登录] 会话已移除: userId={}", userId);
    }

    /**
     * 更新会话最后访问时间
     *
     * @param userId 用户ID
     * @param token 访问令牌
     */
    public void updateLastAccessTime(Long userId, String token) {
        String sessionKey = buildSessionKey(userId, token);
        String sessionData = redisTemplate.opsForValue().get(sessionKey);

        if (sessionData != null) {
            // 解析会话数据
            String[] parts = sessionData.split(":");
            if (parts.length >= 6) {
                // 更新最后访问时间
                long now = System.currentTimeMillis();
                parts[5] = String.valueOf(now);
                String updatedData = String.join(":", parts);

                // 保存回Redis（重置TTL）
                redisTemplate.opsForValue().set(sessionKey, updatedData, SESSION_TTL_HOURS, TimeUnit.HOURS);

                log.trace("[并发登录] 更新访问时间: userId={}, token={}", userId, maskToken(token));
            }
        }
    }

    /**
     * 获取用户活跃会话数
     *
     * @param userId 用户ID
     * @param deviceType 设备类型（null表示全部）
     * @return 会话数
     */
    public int getActiveSessionCount(Long userId, String deviceType) {
        if (deviceType != null) {
            // 按设备类型统计
            String indexKey = buildIndexKey(userId, deviceType);
            Long size = redisTemplate.opsForSet().size(indexKey);
            return size != null ? size.intValue() : 0;
        } else {
            // 统计所有设备类型
            int total = 0;
            for (String type : Arrays.asList("pc", "mobile", "tablet", "unknown")) {
                total += getActiveSessionCount(userId, type);
            }
            return total;
        }
    }

    /**
     * 获取用户所有活跃会话
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    public List<UserSessionInfo> getActiveSessions(Long userId) {
        List<UserSessionInfo> sessions = new ArrayList<>();

        for (String deviceType : Arrays.asList("pc", "mobile", "tablet", "unknown")) {
            String indexKey = buildIndexKey(userId, deviceType);
            Set<String> tokens = redisTemplate.opsForSet().members(indexKey);

            if (tokens != null) {
                for (String token : tokens) {
                    UserSessionInfo sessionInfo = getSessionInfo(userId, token);
                    if (sessionInfo != null) {
                        sessions.add(sessionInfo);
                    }
                }
            }
        }

        // 按登录时间排序
        sessions.sort(Comparator.comparing(UserSessionInfo::getLoginTime));

        return sessions;
    }

    /**
     * 获取会话信息
     *
     * @param userId 用户ID
     * @param token 访问令牌
     * @return 会话信息
     */
    public UserSessionInfo getSessionInfo(Long userId, String token) {
        String sessionKey = buildSessionKey(userId, token);
        String sessionData = redisTemplate.opsForValue().get(sessionKey);

        if (sessionData == null) {
            return null;
        }

        // 解析会话数据
        String[] parts = sessionData.split(":");
        if (parts.length >= 6) {
            UserSessionInfo info = new UserSessionInfo();
            info.setUserId(Long.parseLong(parts[0]));
            info.setToken(parts[1]);
            info.setDeviceType(parts[2]);
            info.setDeviceInfo(parts[3]);
            info.setLoginTime(new Date(Long.parseLong(parts[4])));
            info.setLastAccessTime(new Date(Long.parseLong(parts[5])));
            return info;
        }

        return null;
    }

    /**
     * 检查会话是否有效
     *
     * @param userId 用户ID
     * @param token 访问令牌
     * @return 是否有效
     */
    public boolean isSessionValid(Long userId, String token) {
        String sessionKey = buildSessionKey(userId, token);
        return Boolean.TRUE.equals(redisTemplate.hasKey(sessionKey));
    }

    /**
     * 踢出最早登录的会话
     *
     * @param userId 用户ID
     * @param deviceType 设备类型（null表示全部）
     * @return 被踢出的令牌
     */
    public String evictEarliestSession(Long userId, String deviceType) {
        List<UserSessionInfo> sessions = getActiveSessions(userId);

        // 按设备类型过滤
        if (deviceType != null) {
            sessions = sessions.stream()
                    .filter(s -> deviceType.equals(s.getDeviceType()))
                    .collect(Collectors.toList());
        }

        if (sessions.isEmpty()) {
            return null;
        }

        // 获取最早登录的会话
        UserSessionInfo earliestSession = sessions.get(0);
        String evictedToken = earliestSession.getToken();

        // 移除会话
        removeSession(userId, evictedToken);

        log.info("[并发登录] 踢出最早会话: userId={}, deviceType={}, token={}, loginTime={}",
                userId, deviceType, maskToken(evictedToken), earliestSession.getLoginTime());

        return evictedToken;
    }

    /**
     * 踢出用户所有会话（强制登出）
     *
     * @param userId 用户ID
     * @return 踢出的会话数
     */
    public int evictAllSessions(Long userId) {
        log.info("[并发登录] 踢出所有会话: userId={}", userId);

        List<UserSessionInfo> sessions = getActiveSessions(userId);
        int count = 0;

        for (UserSessionInfo session : sessions) {
            removeSession(userId, session.getToken());
            count++;
        }

        log.info("[并发登录] 已踢出{}个会话: userId={}", count, userId);
        return count;
    }

    /**
     * 获取最大并发登录数
     *
     * @param deviceType 设备类型
     * @return 最大并发数
     */
    private int getMaxConcurrentSessions(String deviceType) {
        // 可根据不同设备类型返回不同的限制
        // 例如：PC最多3个，移动端最多1个
        if ("mobile".equals(deviceType)) {
            return 1;
        } else if ("tablet".equals(deviceType)) {
            return 2;
        }
        return DEFAULT_MAX_CONCURRENT;
    }

    /**
     * 构建会话键
     */
    private String buildSessionKey(Long userId, String token) {
        return USER_SESSION_PREFIX + userId + ":" + token;
    }

    /**
     * 构建索引键
     */
    private String buildIndexKey(Long userId, String deviceType) {
        return USER_SESSION_INDEX_PREFIX + userId + ":" + (deviceType != null ? deviceType : "unknown");
    }

    /**
     * 遮盖令牌
     */
    private String maskToken(String token) {
        if (token == null) {
            return "null";
        }
        int length = Math.min(token.length(), 20);
        return token.substring(0, length) + "...";
    }

    /**
     * 用户会话信息
     */
    public static class UserSessionInfo {
        private Long userId;
        private String token;
        private String deviceType;
        private String deviceInfo;
        private Date loginTime;
        private Date lastAccessTime;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getDeviceType() {
            return deviceType;
        }

        public void setDeviceType(String deviceType) {
            this.deviceType = deviceType;
        }

        public String getDeviceInfo() {
            return deviceInfo;
        }

        public void setDeviceInfo(String deviceInfo) {
            this.deviceInfo = deviceInfo;
        }

        public Date getLoginTime() {
            return loginTime;
        }

        public void setLoginTime(Date loginTime) {
            this.loginTime = loginTime;
        }

        public Date getLastAccessTime() {
            return lastAccessTime;
        }

        public void setLastAccessTime(Date lastAccessTime) {
            this.lastAccessTime = lastAccessTime;
        }

        @Override
        public String toString() {
            return "UserSessionInfo{" +
                    "userId=" + userId +
                    ", deviceType='" + deviceType + '\'' +
                    ", deviceInfo='" + deviceInfo + '\'' +
                    ", loginTime=" + loginTime +
                    ", lastAccessTime=" + lastAccessTime +
                    '}';
        }
    }
}
