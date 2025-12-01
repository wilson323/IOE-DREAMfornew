package net.lab1024.sa.base.module.support.securityprotect.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Level3保护配置服务实现
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-27
 */
@Slf4j
@Service
public class Level3ProtectConfigServiceImpl implements Level3ProtectConfigService {

    /**
     * IP黑名单Redis缓存键
     */
    private static final String BLACK_LIST_KEY_PREFIX = "black_list:";

    /**
     * 限流保护Redis缓存键
     */
    private static final String RATE_LIMIT_KEY_PREFIX = "rate_limit:";

    @Value("${security.level3.protect.enabled:false}")
    private boolean protectionEnabled;

    @Value("${security.level3.max-requests-per-minute:100}")
    private int maxRequestsPerMinute;

    @Value("${security.level3.max-concurrent-connections:50}")
    private int maxConcurrentConnections;

    @Value("${security.level3.black-list-check-interval-minutes:5}")
    private int blackListCheckIntervalMinutes;

    @Value("${security.level3.login-active-timeout-seconds:1800}")
    private int loginActiveTimeoutSeconds;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean isProtectionEnabled() {
        return protectionEnabled;
    }

    @Override
    public int getMaxRequestsPerMinute() {
        return maxRequestsPerMinute;
    }

    @Override
    public int getMaxConcurrentConnections() {
        return maxConcurrentConnections;
    }

    @Override
    public int getBlackListCheckIntervalMinutes() {
        return blackListCheckIntervalMinutes;
    }

    @Override
    public int getLoginActiveTimeoutSeconds() {
        return loginActiveTimeoutSeconds;
    }

    @Override
    public boolean isIpInBlackList(String ipAddress) {
        try {
            return redisTemplate.hasKey(BLACK_LIST_KEY_PREFIX + ipAddress);
        } catch (Exception e) {
            log.error("检查IP黑名单失败", e);
            return false;
        }
    }

    @Override
    public void addIpToBlackList(String ipAddress, int durationMinutes) {
        try {
            redisTemplate.opsForValue().set(BLACK_LIST_KEY_PREFIX + ipAddress, System.currentTimeMillis(),
                durationMinutes, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("添加IP到黑名单失败", e);
        }
    }

    @Override
    public void removeIpFromBlackList(String ipAddress) {
        try {
            redisTemplate.delete(BLACK_LIST_KEY_PREFIX + ipAddress);
        } catch (Exception e) {
            log.error("从黑名单移除IP失败", e);
        }
    }

    @Override
    public boolean isRateLimitExceeded(String ipAddress, String endpoint) {
        try {
            String key = RATE_LIMIT_KEY_PREFIX + ipAddress + ":" + endpoint;
            Long currentCount = redisTemplate.opsForValue().increment(key);
            if (currentCount == 1) {
                redisTemplate.expire(key, 1, TimeUnit.MINUTES);
            }
            return currentCount > maxRequestsPerMinute;
        } catch (Exception e) {
            log.error("检查限流失败", e);
            return false; // 出错时不触发限制
        }
    }

    @Override
    public void recordAccess(String ipAddress, String endpoint) {
        try {
            String key = RATE_LIMIT_KEY_PREFIX + "access:" + ipAddress + ":" + endpoint;
            redisTemplate.opsForValue().increment(key);
            redisTemplate.expire(key, 1, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("记录访问失败", e);
        }
    }
}