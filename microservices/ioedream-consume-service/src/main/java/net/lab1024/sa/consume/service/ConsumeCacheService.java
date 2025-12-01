package net.lab1024.sa.consume.service;

/**
 * 消费缓存服务接口
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
public interface ConsumeCacheService {

    /**
     * 获取缓存值
     *
     * @param key 缓存键
     * @return 缓存值，如果不存在返回null
     */
    Object getCachedValue(String key);

    /**
     * 设置缓存值
     *
     * @param key   缓存键
     * @param value 缓存值
     * @param ttl   过期时间（秒）
     */
    void setCachedValue(String key, Object value, int ttl);
}
