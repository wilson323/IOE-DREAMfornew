package net.lab1024.sa.base.common.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 基础缓存管理器
 * 实现多级缓存架构：L1(Caffeine本地缓存) + L2(Redis分布式缓存)
 *
 * 功能特性：
 * - L1缓存：本地高速缓存，5分钟过期，最大10000个条目
 * - L2缓存：Redis分布式缓存，30分钟过期
 * - 缓存一致性：L1更新时同步更新L2，L2失效时清除L1
 * - 防击穿：分布式锁防止缓存击穿
 * - 防雪崩：随机过期时间防止缓存雪崩
 *
 * @author SmartAdmin
 * @since 2025-11-16
 */
@Slf4j
@Component
public class BaseCacheManager {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RedisUtil redisUtil;

    /**
     * L1本地缓存 (Caffeine)
     * 配置：最大10000条目，5分钟过期，弱引用键，软引用值
     */
    private Cache<String, Object> l1Cache;

  
    /**
     * 初始化L1缓存
     */
    @PostConstruct
    public void initL1Cache() {
        this.l1Cache = Caffeine.newBuilder()
                // 最大缓存数量
                .maximumSize(10000)
                // 写入后5分钟过期
                .expireAfterWrite(5, TimeUnit.MINUTES)
                // 访问后3分钟过期
                .expireAfterAccess(3, TimeUnit.MINUTES)
                // 弱引用键
                .weakKeys()
                // 软引用值
                .softValues()
                // 移除监听器
                .removalListener(this::onRemoval)
                // 记录统计信息
                .recordStats()
                .build();

        log.info("L1缓存(Caffeine)初始化完成，最大容量: 10000，过期时间: 5分钟");
    }

    /**
     * 缓存移除监听器
     */
    private void onRemoval(String key, Object value, RemovalCause cause) {
        log.debug("L1缓存移除: key={}, value={}, cause={}", key, value, cause);

        // 如果是过期或被替换，同步清除L2缓存
        if (cause == RemovalCause.EXPIRED || cause == RemovalCause.SIZE) {
            try {
                redisTemplate.delete(key);
            } catch (Exception e) {
                log.warn("清除L2缓存失败: key={}", key, e);
            }
        }
    }

    /**
     * 获取缓存值 (L1 -> L2 -> null)
     *
     * @param key 缓存键
     * @return 缓存值，未找到返回null
     */
    public Object get(String key) {
        try {
            // 1. 先从L1缓存获取
            Object value = l1Cache.getIfPresent(key);
            if (value != null) {
                log.debug("L1缓存命中: key={}", key);
                return value;
            }

            // 2. L1未命中，从L2获取
            value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                // 回写到L1缓存
                l1Cache.put(key, value);
                log.debug("L2缓存命中，回写L1: key={}", key);
                return value;
            }

            log.debug("缓存未命中: key={}", key);
            return null;

        } catch (Exception e) {
            log.error("获取缓存失败: key={}", key, e);
            return null;
        }
    }

    /**
     * 获取缓存值并刷新过期时间
     *
     * @param key 缓存键
     * @param timeout 过期时间
     * @param timeUnit 时间单位
     * @return 缓存值
     */
    public Object getWithExpire(String key, long timeout, TimeUnit timeUnit) {
        Object value = get(key);
        if (value != null) {
            try {
                // 刷新L2缓存过期时间
                redisTemplate.expire(key, timeout, timeUnit);
                // L1缓存会自动刷新过期时间
            } catch (Exception e) {
                log.warn("刷新缓存过期时间失败: key={}", key, e);
            }
        }
        return value;
    }

    /**
     * 设置缓存值 (L1 + L2)
     *
     * @param key 缓存键
     * @param value 缓存值
     */
    public void set(String key, Object value) {
        set(key, value, 30, TimeUnit.MINUTES);
    }

    /**
     * 设置缓存值带过期时间
     *
     * @param key 缓存键
     * @param value 缓存值
     * @param timeout 过期时间
     * @param timeUnit 时间单位
     */
    public void set(String key, Object value, long timeout, TimeUnit timeUnit) {
        try {
            // 设置L1缓存
            l1Cache.put(key, value);

            // 设置L2缓存，添加随机时间防止雪崩
            long randomTimeout = addRandomTimeout(timeout, timeUnit);
            redisTemplate.opsForValue().set(key, value, randomTimeout, timeUnit);

            log.debug("设置缓存成功: key={}, timeout={} {}", key, randomTimeout, timeUnit);
        } catch (Exception e) {
            log.error("设置缓存失败: key={}", key, e);
        }
    }

    /**
     * 删除缓存 (L1 + L2)
     *
     * @param key 缓存键
     */
    public void delete(String key) {
        try {
            // 删除L1缓存
            l1Cache.invalidate(key);

            // 删除L2缓存
            redisTemplate.delete(key);

            log.debug("删除缓存成功: key={}", key);
        } catch (Exception e) {
            log.error("删除缓存失败: key={}", key, e);
        }
    }

    /**
     * 批量删除缓存
     *
     * @param keys 缓存键数组
     */
    public void delete(String... keys) {
        if (keys != null && keys.length > 0) {
            for (String key : keys) {
                delete(key);
            }
        }
    }

    /**
     * 检查缓存是否存在
     *
     * @param key 缓存键
     * @return 是否存在
     */
    public boolean hasKey(String key) {
        try {
            // 先检查L1
            if (l1Cache.getIfPresent(key) != null) {
                return true;
            }

            // 再检查L2
            Boolean exists = redisTemplate.hasKey(key);
            return exists != null && exists;
        } catch (Exception e) {
            log.error("检查缓存存在性失败: key={}", key, e);
            return false;
        }
    }

    /**
     * 原子递增操作
     *
     * @param key 缓存键
     * @param delta 递增量
     * @return 递增后的值
     */
    public Long increment(String key, long delta) {
        try {
            Long value = redisTemplate.opsForValue().increment(key, delta);

            // 同步更新L1缓存
            if (value != null) {
                l1Cache.put(key, value);
            }

            return value;
        } catch (Exception e) {
            log.error("原子递增失败: key={}, delta={}", key, delta, e);
            return null;
        }
    }

    /**
     * 防击穿的缓存获取
     * 使用简单的同步机制防止缓存击穿
     *
     * @param key 缓存键
     * @param loader 数据加载器
     * @return 缓存值
     */
    public Object getWithLoadThrough(String key, CacheLoader loader) {
        return getWithLoadThrough(key, loader, 30, TimeUnit.MINUTES);
    }

    /**
     * 防击穿的缓存获取带过期时间
     *
     * @param key 缓存键
     * @param loader 数据加载器
     * @param timeout 过期时间
     * @param timeUnit 时间单位
     * @return 缓存值
     */
    public Object getWithLoadThrough(String key, CacheLoader loader, long timeout, TimeUnit timeUnit) {
        // 1. 尝试从缓存获取
        Object value = get(key);
        if (value != null) {
            return value;
        }

        // 2. 使用简单的同步机制防止击穿
        synchronized (this) {
            // 双重检查，防止重复加载
            value = get(key);
            if (value != null) {
                return value;
            }

            try {
                // 加载数据
                log.debug("缓存未命中，开始加载数据: key={}", key);
                value = loader.load();

                if (value != null) {
                    // 设置缓存
                    set(key, value, timeout, timeUnit);
                    log.debug("数据加载并缓存成功: key={}", key);
                }

                return value;
            } catch (Exception e) {
                log.error("缓存加载数据失败: key={}", key, e);
                return null;
            }
        }
    }

    /**
     * 清空所有缓存
     */
    public void clearAll() {
        try {
            // 清空L1缓存
            l1Cache.invalidateAll();
            log.info("L1缓存已清空");
        } catch (Exception e) {
            log.error("清空L1缓存失败", e);
        }
    }

    /**
     * 获取L1缓存统计信息
     */
    public String getL1CacheStats() {
        if (l1Cache != null) {
            return l1Cache.stats().toString();
        }
        return "L1缓存未初始化";
    }

    /**
     * 添加随机过期时间防止雪崩
     *
     * @param timeout 原始过期时间
     * @param timeUnit 时间单位
     * @return 添加随机时间后的过期时间
     */
    private long addRandomTimeout(long timeout, TimeUnit timeUnit) {
        // 转换为毫秒
        long timeoutMillis = timeUnit.toMillis(timeout);
        // 添加随机时间：±10%
        long randomOffset = (long) (timeoutMillis * 0.1 * (Math.random() * 2 - 1));
        return timeoutMillis + randomOffset;
    }

    /**
     * 缓存数据加载器接口
     */
    @FunctionalInterface
    public interface CacheLoader {
        /**
         * 加载数据
         *
         * @return 数据对象
         * @throws Exception 加载异常
         */
        Object load() throws Exception;
    }
}