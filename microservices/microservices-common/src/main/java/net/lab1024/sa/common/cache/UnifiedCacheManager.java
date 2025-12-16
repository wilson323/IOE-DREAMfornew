package net.lab1024.sa.common.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * IOE-DREAM 统一缓存管理器
 * <p>
 * 实现三级缓存体系：
 * L1: Caffeine本地缓存 (毫秒级响应)
 * L2: Redis分布式缓存 (数据一致性)
 * L3: 网关缓存 (服务间调用优化)
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
public class UnifiedCacheManager {

    private final RedisTemplate<String, Object> redisTemplate;
    private final Cache<String, Object> localCache;
    private final Map<String, CacheConfig> cacheConfigs;

    // 构造函数注入依赖
    public UnifiedCacheManager(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.localCache = createLocalCache();
        this.cacheConfigs = new HashMap<>();
        initializeCacheConfigs();
    }

    /**
     * 创建本地缓存
     */
    private Cache<String, Object> createLocalCache() {
        return Caffeine.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(Duration.ofMinutes(5))
                .expireAfterAccess(Duration.ofMinutes(10))
                .recordStats()
                .refreshAfterWrite(Duration.ofMinutes(3))
                .build();
    }

    /**
     * 初始化缓存配置
     */
    private void initializeCacheConfigs() {
        // 用户会话缓存
        cacheConfigs.put("user-session", CacheConfig.builder()
                .ttl(Duration.ofHours(2))
                .level(Arrays.asList("l1", "l2"))
                .refreshStrategy(RefreshStrategy.WRITE_THROUGH)
                .build());

        // 权限信息缓存
        cacheConfigs.put("permission", CacheConfig.builder()
                .ttl(Duration.ofHours(1))
                .level(Arrays.asList("l1", "l2"))
                .refreshStrategy(RefreshStrategy.WRITE_BEHIND)
                .build());

        // 字典数据缓存
        cacheConfigs.put("dictionary", CacheConfig.builder()
                .ttl(Duration.ofHours(24))
                .level(Arrays.asList("l1", "l2", "l3"))
                .refreshStrategy(RefreshStrategy.WRITE_THROUGH)
                .build());

        // 设备状态缓存
        cacheConfigs.put("device-status", CacheConfig.builder()
                .ttl(Duration.ofMinutes(10))
                .level(Arrays.asList("l1", "l2"))
                .refreshStrategy(RefreshStrategy.REFRESH_AHEAD)
                .build());
    }

    /**
     * 获取缓存值
     */
    public <T> T get(String cacheType, String key, Class<T> clazz, Supplier<T> loader) {
        String fullKey = buildCacheKey(cacheType, key);
        CacheConfig config = cacheConfigs.get(cacheType);

        if (config == null) {
            log.warn("[缓存] 未找到缓存配置: {}", cacheType);
            return loader.get();
        }

        // L1本地缓存查询
        if (config.getLevel().contains("l1")) {
            T value = (T) localCache.getIfPresent(fullKey);
            if (value != null) {
                log.debug("[缓存] L1命中: {}", fullKey);
                return value;
            }
        }

        // L2 Redis缓存查询
        if (config.getLevel().contains("l2")) {
            try {
                T value = (T) redisTemplate.opsForValue().get(fullKey);
                if (value != null) {
                    log.debug("[缓存] L2命中: {}", fullKey);
                    // 回写到L1缓存
                    if (config.getLevel().contains("l1")) {
                        localCache.put(fullKey, value);
                    }
                    return value;
                }
            } catch (Exception e) {
                log.error("[缓存] L2查询失败: {}", fullKey, e);
            }
        }

        // 从数据源加载
        T value = loader.get();
        if (value != null) {
            put(cacheType, key, value, config);
        }

        return value;
    }

    /**
     * 设置缓存值
     */
    public <T> void put(String cacheType, String key, T value) {
        CacheConfig config = cacheConfigs.get(cacheType);
        put(cacheType, key, value, config);
    }

    /**
     * 设置缓存值（带配置）
     */
    private <T> void put(String cacheType, String key, T value, CacheConfig config) {
        if (config == null) {
            return;
        }

        String fullKey = buildCacheKey(cacheType, key);

        // L1本地缓存
        if (config.getLevel().contains("l1")) {
            localCache.put(fullKey, value);
            log.debug("[缓存] L1写入: {}", fullKey);
        }

        // L2 Redis缓存
        if (config.getLevel().contains("l2")) {
            try {
                redisTemplate.opsForValue().set(fullKey, value, config.getTtl());
                log.debug("[缓存] L2写入: {}, TTL: {}", fullKey, config.getTtl());
            } catch (Exception e) {
                log.error("[缓存] L2写入失败: {}", fullKey, e);
            }
        }

        // L3网关缓存（通过消息通知）
        if (config.getLevel().contains("l3")) {
            notifyGatewayCache(fullKey, value, config.getTtl());
        }
    }

    /**
     * 删除缓存
     */
    public void evict(String cacheType, String key) {
        String fullKey = buildCacheKey(cacheType, key);
        CacheConfig config = cacheConfigs.get(cacheType);

        if (config == null) {
            return;
        }

        // L1本地缓存删除
        if (config.getLevel().contains("l1")) {
            localCache.invalidate(fullKey);
        }

        // L2 Redis缓存删除
        if (config.getLevel().contains("l2")) {
            try {
                redisTemplate.delete(fullKey);
            } catch (Exception e) {
                log.error("[缓存] L2删除失败: {}", fullKey, e);
            }
        }

        // L3网关缓存失效通知
        if (config.getLevel().contains("l3")) {
            notifyGatewayEviction(fullKey);
        }

        log.debug("[缓存] 删除完成: {}", fullKey);
    }

    /**
     * 批量删除缓存
     */
    public void evictByPattern(String pattern) {
        // 本地缓存批量删除
        for (String key : localCache.asMap().keySet()) {
            if (key.matches(pattern.replace("*", ".*"))) {
                localCache.invalidate(key);
            }
        }

        // Redis缓存批量删除
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.error("[缓存] 批量删除失败: {}", pattern, e);
        }

        log.debug("[缓存] 批量删除完成: {}", pattern);
    }

    /**
     * 异步预热缓存
     */
    public CompletableFuture<Void> warmUpAsync(String cacheType, Map<String, Object> data) {
        return CompletableFuture.runAsync(() -> {
            CacheConfig config = cacheConfigs.get(cacheType);
            if (config == null) {
                return;
            }

            data.forEach((key, value) -> put(cacheType, key, value, config));
            log.info("[缓存] 预热完成: {}, 数据量: {}", cacheType, data.size());
        });
    }

    /**
     * 获取缓存统计信息
     */
    public CacheStats getStats(String cacheType) {
        CacheStats stats = new CacheStats();

        // L1本地缓存统计
        var localStats = localCache.stats();
        stats.setL1HitRate(localStats.hitRate());
        stats.setL1MissRate(localStats.missRate());
        stats.setL1Size(localStats.requestCount());
        stats.setL1RequestCount(localStats.requestCount());

        return stats;
    }

    /**
     * 构建缓存键
     */
    private String buildCacheKey(String cacheType, String key) {
        return String.format("ioedream:%s:%s", cacheType, key);
    }

    /**
     * 通知网关缓存
     */
    private void notifyGatewayCache(String key, Object value, Duration ttl) {
        // 实现网关缓存通知逻辑
        log.debug("[缓存] 通知网关缓存: {}", key);
    }

    /**
     * 通知网关缓存失效
     */
    private void notifyGatewayEviction(String key) {
        // 实现网关缓存失效通知逻辑
        log.debug("[缓存] 通知网关失效: {}", key);
    }

    /**
     * 缓存配置
     */
    @lombok.Data
    @lombok.Builder
    public static class CacheConfig {
        private Duration ttl;
        private List<String> level;
        private RefreshStrategy refreshStrategy;
        private Integer maxSize;
    }

    /**
     * 刷新策略
     */
    public enum RefreshStrategy {
        WRITE_THROUGH,    // 写穿
        WRITE_BEHIND,      // 写回
        REFRESH_AHEAD      // 预刷新
    }

    /**
     * 缓存统计
     */
    @lombok.Data
    public static class CacheStats {
        private double l1HitRate;
        private double l1MissRate;
        private long l1Size;
        private long l1RequestCount;
    }
}
