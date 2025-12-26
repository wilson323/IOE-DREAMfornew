package net.lab1024.sa.common.cache;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 缓存服务接口
 * <p>
 * 提供统一的缓存操作接口，支持多级缓存（L1本地缓存 + L2 Redis缓存）
 * 严格遵循CLAUDE.md规范：纯Java接口，无框架依赖
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-21
 */
public interface CacheService {

    /**
     * 获取缓存值
     *
     * @param key 缓存键
     * @param valueClass 值类型
     * @param <T> 值类型泛型
     * @return 缓存值，不存在返回null
     */
    <T> T get(String key, Class<T> valueClass);

    /**
     * 获取缓存值（带默认值）
     *
     * @param key 缓存键
     * @param valueClass 值类型
     * @param defaultValue 默认值
     * @param <T> 值类型泛型
     * @return 缓存值，不存在返回默认值
     */
    <T> T get(String key, Class<T> valueClass, T defaultValue);

    /**
     * 设置缓存值
     *
     * @param key 缓存键
     * @param value 缓存值
     */
    void put(String key, Object value);

    /**
     * 设置缓存值（带过期时间）
     *
     * @param key 缓存键
     * @param value 缓存值
     * @param timeout 过期时间
     * @param timeUnit 时间单位
     */
    void put(String key, Object value, long timeout, TimeUnit timeUnit);

    /**
     * 删除缓存键
     *
     * @param key 缓存键
     * @return 是否删除成功
     */
    boolean evict(String key);

    /**
     * 判断缓存键是否存在
     *
     * @param key 缓存键
     * @return 是否存在
     */
    boolean exists(String key);

    /**
     * 清空所有缓存
     */
    void clear();

    /**
     * 获取缓存大小
     *
     * @return 缓存大小
     */
    long size();
}

