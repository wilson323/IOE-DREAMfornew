package net.lab1024.sa.admin.module.consume.manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import jakarta.annotation.Resource;

import net.lab1024.sa.base.common.constant.RedisKeyConst;
import net.lab1024.sa.base.common.exception.SmartException;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 分布式锁管理器
 * 严格遵循repowiki规范：核心管理器，负责分布式锁的获取和释放
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
@Slf4j
@Component
public class DistributedLockManager {

    @Resource
    private RedissonManager redissonManager;

    /**
     * 使用分布式锁执行业务逻辑
     *
     * @param lockKey 锁的key
     * @param waitTime 等待获取锁的时间
     * @param unit 时间单位
     * @param supplier 业务逻辑提供者
     * @param <T> 返回值类型
     * @return 业务逻辑执行结果
     */
    public <T> T executeWithLock(String lockKey, long waitTime, TimeUnit unit, Supplier<T> supplier) {
        if (lockKey == null || lockKey.trim().isEmpty()) {
            throw new SmartException("锁的key不能为空");
        }

        String fullLockKey = RedisKeyConst.Consume.CONSUME_LOCK_PREFIX + lockKey;

        try {
            log.debug("尝试获取分布式锁: key={}", fullLockKey);

            boolean acquired = redissonManager.tryLock(fullLockKey, waitTime, 30, unit);

            if (!acquired) {
                log.warn("获取分布式锁失败，系统繁忙: key={}", fullLockKey);
                throw new SmartException("系统繁忙，请稍后重试");
            }

            log.debug("成功获取分布式锁: key={}", fullLockKey);

            try {
                // 执行业务逻辑
                T result = supplier.get();
                log.debug("业务逻辑执行成功: key={}", fullLockKey);
                return result;

            } finally {
                // 释放锁
                redissonManager.unlock(fullLockKey);
                log.debug("释放分布式锁: key={}", fullLockKey);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取分布式锁被中断: key={}", fullLockKey, e);
            throw new SmartException("操作被中断");
        } catch (Exception e) {
            log.error("分布式锁执行异常: key={}", fullLockKey, e);
            throw new SmartException("分布式锁执行异常: " + e.getMessage());
        }
    }

    /**
     * 尝试获取分布式锁
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

        String fullLockKey = RedisKeyConst.Consume.CONSUME_LOCK_PREFIX + lockKey;

        try {
            return redissonManager.tryLock(fullLockKey, waitTime, leaseTime, unit);
        } catch (Exception e) {
            log.error("尝试获取分布式锁异常: key={}", fullLockKey, e);
            return false;
        }
    }

    /**
     * 释放分布式锁
     *
     * @param lockKey 锁的key
     */
    public void unlock(String lockKey) {
        if (lockKey == null || lockKey.trim().isEmpty()) {
            return;
        }

        String fullLockKey = RedisKeyConst.Consume.CONSUME_LOCK_PREFIX + lockKey;

        try {
            redissonManager.unlock(fullLockKey);
            log.debug("释放分布式锁: key={}", fullLockKey);
        } catch (Exception e) {
            log.error("释放分布式锁异常: key={}", fullLockKey, e);
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

        String fullLockKey = RedisKeyConst.Consume.CONSUME_LOCK_PREFIX + lockKey;

        try {
            return redissonManager.isLocked(fullLockKey);
        } catch (Exception e) {
            log.error("检查锁状态异常: key={}", fullLockKey, e);
            return false;
        }
    }

    /**
     * 强制释放锁（危险操作，仅用于紧急情况）
     *
     * @param lockKey 锁的key
     */
    public void forceUnlock(String lockKey) {
        if (lockKey == null || lockKey.trim().isEmpty()) {
            return;
        }

        String fullLockKey = RedisKeyConst.Consume.CONSUME_LOCK_PREFIX + lockKey;

        try {
            redissonManager.forceUnlock(fullLockKey);
            log.warn("强制释放分布式锁: key={}", fullLockKey);
        } catch (Exception e) {
            log.error("强制释放分布式锁异常: key={}", fullLockKey, e);
        }
    }

    /**
     * 检查管理器健康状态
     *
     * @return 是否健康
     */
    public boolean isHealthy() {
        try {
            // 简单的健康检查：尝试获取一个测试锁并立即释放
            String testKey = "health-check:" + System.currentTimeMillis();
            boolean acquired = tryLock(testKey, 1, 5, TimeUnit.SECONDS);

            if (acquired) {
                unlock(testKey);
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            log.error("分布式锁管理器健康检查异常", e);
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

        String fullLockKey = RedisKeyConst.Consume.CONSUME_LOCK_PREFIX + lockKey;

        try {
            return redissonManager.getRemainingTime(fullLockKey);
        } catch (Exception e) {
            log.error("获取锁剩余时间异常: key={}", fullLockKey, e);
            return -1;
        }
    }
}