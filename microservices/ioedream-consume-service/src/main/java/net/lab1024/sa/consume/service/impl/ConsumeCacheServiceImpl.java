package net.lab1024.sa.consume.service.impl;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.cache.CacheService;
import net.lab1024.sa.consume.service.ConsumeCacheService;

/**
 * 消费缓存服务实现类
 * <p>
 * 实现消费相关的缓存操作
 * 严格遵循CLAUDE.md规范：
 * - Service实现类在ioedream-consume-service中
 * - 使用@Resource注入依赖
 * - 封装CacheService提供消费专用缓存操作
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
public class ConsumeCacheServiceImpl implements ConsumeCacheService {

    @Resource
    private CacheService cacheService;

    /**
     * 获取缓存值
     *
     * @param key 缓存键
     * @param clazz 数据类型
     * @param <T> 数据类型
     * @return 缓存值
     */
    @Override
    public <T> T get(String key, Class<T> clazz) {
        try {
            return cacheService.get(key, clazz);
        } catch (Exception e) {
            log.error("[消费缓存] 获取缓存失败，key：{}", key, e);
            return null;
        }
    }

    /**
     * 设置缓存值
     *
     * @param key 缓存键
     * @param value 缓存值
     */
    @Override
    public void set(String key, Object value) {
        try {
            cacheService.set(key, value);
        } catch (Exception e) {
            log.error("[消费缓存] 设置缓存失败，key：{}", key, e);
        }
    }

    /**
     * 设置缓存值（指定过期时间，单位：秒）
     *
     * @param key 缓存键
     * @param value 缓存值
     * @param timeoutSeconds 过期时间（秒）
     */
    @Override
    public void set(String key, Object value, int timeoutSeconds) {
        try {
            cacheService.set(key, value, timeoutSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("[消费缓存] 设置缓存失败，key：{}，timeoutSeconds：{}", key, timeoutSeconds, e);
        }
    }

    /**
     * 删除缓存
     *
     * @param key 缓存键
     */
    @Override
    public void delete(String key) {
        try {
            cacheService.delete(key);
        } catch (Exception e) {
            log.error("[消费缓存] 删除缓存失败，key：{}", key, e);
        }
    }
}
