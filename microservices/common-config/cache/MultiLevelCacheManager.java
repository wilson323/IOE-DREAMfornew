package net.lab1024.sa.common.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 多级缓存管理器
 *
 * 针对1000台设备、20000人规模优化的三级缓存架构
 *
 * @author IOE-DREAM Team
 * @date 2025-12-09
 * @description L1本地缓存 + L2Redis缓存 + L3数据库缓存的统一管理
 */
@Slf4j
public class MultiLevelCacheManager {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedissonClient redissonClient;

    // L1本地缓存 (Caffeine)
    private final Cache<String, Object> l1Cache;

    // 缓存配置
    private static final String L1_PREFIX = "L1:";
    private static final String L2_PREFIX = "L2:";
    private static final String LOCK_PREFIX = "LOCK:";
    private static final int DEFAULT_LOCK_TIMEOUT = 30; // 秒

    public MultiLevelCacheManager(RedisTemplate<String, Object> redisTemplate, RedissonClient redissonClient) {
        this.redisTemplate = redisTemplate;
        this.redissonClient = redissonClient;

        // 针对单企业20000人规模优化的L1缓存配置
        this.l1Cache = Caffeine.newBuilder()
                .maximumSize(10000)                    // 最大缓存10000个key
                .expireAfterWrite(5, TimeUnit.MINUTES)  // 写入后5分钟过期
                .expireAfterAccess(2, TimeUnit.MINUTES)  // 访问后2分钟过期
                .recordStats()                         // 启用统计
                .refreshAfterWrite(1, TimeUnit.MINUTES)  // 写入后1分钟刷新
                .build();
    }

    // ============================================================
    # 获取缓存 - 三级缓存架构
    # ============================================================

    /**
     * 获取缓存数据 - 三级缓存架构
     * L1(本地) -> L2(Redis) -> L3(数据库加载)
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type, Supplier<T> loader) {
        return get(key, type, loader, 30, TimeUnit.MINUTES);
    }

    /**
     * 获取缓存数据 - 指定TTL
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type, Supplier<T> loader, long ttl, TimeUnit unit) {
        String l1Key = L1_PREFIX + key;
        String l2Key = L2_PREFIX + key;

        try {
            // L1本地缓存查询
            T value = (T) l1Cache.getIfPresent(l1Key);
            if (value != null) {
                log.debug("[多级缓存] L1缓存命中: key={}", key);
                return value;
            }

            // L2 Redis缓存查询
            Object redisValue = redisTemplate.opsForValue().get(l2Key);
            if (redisValue != null) {
                value = type.cast(redisValue);
                l1Cache.put(l1Key, value);
                log.debug("[多级缓存] L2缓存命中: key={}", key);
                return value;
            }

            // L3数据库加载（使用分布式锁防止缓存击穿）
            value = loadWithLock(key, type, loader, ttl, unit);

            // 异步预热L1缓存
            l1Cache.put(l1Key, value);

            log.debug("[多级缓存] L3加载成功: key={}", key);
            return value;

        } catch (Exception e) {
            log.error("[多级缓存] 获取缓存失败: key={}, error={}", key, e.getMessage(), e);
            // 降级处理：直接加载器加载
            return loader.get();
        }
    }

    /**
     * 使用分布式锁加载数据（防止缓存击穿）
     */
    @SuppressWarnings("unchecked")
    private <T> T loadWithLock(String key, Class<T> type, Supplier<T> loader, long ttl, TimeUnit unit) {
        String lockKey = LOCK_PREFIX + key;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 尝试获取锁，最多等待5秒，锁定30秒
            boolean acquired = lock.tryLock(5, DEFAULT_LOCK_TIMEOUT, TimeUnit.SECONDS);
            if (acquired) {
                try {
                    // 双重检查，防止重复加载
                    String l2Key = L2_PREFIX + key;
                    Object redisValue = redisTemplate.opsForValue().get(l2Key);
                    if (redisValue != null) {
                        return type.cast(redisValue);
                    }

                    // 加载数据
                    T value = loader.get();

                    // 设置L2缓存
                    redisTemplate.opsForValue().set(l2Key, value, ttl, unit);

                    log.info("[多级缓存] 数据加载并缓存: key={}", key);
                    return value;

                } finally {
                    lock.unlock();
                }
            } else {
                // 获取锁失败，降级处理：使用加载器
                log.warn("[多级缓存] 获取锁失败，降级处理: key={}", key);
                return loader.get();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[多级缓存] 获取锁被中断: key={}", key, e);
            return loader.get();
        }
    }

    /**
     * 批量获取缓存 - 针对20000人规模优化
     */
    public <T> java.util.Map<String, T> getBatch(java.util.Collection<String> keys, Class<T> type,
                                               Function<String, T> loader) {
        java.util.Map<String, T> result = new java.util.HashMap<>();

        keys.parallelStream().forEach(key -> {
            try {
                T value = get(key, type, () -> loader.apply(key));
                if (value != null) {
                    result.put(key, value);
                }
            } catch (Exception e) {
                log.error("[多级缓存] 批量获取失败: key={}, error={}", key, e.getMessage());
            }
        });

        return result;
    }

    // ============================================================
    # 设置缓存
    # ============================================================

    /**
     * 设置缓存 - 同步更新L1和L2
     */
    public void put(String key, Object value) {
        put(key, value, 30, TimeUnit.MINUTES);
    }

    /**
     * 设置缓存 - 指定TTL
     */
    public void put(String key, Object value, long ttl, TimeUnit unit) {
        try {
            String l1Key = L1_PREFIX + key;
            String l2Key = L2_PREFIX + key;

            // 更新L1缓存
            l1Cache.put(l1Key, value);

            // 更新L2缓存
            redisTemplate.opsForValue().set(l2Key, value, ttl, unit);

            log.debug("[多级缓存] 设置缓存: key={}, ttl={}{}", key, ttl, unit);

        } catch (Exception e) {
            log.error("[多级缓存] 设置缓存失败: key={}, error={}", key, e.getMessage(), e);
        }
    }

    /**
     * 异步设置缓存 - 提升性能
     */
    public void putAsync(String key, Object value, long ttl, TimeUnit unit) {
        CompletableFuture.runAsync(() -> put(key, value, ttl, unit))
                .exceptionally(throwable -> {
                    log.error("[多级缓存] 异步设置缓存失败: key={}, error={}", key, throwable.getMessage());
                    return null;
                });
    }

    // ============================================================
    # 删除缓存
    # ============================================================

    /**
     * 删除缓存
     */
    public void evict(String key) {
        try {
            String l1Key = L1_PREFIX + key;
            String l2Key = L2_PREFIX + key;

            // 删除L1缓存
            l1Cache.invalidate(l1Key);

            // 删除L2缓存
            redisTemplate.delete(l2Key);

            log.debug("[多级缓存] 删除缓存: key={}", key);

        } catch (Exception e) {
            log.error("[多级缓存] 删除缓存失败: key={}, error={}", key, e.getMessage(), e);
        }
    }

    /**
     * 批量删除缓存
     */
    public void evictBatch(java.util.Collection<String> keys) {
        keys.parallelStream().forEach(this::evict);
    }

    /**
     * 清空所有缓存
     */
    public void clear() {
        try {
            // 清空L1缓存
            l1Cache.invalidateAll();

            // 清空L2缓存（按前缀批量删除）
            String pattern = L2_PREFIX + "*";
            java.util.Set<String> keys = redisTemplate.keys(pattern);
            if (!keys.isEmpty()) {
                redisTemplate.delete(keys);
            }

            log.info("[多级缓存] 清空所有缓存");

        } catch (Exception e) {
            log.error("[多级缓存] 清空缓存失败: error={}", e.getMessage(), e);
        }
    }

    // ============================================================
    # 缓存预热
    # ============================================================

    /**
     * 缓存预热 - 针对1000台设备、20000人规模
     */
    public void warmup(java.util.Map<String, Supplier<Object>> warmupData) {
        log.info("[多级缓存] 开始缓存预热: {} 个key", warmupData.size());

        warmupData.entrySet().parallelStream().forEach(entry -> {
            try {
                String key = entry.getKey();
                Supplier<Object> loader = entry.getValue();

                // 预加载到L2缓存（不加载到L1，按需加载）
                Object value = loader.get();
                redisTemplate.opsForValue().set(L2_PREFIX + key, value, 60, TimeUnit.MINUTES);

                log.debug("[多级缓存] 预热完成: key={}", key);

            } catch (Exception e) {
                log.error("[多级缓存] 预热失败: key={}, error={}", entry.getKey(), e.getMessage());
            }
        });

        log.info("[多级缓存] 缓存预热完成");
    }

    // ============================================================
    # 缓存统计
    # ============================================================

    /**
     * 获取缓存统计信息
     */
    public CacheStats getStats() {
        return CacheStats.builder()
                .l1Stats(l1Cache.stats())
                .build();
    }

    // ============================================================
    # 缓存同步 - 集群环境
    # ============================================================

    /**
     * 广播缓存失效消息
     */
    public void broadcastEviction(String key) {
        try {
            String channel = "ioedream:cache:sync";
            CacheEvictionMessage message = new CacheEvictionMessage(key, System.currentTimeMillis());

            redisTemplate.convertAndSend(channel, message);

            log.debug("[多级缓存] 广播缓存失效: key={}", key);

        } catch (Exception e) {
            log.error("[多级缓存] 广播缓存失效失败: key={}, error={}", key, e.getMessage(), e);
        }
    }

    /**
     * 处理缓存失效消息
     */
    public void handleEvictionMessage(String key) {
        try {
            String l1Key = L1_PREFIX + key;
            l1Cache.invalidate(l1Key);

            log.debug("[多级缓存] 处理缓存失效消息: key={}", key);

        } catch (Exception e) {
            log.error("[多级缓存] 处理缓存失效消息失败: key={}, error={}", key, e.getMessage(), e);
        }
    }

    // ============================================================
    # 内部类
    # ============================================================

    /**
     * 缓存统计信息
     */
    @lombok.Data
    @lombok.Builder
    public static class CacheStats {
        private com.github.benmanes.caffeine.cache.stats.CacheStats l1Stats;
    }

    /**
     * 缓存失效消息
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class CacheEvictionMessage {
        private String key;
        private long timestamp;
    }
}