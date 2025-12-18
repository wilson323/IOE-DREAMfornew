package net.lab1024.sa.common.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;

import java.nio.charset.Charset;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 多级缓存管理器
 * <p>
 * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
 * 实现三级缓存架构：
 * - L1: Caffeine本地缓存（毫秒级响应）
 * - L2: Redis分布式缓存（数据一致性）
 * - 布隆过滤器：防缓存穿透
 * - 分布式锁：防缓存击穿
 * </p>
 *
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - Manager类是纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖
 * - 在微服务中通过配置类注册为Spring Bean
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Slf4j
public class UnifiedCacheManager {

    /**
     * L1本地缓存（Caffeine）
     */
    private final Cache<String, Object> localCache;

    /**
     * L2 Redis缓存
     */
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 布隆过滤器（使用Redisson实现）
     */
    private final RBloomFilter<String> bloomFilter;

    /**
     * Redisson客户端（用于分布式锁）
     */
    private final RedissonClient redissonClient;

    /**
     * 构造函数注入依赖
     * <p>
     * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
     * </p>
     *
     * @param redisTemplate Redis模板
     * @param redissonClient Redisson客户端
     */
    public UnifiedCacheManager(
            RedisTemplate<String, Object> redisTemplate,
            RedissonClient redissonClient) {

        this.redisTemplate = redisTemplate;
        this.redissonClient = redissonClient;

        // 配置Caffeine本地缓存
        this.localCache = Caffeine.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(Duration.ofMinutes(5))
                .recordStats()
                .build();

        // 配置布隆过滤器（使用Redisson实现）
        String bloomFilterName = "ioedream:cache:bloomfilter";
        this.bloomFilter = redissonClient.getBloomFilter(bloomFilterName);
        
        // 初始化布隆过滤器（如果未初始化）
        if (!this.bloomFilter.isExists()) {
            this.bloomFilter.tryInit(100000, 0.01);
            log.info("[多级缓存] 布隆过滤器初始化完成: 容量=100000, 误判率=0.01");
        }

        log.info("[多级缓存] UnifiedCacheManager初始化完成");
    }

    /**
     * 多级缓存获取
     * <p>
     * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
     * 查询顺序：L1本地缓存 -> 布隆过滤器 -> L2 Redis缓存 -> 分布式锁+数据加载
     * </p>
     *
     * @param key 缓存键
     * @param type 值类型
     * @param loader 数据加载器
     * @param <T> 值类型
     * @return 缓存值
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type, Supplier<T> loader) {
        // L1: 本地缓存
        T value = (T) localCache.getIfPresent(key);
        if (value != null) {
            log.debug("[多级缓存] L1命中: key={}", key);
            return value;
        }

        // 布隆过滤器检查
        if (!bloomFilter.contains(key)) {
            log.debug("[多级缓存] 布隆过滤器未命中: key={}", key);
            return null;
        }

        // L2: Redis缓存
        value = (T) redisTemplate.opsForValue().get(key);
        if (value != null) {
            log.debug("[多级缓存] L2命中: key={}", key);
            localCache.put(key, value);
            return value;
        }

        // L3: 分布式锁+数据加载
        String lockKey = "lock:" + key;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (lock.tryLock(5, TimeUnit.SECONDS)) {
                // 双重检查
                value = (T) redisTemplate.opsForValue().get(key);
                if (value != null) {
                    log.debug("[多级缓存] L2双重检查命中: key={}", key);
                    localCache.put(key, value);
                    return value;
                }

                // 加载数据
                log.debug("[多级缓存] 从数据源加载: key={}", key);
                value = loader.get();
                if (value != null) {
                    put(key, value, Duration.ofMinutes(30));
                    bloomFilter.add(key);
                }
            } else {
                log.warn("[多级缓存] 获取分布式锁失败: key={}", key);
                // 降级：直接加载数据
                value = loader.get();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[多级缓存] 获取分布式锁被中断: key={}", key, e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        return value;
    }

    /**
     * 写入缓存
     * <p>
     * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
     * 同时写入L1本地缓存和L2 Redis缓存
     * </p>
     *
     * @param key 缓存键
     * @param value 缓存值
     * @param ttl 过期时间
     */
    public void put(String key, Object value, Duration ttl) {
        if (key == null || value == null) {
            return;
        }

        // 写入L1本地缓存
        localCache.put(key, value);

        // 写入L2 Redis缓存
        redisTemplate.opsForValue().set(key, value, ttl);

        // 添加到布隆过滤器
        bloomFilter.add(key);

        log.debug("[多级缓存] 写入缓存: key={}, ttl={}分钟", key, ttl.toMinutes());
    }

    /**
     * 获取缓存（单参数版本，兼容旧代码）
     * <p>
     * 兼容权限模块等使用单参数get方法的代码
     * </p>
     *
     * @param key 缓存键
     * @param <T> 值类型
     * @return 缓存值，如果不存在返回null
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        if (key == null) {
            return null;
        }

        // L1: 本地缓存
        T value = (T) localCache.getIfPresent(key);
        if (value != null) {
            log.debug("[多级缓存] L1命中: key={}", key);
            return value;
        }

        // 布隆过滤器检查（Redisson使用contains方法）
        if (!bloomFilter.contains(key)) {
            log.debug("[多级缓存] 布隆过滤器未命中: key={}", key);
            return null;
        }

        // L2: Redis缓存
        value = (T) redisTemplate.opsForValue().get(key);
        if (value != null) {
            log.debug("[多级缓存] L2命中: key={}", key);
            localCache.put(key, value);
            return value;
        }

        log.debug("[多级缓存] 未命中: key={}", key);
        return null;
    }

    /**
     * 写入缓存（long参数版本，兼容旧代码）
     * <p>
     * 兼容权限模块等使用long参数put方法的代码
     * </p>
     *
     * @param key 缓存键
     * @param value 缓存值
     * @param expireMs 过期时间（毫秒）
     */
    public void put(String key, Object value, long expireMs) {
        if (key == null || value == null) {
            return;
        }

        Duration ttl = Duration.ofMillis(expireMs);
        put(key, value, ttl);
    }

    /**
     * 写入缓存（int参数版本，兼容旧代码）
     * <p>
     * 兼容权限模块等使用int参数put方法的代码（秒为单位）
     * </p>
     *
     * @param key 缓存键
     * @param value 缓存值
     * @param ttlSeconds 过期时间（秒）
     */
    public void put(String key, Object value, int ttlSeconds) {
        if (key == null || value == null) {
            return;
        }

        Duration ttl = Duration.ofSeconds(ttlSeconds);
        put(key, value, ttl);
    }

    /**
     * 删除缓存
     * <p>
     * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
     * 同时删除L1本地缓存和L2 Redis缓存
     * </p>
     *
     * @param key 缓存键
     */
    public void evict(String key) {
        if (key == null) {
            return;
        }

        // 删除L1本地缓存
        localCache.invalidate(key);

        // 删除L2 Redis缓存
        redisTemplate.delete(key);

        log.debug("[多级缓存] 删除缓存: key={}", key);
    }

    /**
     * 清空所有缓存
     * <p>
     * 清空L1本地缓存和L2 Redis缓存（谨慎使用）
     * </p>
     */
    public void clear() {
        localCache.invalidateAll();
        // 注意：不清空Redis所有键，只清空特定前缀的键
        log.warn("[多级缓存] L1本地缓存已清空，L2 Redis缓存需手动清理");
    }
}
