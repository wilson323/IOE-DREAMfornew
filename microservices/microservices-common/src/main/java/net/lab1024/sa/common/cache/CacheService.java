package net.lab1024.sa.common.cache;

import java.util.concurrent.TimeUnit;

/**
 * 缓存服务接口
 * <p>
 * 提供统一的缓存操作接口，封装UnifiedCacheManager
 * 严格遵循CLAUDE.md规范：
 * - 接口在microservices-common中
 * - 提供统一的缓存操作接口
 * - 支持多级缓存策略
 * </p>
 * <p>
 * 业务场景：
 * - 数据缓存
 * - 热点数据缓存
 * - 分布式缓存
 * - 缓存失效管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface CacheService {

    /**
     * 获取缓存值
     *
     * @param key 缓存键
     * @param clazz 数据类型
     * @param <T> 数据类型
     * @return 缓存值，如果不存在返回null
     */
    <T> T get(String key, Class<T> clazz);

    /**
     * 设置缓存值（使用默认过期时间）
     *
     * @param key 缓存键
     * @param value 缓存值
     */
    void set(String key, Object value);

    /**
     * 设置缓存值（指定过期时间）
     *
     * @param key 缓存键
     * @param value 缓存值
     * @param timeout 过期时间
     * @param unit 时间单位
     */
    void set(String key, Object value, long timeout, TimeUnit unit);

    /**
     * 删除缓存
     *
     * @param key 缓存键
     * @return 是否成功
     */
    Boolean delete(String key);

    /**
     * 判断缓存是否存在
     *
     * @param key 缓存键
     * @return 是否存在
     */
    Boolean hasKey(String key);

    /**
     * 设置过期时间
     *
     * @param key 缓存键
     * @param timeout 过期时间
     * @param unit 时间单位
     * @return 是否成功
     */
    Boolean expire(String key, long timeout, TimeUnit unit);

    /**
     * 递增
     *
     * @param key 缓存键
     * @return 递增后的值
     */
    Long increment(String key);

    /**
     * 递增（指定增量）
     *
     * @param key 缓存键
     * @param delta 增量
     * @return 递增后的值
     */
    Long increment(String key, long delta);

    /**
     * 递减
     *
     * @param key 缓存键
     * @return 递减后的值
     */
    Long decrement(String key);
}
