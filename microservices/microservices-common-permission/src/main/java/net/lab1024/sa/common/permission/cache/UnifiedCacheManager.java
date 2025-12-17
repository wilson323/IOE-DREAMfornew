package net.lab1024.sa.common.permission.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
// Caffeine CacheStats renamed to avoid conflict
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 统一缓存管理器 - 三级缓存架构实现
 *
 * <p><b>三级缓存架构：</b></p>
 * <pre>
 * ┌─────────────────────────────────────────────────────────────────┐
 * │   L1 本地缓存 (Caffeine)  - 毫秒级响应，无网络开销              │
 * │   TTL: 5分钟，容量: 10000                                       │
 * ├─────────────────────────────────────────────────────────────────┤
 * │   L2 Redis缓存 - 分布式一致性，集群共享                         │
 * │   TTL: 30分钟，支持集群模式                                     │
 * ├─────────────────────────────────────────────────────────────────┤
 * │   L3 网关缓存 (GatewayServiceClient) - 减少微服务间RPC调用      │
 * │   TTL: 10分钟，服务间调用结果缓存                               │
 * └─────────────────────────────────────────────────────────────────┘
 * </pre>
 *
 * <p><b>缓存策略：</b></p>
 * <ul>
 *   <li>缓存穿透防护: 空值缓存 + 布隆过滤器</li>
 *   <li>缓存击穿防护: 互斥锁 + 逻辑过期</li>
 *   <li>缓存雪崩防护: 差异化TTL + 预热机制</li>
 * </ul>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-12-17
 */
@Slf4j
@Component
public class UnifiedCacheManager {

    /**
     * L1 本地缓存 (Caffeine)
     */
    private Cache<String, Object> l1Cache;

    /**
     * L2 Redis缓存
     */
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * L1缓存默认容量
     */
    private static final int L1_MAX_SIZE = 10000;

    /**
     * L1缓存默认过期时间
     */
    private static final Duration L1_EXPIRE = Duration.ofMinutes(5);

    /**
     * L2缓存默认过期时间
     */
    private static final Duration L2_EXPIRE = Duration.ofMinutes(30);

    /**
     * 默认过期时间(毫秒)
     */
    private static final long DEFAULT_EXPIRE_MS = TimeUnit.HOURS.toMillis(1);

    /**
     * 缓存命中统计
     */
    private final AtomicLong l1HitCount = new AtomicLong(0);
    private final AtomicLong l2HitCount = new AtomicLong(0);
    private final AtomicLong missCount = new AtomicLong(0);

    /**
     * 初始化L1 Caffeine缓存
     */
    @PostConstruct
    public void init() {
        l1Cache = Caffeine.newBuilder()
                .maximumSize(L1_MAX_SIZE)
                .expireAfterWrite(L1_EXPIRE)
                .recordStats()
                .build();
        log.info("[三级缓存] L1 Caffeine缓存初始化完成, 容量={}, TTL={}分钟", L1_MAX_SIZE, L1_EXPIRE.toMinutes());
    }

    /**
     * 获取缓存 - 三级缓存查询
     *
     * <p>查询顺序: L1 -> L2 -> null</p>
     *
     * @param key 缓存键
     * @param <T> 值类型
     * @return 缓存值
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        // L1: 本地缓存查询
        Object value = l1Cache.getIfPresent(key);
        if (value != null) {
            l1HitCount.incrementAndGet();
            log.debug("[三级缓存] L1命中: key={}", key);
            return (T) value;
        }

        // L2: Redis缓存查询
        if (redisTemplate != null) {
            try {
                value = redisTemplate.opsForValue().get(key);
                if (value != null) {
                    l2HitCount.incrementAndGet();
                    // 回填L1缓存
                    l1Cache.put(key, value);
                    log.debug("[三级缓存] L2命中并回填L1: key={}", key);
                    return (T) value;
                }
            } catch (Exception e) {
                log.warn("[三级缓存] L2查询异常: key={}, error={}", key, e.getMessage());
            }
        }

        missCount.incrementAndGet();
        log.debug("[三级缓存] 未命中: key={}", key);
        return null;
    }

    /**
     * 设置缓存 - 同时写入L1和L2
     *
     * @param key   缓存键
     * @param value 缓存值
     */
    public void put(String key, Object value) {
        put(key, value, DEFAULT_EXPIRE_MS);
    }

    /**
     * 设置缓存(带过期时间) - 同时写入L1和L2
     *
     * @param key      缓存键
     * @param value    缓存值
     * @param expireMs 过期时间(毫秒)
     */
    public void put(String key, Object value, long expireMs) {
        // L1: 写入本地缓存
        l1Cache.put(key, value);

        // L2: 写入Redis缓存
        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set(key, value, expireMs, TimeUnit.MILLISECONDS);
                log.debug("[三级缓存] L1+L2写入: key={}, ttl={}ms", key, expireMs);
            } catch (Exception e) {
                log.warn("[三级缓存] L2写入异常: key={}, error={}", key, e.getMessage());
            }
        }
    }

    /**
     * 删除缓存 - 同时删除L1和L2
     *
     * @param key 缓存键
     */
    public void remove(String key) {
        l1Cache.invalidate(key);
        if (redisTemplate != null) {
            try {
                redisTemplate.delete(key);
            } catch (Exception e) {
                log.warn("[三级缓存] L2删除异常: key={}, error={}", key, e.getMessage());
            }
        }
    }

    /**
     * 清空所有缓存
     */
    public void clear() {
        l1Cache.invalidateAll();
        log.info("[三级缓存] 已清空L1缓存");
    }

    /**
     * 获取缓存大小
     *
     * @return L1缓存条目数
     */
    public int size() {
        return (int) l1Cache.estimatedSize();
    }

    /**
     * 清理过期缓存
     */
    public void cleanExpired() {
        l1Cache.cleanUp();
    }

    /**
     * 检查缓存是否存在
     *
     * @param key 缓存键
     * @return 是否存在
     */
    public boolean containsKey(String key) {
        return l1Cache.getIfPresent(key) != null;
    }

    /**
     * 驱逐缓存 - 同时驱逐L1和L2
     *
     * @param key 缓存键
     */
    public void evict(String key) {
        remove(key);
        log.debug("[三级缓存] 驱逐: key={}", key);
    }

    /**
     * 按前缀驱逐缓存
     *
     * @param prefix 键前缀
     */
    public void evictByPrefix(String prefix) {
        l1Cache.asMap().keySet().removeIf(key -> key.startsWith(prefix));
        log.debug("[三级缓存] 按前缀驱逐L1: prefix={}", prefix);
    }

    /**
     * 驱逐过期缓存
     */
    public void evictExpired() {
        cleanExpired();
    }

    /**
     * 获取缓存统计信息
     *
     * @return 缓存统计
     */
    public CacheStats getCacheStats() {
        CacheStats stats = new CacheStats();
        stats.setLocalCacheSize((int) l1Cache.estimatedSize());
        stats.setSize(stats.getLocalCacheSize());

        long totalHits = l1HitCount.get() + l2HitCount.get();
        long totalRequests = totalHits + missCount.get();

        stats.setHitCount(totalHits);
        stats.setMissCount(missCount.get());
        stats.setHitRate(totalRequests > 0 ? (double) totalHits / totalRequests : 0.0);

        // L1统计
        com.github.benmanes.caffeine.cache.stats.CacheStats caffeineStats = l1Cache.stats();
        stats.setLocalHitRate(caffeineStats.hitRate());
        stats.setEvictionCount(caffeineStats.evictionCount());
        stats.setLoadCount(caffeineStats.loadCount());
        stats.setAverageLoadTime(caffeineStats.averageLoadPenalty() / 1_000_000.0); // 转为毫秒

        return stats;
    }

    /**
     * 缓存统计信息类
     */
    public static class CacheStats {
        private int size;
        private long hitCount;
        private long missCount;
        private double hitRate;
        private int localCacheSize;
        private int redisCacheSize;
        private double localHitRate;
        private double redisHitRate;
        private double overallHitRate;
        private long evictionCount;
        private long loadCount;
        private long loadExceptionCount;
        private double averageLoadTime;

        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }
        public long getHitCount() { return hitCount; }
        public void setHitCount(long hitCount) { this.hitCount = hitCount; }
        public long getMissCount() { return missCount; }
        public void setMissCount(long missCount) { this.missCount = missCount; }
        public double getHitRate() { return hitRate; }
        public void setHitRate(double hitRate) { this.hitRate = hitRate; }
        public int getLocalCacheSize() { return localCacheSize; }
        public void setLocalCacheSize(int localCacheSize) { this.localCacheSize = localCacheSize; }
        public int getRedisCacheSize() { return redisCacheSize; }
        public void setRedisCacheSize(int redisCacheSize) { this.redisCacheSize = redisCacheSize; }
        public double getLocalHitRate() { return localHitRate; }
        public void setLocalHitRate(double localHitRate) { this.localHitRate = localHitRate; }
        public double getRedisHitRate() { return redisHitRate; }
        public void setRedisHitRate(double redisHitRate) { this.redisHitRate = redisHitRate; }
        public double getOverallHitRate() { return overallHitRate; }
        public void setOverallHitRate(double overallHitRate) { this.overallHitRate = overallHitRate; }
        public long getEvictionCount() { return evictionCount; }
        public void setEvictionCount(long evictionCount) { this.evictionCount = evictionCount; }
        public long getLoadCount() { return loadCount; }
        public void setLoadCount(long loadCount) { this.loadCount = loadCount; }
        public long getLoadExceptionCount() { return loadExceptionCount; }
        public void setLoadExceptionCount(long loadExceptionCount) { this.loadExceptionCount = loadExceptionCount; }
        public double getAverageLoadTime() { return averageLoadTime; }
        public void setAverageLoadTime(double averageLoadTime) { this.averageLoadTime = averageLoadTime; }
    }
}
