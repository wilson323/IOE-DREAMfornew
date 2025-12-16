package net.lab1024.sa.consume.service;

/**
 * 消费缓存服务接口
 * <p>
 * 提供消费相关的缓存操作
 * 严格遵循CLAUDE.md规范：
 * - Service接口在ioedream-consume-service中
 * - 提供统一的缓存操作接口
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface ConsumeCacheService {

    /**
     * 获取缓存值
     *
     * @param key 缓存键
     * @param clazz 数据类型
     * @param <T> 数据类型
     * @return 缓存值
     */
    <T> T get(String key, Class<T> clazz);

    /**
     * 设置缓存值
     *
     * @param key 缓存键
     * @param value 缓存值
     */
    void set(String key, Object value);

    /**
     * 设置缓存值（指定过期时间，单位：秒）
     *
     * @param key 缓存键
     * @param value 缓存值
     * @param timeoutSeconds 过期时间（秒）
     */
    void set(String key, Object value, int timeoutSeconds);

    /**
     * 删除缓存
     *
     * @param key 缓存键
     */
    void delete(String key);
}



