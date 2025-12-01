package net.lab1024.sa.admin.module.consume.manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import jakarta.annotation.Resource;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

/**
 * Redisson管理器
 * 严格遵循repowiki规范：核心管理器，负责Redisson客户端的管理和操作
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
@Slf4j
@Component
public class RedissonManager {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 尝试获取锁
     *
     * @param lockKey 锁的key
     * @param waitTime 等待时间
     * @param leaseTime 锁的持有时间
     * @param unit 时间单位
     * @return 是否获取成功
     */
    public boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit) {
        if (lockKey == null || lockKey.trim().isEmpty()) {
            return false;
        }

        try {
            RLock lock = redissonClient.getLock(lockKey);
            return lock.tryLock(waitTime, leaseTime, unit);
        } catch (Exception e) {
            log.error("获取Redisson锁异常: key={}", lockKey, e);
            return false;
        }
    }

    /**
     * 释放锁
     *
     * @param lockKey 锁的key
     */
    public void unlock(String lockKey) {
        if (lockKey == null || lockKey.trim().isEmpty()) {
            return;
        }

        try {
            RLock lock = redissonClient.getLock(lockKey);
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("释放Redisson锁: key={}", lockKey);
            } else {
                log.warn("尝试释放非当前线程持有的锁: key={}", lockKey);
            }
        } catch (Exception e) {
            log.error("释放Redisson锁异常: key={}", lockKey, e);
        }
    }

    /**
     * 强制释放锁（危险操作）
     *
     * @param lockKey 锁的key
     */
    public void forceUnlock(String lockKey) {
        if (lockKey == null || lockKey.trim().isEmpty()) {
            return;
        }

        try {
            RLock lock = redissonClient.getLock(lockKey);
            lock.forceUnlock();
            log.warn("强制释放Redisson锁: key={}", lockKey);
        } catch (Exception e) {
            log.error("强制释放Redisson锁异常: key={}", lockKey, e);
        }
    }

    /**
     * 检查锁是否存在
     *
     * @param lockKey 锁的key
     * @return 锁是否存在
     */
    public boolean isLocked(String lockKey) {
        if (lockKey == null || lockKey.trim().isEmpty()) {
            return false;
        }

        try {
            RLock lock = redissonClient.getLock(lockKey);
            return lock.isLocked();
        } catch (Exception e) {
            log.error("检查Redisson锁状态异常: key={}", lockKey, e);
            return false;
        }
    }

    /**
     * 检查锁是否被当前线程持有
     *
     * @param lockKey 锁的key
     * @return 是否被当前线程持有
     */
    public boolean isHeldByCurrentThread(String lockKey) {
        if (lockKey == null || lockKey.trim().isEmpty()) {
            return false;
        }

        try {
            RLock lock = redissonClient.getLock(lockKey);
            return lock.isHeldByCurrentThread();
        } catch (Exception e) {
            log.error("检查Redisson锁持有状态异常: key={}", lockKey, e);
            return false;
        }
    }

    /**
     * 获取锁的剩余持有时间
     *
     * @param lockKey 锁的key
     * @return 剩余持有时间（毫秒），如果锁不存在返回-1
     */
    public long getRemainingTime(String lockKey) {
        if (lockKey == null || lockKey.trim().isEmpty()) {
            return -1;
        }

        try {
            RLock lock = redissonClient.getLock(lockKey);
            return lock.remainTimeToLive();
        } catch (Exception e) {
            log.error("获取Redisson锁剩余时间异常: key={}", lockKey, e);
            return -1;
        }
    }

    /**
     * 设置键值对
     *
     * @param key 键
     * @param value 值
     */
    public void set(String key, Object value) {
        if (key == null || key.trim().isEmpty()) {
            return;
        }

        try {
            redissonClient.getBucket(key).set(value);
        } catch (Exception e) {
            log.error("设置Redisson键值对异常: key={}", key, e);
        }
    }

    /**
     * 设置键值对并指定过期时间
     *
     * @param key 键
     * @param value 值
     * @param ttl 过期时间
     * @param unit 时间单位
     */
    public void setex(String key, Object value, long ttl, TimeUnit unit) {
        if (key == null || key.trim().isEmpty()) {
            return;
        }

        try {
            redissonClient.getBucket(key).set(value, ttl, unit);
        } catch (Exception e) {
            log.error("设置Redisson键值对异常: key={}, ttl={}", key, ttl, e);
        }
    }

    /**
     * 获取值
     *
     * @param key 键
     * @param <T> 值的类型
     * @return 值
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        if (key == null || key.trim().isEmpty()) {
            return null;
        }

        try {
            return (T) redissonClient.getBucket(key).get();
        } catch (Exception e) {
            log.error("获取Redisson值异常: key={}", key, e);
            return null;
        }
    }

    /**
     * 检查键是否存在
     *
     * @param key 键
     * @return 是否存在
     */
    public boolean exists(String key) {
        if (key == null || key.trim().isEmpty()) {
            return false;
        }

        try {
            return redissonClient.getBucket(key).isExists();
        } catch (Exception e) {
            log.error("检查Redisson键存在性异常: key={}", key, e);
            return false;
        }
    }

    /**
     * 删除键
     *
     * @param key 键
     * @return 是否删除成功
     */
    public boolean delete(String key) {
        if (key == null || key.trim().isEmpty()) {
            return false;
        }

        try {
            return redissonClient.getBucket(key).delete();
        } catch (Exception e) {
            log.error("删除Redisson键异常: key={}", key, e);
            return false;
        }
    }

    /**
     * 获取Redisson客户端
     *
     * @return RedissonClient
     */
    public RedissonClient getRedissonClient() {
        return redissonClient;
    }

    /**
     * 检查管理器健康状态
     *
     * @return 是否健康
     */
    public boolean isHealthy() {
        try {
            // 简单的健康检查：尝试设置和获取一个测试键
            String testKey = "health-check:" + System.currentTimeMillis();
            set(testKey, "test");
            String value = get(testKey, String.class);
            delete(testKey);

            return "test".equals(value);
        } catch (Exception e) {
            log.error("Redisson管理器健康检查异常", e);
            return false;
        }
    }
}