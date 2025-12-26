package net.lab1024.sa.gateway.manager;

import lombok.extern.slf4j.Slf4j;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;


/**
 * 限流管理器
 * <p>
 * 管理网关限流策略和配置
 * 严格遵循CLAUDE.md规范：
 * - Manager层负责复杂业务逻辑
 * - 保持纯Java特性，不使用Spring注解
 * - 通过Configuration类注册为Bean
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
public class RateLimitManager {


    private final Map<String, RateLimitConfig> configCache = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> requestCounters = new ConcurrentHashMap<>();
    private final Map<String, Long> lastResetTime = new ConcurrentHashMap<>();

    /**
     * 检查是否允许请求
     *
     * @param key 限流键（如IP、用户ID、API路径）
     * @return 是否允许
     */
    public boolean isAllowed(String key) {
        RateLimitConfig config = configCache.get(key);
        if (config == null) {
            config = getDefaultConfig();
        }

        long now = System.currentTimeMillis();
        Long lastReset = lastResetTime.get(key);

        if (lastReset == null || now - lastReset > config.getWindowMs()) {
            requestCounters.put(key, new AtomicLong(0));
            lastResetTime.put(key, now);
        }

        AtomicLong counter = requestCounters.computeIfAbsent(key, k -> new AtomicLong(0));
        long currentCount = counter.incrementAndGet();

        boolean allowed = currentCount <= config.getMaxRequests();
        if (!allowed) {
            log.warn("[限流管理] 请求被限流，key={}, currentCount={}, maxRequests={}",
                    key, currentCount, config.getMaxRequests());
        }

        return allowed;
    }

    /**
     * 设置限流配置
     *
     * @param key 限流键
     * @param config 限流配置
     */
    public void setRateLimitConfig(String key, RateLimitConfig config) {
        log.info("[限流管理] 设置限流配置，key={}, maxRequests={}, windowMs={}",
                key, config.getMaxRequests(), config.getWindowMs());
        configCache.put(key, config);
    }

    /**
     * 获取限流配置
     *
     * @param key 限流键
     * @return 限流配置
     */
    public RateLimitConfig getRateLimitConfig(String key) {
        return configCache.getOrDefault(key, getDefaultConfig());
    }

    /**
     * 删除限流配置
     *
     * @param key 限流键
     */
    public void removeRateLimitConfig(String key) {
        log.info("[限流管理] 删除限流配置，key={}", key);
        configCache.remove(key);
        requestCounters.remove(key);
        lastResetTime.remove(key);
    }

    /**
     * 获取限流统计
     *
     * @return 统计信息
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalConfigs", configCache.size());
        stats.put("activeCounters", requestCounters.size());

        long totalBlocked = requestCounters.values().stream()
                .mapToLong(AtomicLong::get)
                .sum();
        stats.put("totalRequests", totalBlocked);

        return stats;
    }

    /**
     * 重置所有计数器
     */
    public void resetAllCounters() {
        log.info("[限流管理] 重置所有计数器");
        requestCounters.clear();
        lastResetTime.clear();
    }

    /**
     * 获取默认配置
     */
    private RateLimitConfig getDefaultConfig() {
        RateLimitConfig config = new RateLimitConfig();
        config.setMaxRequests(100);
        config.setWindowMs(60000); // 1分钟
        return config;
    }

    /**
     * 限流配置
     */
    @lombok.Data
    public static class RateLimitConfig {
        private long maxRequests = 100;
        private long windowMs = 60000;
        private String strategy = "SLIDING_WINDOW";
        private boolean enabled = true;
    }
}
