package net.lab1024.sa.common.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * JWT令牌黑名单服务
 * <p>
 * 实现JWT令牌的撤销功能，支持分布式环境
 * 使用Redis存储黑名单，确保集群环境下的一致性
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Service
@Slf4j
public class JwtTokenBlacklistService {

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    private static final String BLACKLIST_KEY_PREFIX = "jwt:blacklist:";
    private static final String USER_SESSION_PREFIX = "jwt:session:";
    private static final long BLACKLIST_TTL_SECONDS = 7 * 24 * 60 * 60; // 7天
    private static final long SESSION_TTL_SECONDS = 24 * 60 * 60; // 24小时

    /**
     * 将令牌加入黑名单
     *
     * @param token JWT令牌
     * @param expSeconds 令牌过期时间（秒）
     */
    public void blacklistToken(String token, Long expSeconds) {
        log.info("[JWT黑名单] 添加令牌: token={}", maskToken(token));

        // 计算黑名单TTL：令牌剩余有效期或7天，取较小值
        long ttl = Math.min(expSeconds != null ? expSeconds : BLACKLIST_TTL_SECONDS, BLACKLIST_TTL_SECONDS);

        // 存储到Redis，支持分布式
        String key = BLACKLIST_KEY_PREFIX + token;
        redisTemplate.opsForValue().set(key, "1", ttl, TimeUnit.SECONDS);

        log.info("[JWT黑名单] 令牌已加入黑名单: ttl={}秒", ttl);
    }

    /**
     * 检查令牌是否在黑名单中
     *
     * @param token JWT令牌
     * @return true=已撤销
     */
    public boolean isTokenBlacklisted(String token) {
        String key = BLACKLIST_KEY_PREFIX + token;
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    /**
     * 保存用户会话信息
     *
     * @param userId 用户ID
     * @param token JWT令牌
     * @param deviceInfo 设备信息
     */
    public void saveUserSession(Long userId, String token, String deviceInfo) {
        String sessionKey = USER_SESSION_PREFIX + userId + ":" + token;

        // 存储会话信息
        String sessionData = userId + ":" + token + ":" + deviceInfo + ":" + System.currentTimeMillis();
        redisTemplate.opsForValue().set(sessionKey, sessionData, SESSION_TTL_SECONDS, TimeUnit.SECONDS);

        log.debug("[JWT会话] 保存用户会话: userId={}, device={}", userId, deviceInfo);
    }

    /**
     * 批量撤销用户的所有令牌
     *
     * @param userId 用户ID
     * @return 撤销的令牌数量
     */
    public int revokeAllUserTokens(Long userId) {
        log.info("[JWT黑名单] 撤销用户所有令牌: userId={}", userId);

        // 从Redis中查找该用户的所有活跃会话
        String pattern = USER_SESSION_PREFIX + userId + ":*";
        Set<String> sessionKeys = redisTemplate.keys(pattern);

        if (sessionKeys == null || sessionKeys.isEmpty()) {
            log.debug("[JWT黑名单] 用户没有活跃会话: userId={}", userId);
            return 0;
        }

        // 批量加入黑名单
        int revokedCount = 0;
        for (String sessionKey : sessionKeys) {
            // 从会话key中提取token
            String token = extractTokenFromSessionKey(sessionKey);
            if (token != null) {
                blacklistToken(token, null);
                revokedCount++;
            }
        }

        // 删除用户会话记录
        redisTemplate.delete(sessionKeys);

        log.info("[JWT黑名单] 已撤销{}个令牌: userId={}", revokedCount, userId);
        return revokedCount;
    }

    /**
     * 移除用户会话（登出时调用）
     *
     * @param userId 用户ID
     * @param token JWT令牌
     */
    public void removeUserSession(Long userId, String token) {
        String sessionKey = USER_SESSION_PREFIX + userId + ":" + token;
        redisTemplate.delete(sessionKey);
        log.debug("[JWT会话] 移除会话: userId={}", userId);
    }

    /**
     * 验证用户会话是否有效
     *
     * @param userId 用户ID
     * @param token JWT令牌
     * @return true=会话有效
     */
    public boolean isValidUserSession(Long userId, String token) {
        String sessionKey = USER_SESSION_PREFIX + userId + ":" + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(sessionKey));
    }

    /**
     * 清理过期的黑名单记录
     * 定时任务，每天凌晨2点执行
     */
    public void cleanExpiredBlacklistEntries() {
        log.info("[JWT黑名单] 开始清理过期记录");

        // Redis会自动清理过期key，这里主要是记录日志
        // 如果需要手动清理，可以 SCAN 遍历 BLACKLIST_KEY_PREFIX 并检查TTL

        log.debug("[JWT黑名单] 清理过期记录完成");
    }

    /**
     * 获取黑名单统计信息
     *
     * @return 统计信息
     */
    public BlacklistStatistics getBlacklistStatistics() {
        // 统计当前黑名单中的令牌数量
        String pattern = BLACKLIST_KEY_PREFIX + "*";
        Set<String> keys = redisTemplate.keys(pattern);

        BlacklistStatistics stats = new BlacklistStatistics();
        stats.setTotalBlacklisted(keys != null ? (long) keys.size() : 0L);
        stats.setQueryTime(System.currentTimeMillis());

        log.debug("[JWT黑名单] 统计信息: total={}", stats.getTotalBlacklisted());
        return stats;
    }

    /**
     * 从会话key中提取token
     */
    private String extractTokenFromSessionKey(String sessionKey) {
        // sessionKey格式: jwt:session:{userId}:{token}
        int lastColonIndex = sessionKey.lastIndexOf(':');
        if (lastColonIndex > 0 && lastColonIndex < sessionKey.length() - 1) {
            return sessionKey.substring(lastColonIndex + 1);
        }
        return null;
    }

    /**
     * 遮盖令牌，只显示前20个字符
     */
    private String maskToken(String token) {
        if (token == null) {
            return "null";
        }
        int length = Math.min(token.length(), 20);
        return token.substring(0, length) + "...";
    }

    /**
     * 黑名单统计信息
     */
    public static class BlacklistStatistics {
        private Long totalBlacklisted;
        private Long queryTime;

        public Long getTotalBlacklisted() {
            return totalBlacklisted;
        }

        public void setTotalBlacklisted(Long totalBlacklisted) {
            this.totalBlacklisted = totalBlacklisted;
        }

        public Long getQueryTime() {
            return queryTime;
        }

        public void setQueryTime(Long queryTime) {
            this.queryTime = queryTime;
        }
    }
}
