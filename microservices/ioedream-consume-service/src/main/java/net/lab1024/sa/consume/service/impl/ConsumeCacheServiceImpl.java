package net.lab1024.sa.consume.service.impl;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.cache.CacheService;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.exception.SystemException;
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
@Transactional(readOnly = true)
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
    @Observed(name = "consume.cache.get", contextualName = "consume-cache-get")
    public <T> T get(String key, Class<T> clazz) {
        try {
            // 参数验证
            if (key == null || key.trim().isEmpty()) {
                log.warn("[消费缓存] 缓存键不能为空");
                return null;
            }
            if (clazz == null) {
                log.warn("[消费缓存] 数据类型不能为空");
                return null;
            }

            return cacheService.get(key, clazz);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[消费缓存] 获取缓存参数错误: key={}, error={}", key, e.getMessage());
            return null; // For cache operations, return null on parameter error
        } catch (BusinessException e) {
            log.warn("[消费缓存] 获取缓存业务异常: key={}, code={}, message={}", key, e.getCode(), e.getMessage());
            return null; // For cache operations, return null on business error
        } catch (SystemException e) {
            log.error("[消费缓存] 获取缓存系统异常: key={}, code={}, message={}", key, e.getCode(), e.getMessage(), e);
            return null; // For cache operations, return null on system error
        } catch (Exception e) {
            log.error("[消费缓存] 获取缓存未知异常: key={}", key, e);
            return null; // For cache operations, return null on unknown error
        }
    }

    /**
     * 设置缓存值
     *
     * @param key 缓存键
     * @param value 缓存值
     */
    @Override
    @Observed(name = "consume.cache.set", contextualName = "consume-cache-set")
    public void set(String key, Object value) {
        try {
            // 参数验证
            if (key == null || key.trim().isEmpty()) {
                log.warn("[消费缓存] 缓存键不能为空");
                return;
            }

            cacheService.set(key, value);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[消费缓存] 设置缓存参数错误: key={}, error={}", key, e.getMessage());
            // For cache operations, silently fail on parameter error
        } catch (BusinessException e) {
            log.warn("[消费缓存] 设置缓存业务异常: key={}, code={}, message={}", key, e.getCode(), e.getMessage());
            // For cache operations, silently fail on business error
        } catch (SystemException e) {
            log.error("[消费缓存] 设置缓存系统异常: key={}, code={}, message={}", key, e.getCode(), e.getMessage(), e);
            // For cache operations, silently fail on system error
        } catch (Exception e) {
            log.error("[消费缓存] 设置缓存未知异常: key={}", key, e);
            // For cache operations, silently fail on unknown error
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
    @Observed(name = "consume.cache.setWithTimeout", contextualName = "consume-cache-set-timeout")
    public void set(String key, Object value, int timeoutSeconds) {
        try {
            // 参数验证
            if (key == null || key.trim().isEmpty()) {
                log.warn("[消费缓存] 缓存键不能为空");
                return;
            }
            if (timeoutSeconds < 0) {
                log.warn("[消费缓存] 过期时间不能为负数，timeoutSeconds={}", timeoutSeconds);
                return;
            }

            cacheService.set(key, value, timeoutSeconds, TimeUnit.SECONDS);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[消费缓存] 设置缓存参数错误: key={}, timeoutSeconds={}, error={}", key, timeoutSeconds, e.getMessage());
            // For cache operations, silently fail on parameter error
        } catch (BusinessException e) {
            log.warn("[消费缓存] 设置缓存业务异常: key={}, timeoutSeconds={}, code={}, message={}", key, timeoutSeconds, e.getCode(), e.getMessage());
            // For cache operations, silently fail on business error
        } catch (SystemException e) {
            log.error("[消费缓存] 设置缓存系统异常: key={}, timeoutSeconds={}, code={}, message={}", key, timeoutSeconds, e.getCode(), e.getMessage(), e);
            // For cache operations, silently fail on system error
        } catch (Exception e) {
            log.error("[消费缓存] 设置缓存未知异常: key={}, timeoutSeconds={}", key, timeoutSeconds, e);
            // For cache operations, silently fail on unknown error
        }
    }

    /**
     * 删除缓存
     *
     * @param key 缓存键
     */
    @Override
    @Observed(name = "consume.cache.delete", contextualName = "consume-cache-delete")
    public void delete(String key) {
        try {
            // 参数验证
            if (key == null || key.trim().isEmpty()) {
                log.warn("[消费缓存] 缓存键不能为空");
                return;
            }

            cacheService.delete(key);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[消费缓存] 删除缓存参数错误: key={}, error={}", key, e.getMessage());
            // For cache operations, silently fail on parameter error
        } catch (BusinessException e) {
            log.warn("[消费缓存] 删除缓存业务异常: key={}, code={}, message={}", key, e.getCode(), e.getMessage());
            // For cache operations, silently fail on business error
        } catch (SystemException e) {
            log.error("[消费缓存] 删除缓存系统异常: key={}, code={}, message={}", key, e.getCode(), e.getMessage(), e);
            // For cache operations, silently fail on system error
        } catch (Exception e) {
            log.error("[消费缓存] 删除缓存未知异常: key={}", key, e);
            // For cache operations, silently fail on unknown error
        }
    }
}



