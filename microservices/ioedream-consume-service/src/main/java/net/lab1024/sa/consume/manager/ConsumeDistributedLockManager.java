package net.lab1024.sa.consume.manager;

import java.time.Duration;
import java.util.function.Supplier;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.exception.ConsumeBusinessException;

/**
 * 消费模块分布式锁管理器
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖
 * - 基于Redisson实现分布式锁
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Slf4j
public class ConsumeDistributedLockManager {

    private final RedissonClient redissonClient;

    /**
     * 构造函数注入依赖
     *
     * @param redissonClient Redisson客户端
     */
    public ConsumeDistributedLockManager(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 账户操作分布式锁
     *
     * @param accountId 账户ID
     * @param operation 业务操作
     * @param <T>        返回类型
     * @return 操作结果
     */
    public <T> T executeWithAccountLock(Long accountId, Supplier<T> operation) {
        String lockKey = "lock:consume:account:" + accountId;
        return executeWithLock(lockKey, "账户", accountId.toString(), operation, Duration.ofSeconds(5), Duration.ofSeconds(30));
    }

    /**
     * 设备操作分布式锁
     *
     * @param deviceId  设备ID
     * @param operation 业务操作
     * @param <T>        返回类型
     * @return 操作结果
     */
    public <T> T executeWithDeviceLock(String deviceId, Supplier<T> operation) {
        String lockKey = "lock:consume:device:" + deviceId;
        return executeWithLock(lockKey, "设备", deviceId, operation, Duration.ofSeconds(3), Duration.ofSeconds(15));
    }

    /**
     * 交易操作分布式锁
     *
     * @param transactionId 交易ID
     * @param operation      业务操作
     * @param <T>            返回类型
     * @return 操作结果
     */
    public <T> T executeWithTransactionLock(String transactionId, Supplier<T> operation) {
        String lockKey = "lock:consume:transaction:" + transactionId;
        return executeWithLock(lockKey, "交易", transactionId, operation, Duration.ofSeconds(2), Duration.ofSeconds(10));
    }

    /**
     * 用户操作分布式锁
     *
     * @param userId    用户ID
     * @param operation 业务操作
     * @param <T>        返回类型
     * @return 操作结果
     */
    public <T> T executeWithUserLock(Long userId, Supplier<T> operation) {
        String lockKey = "lock:consume:user:" + userId;
        return executeWithLock(lockKey, "用户", userId.toString(), operation, Duration.ofSeconds(3), Duration.ofSeconds(20));
    }

    /**
     * 批量操作分布式锁
     *
     * @param batchId   批次ID
     * @param operation 业务操作
     * @param <T>       返回类型
     * @return 操作结果
     */
    public <T> T executeWithBatchLock(String batchId, Supplier<T> operation) {
        String lockKey = "lock:consume:batch:" + batchId;
        return executeWithLock(lockKey, "批量", batchId, operation, Duration.ofSeconds(5), Duration.ofMinutes(2));
    }

    /**
     * 通用分布式锁执行器
     *
     * @param lockKey       锁键
     * @param resourceType  资源类型
     * @param resourceId    资源ID
     * @param operation     业务操作
     * @param waitTime      等待时间
     * @param leaseTime     锁定时间
     * @param <T>           返回类型
     * @return 操作结果
     */
    private <T> T executeWithLock(String lockKey, String resourceType, String resourceId,
                                 Supplier<T> operation, Duration waitTime, Duration leaseTime) {
        RLock lock = redissonClient.getLock(lockKey);

        long startTime = System.currentTimeMillis();
        try {
            // 尝试获取锁
            boolean acquired = lock.tryLock(waitTime.toMillis(), leaseTime.toMillis(), TimeUnit.MILLISECONDS);

            long lockAcquireTime = System.currentTimeMillis() - startTime;

            if (!acquired) {
                log.warn("[分布式锁] 获取{}锁失败: resourceId={}, waitTime={}ms", resourceType, resourceId, waitTime.toMillis());
                throw new ConsumeBusinessException("LOCK_ACQUIRE_FAILED",
                    String.format("获取%s锁失败，请稍后重试", resourceType));
            }

            log.debug("[分布式锁] 获取{}锁成功: resourceId={}, waitTime={}ms, leaseTime={}ms",
                resourceType, resourceId, lockAcquireTime, leaseTime.toMillis());

            try {
                // 执行业务操作
                T result = operation.get();

                log.debug("[分布式锁] {}操作执行成功: resourceId={}", resourceType, resourceId);
                return result;

            } finally {
                // 释放锁（只释放当前线程持有的锁）
                if (lock.isHeldByCurrentThread()) {
                    long lockHoldTime = System.currentTimeMillis() - startTime;
                    lock.unlock();
                    log.debug("[分布式锁] 释放{}锁成功: resourceId={}, holdTime={}ms",
                        resourceType, resourceId, lockHoldTime);
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[分布式锁] 获取{}锁被中断: resourceId={}", resourceType, resourceId, e);
            throw new ConsumeBusinessException("LOCK_INTERRUPTED",
                String.format("获取%s锁被中断", resourceType));

        } catch (ConsumeBusinessException e) {
            // 业务异常直接抛出
            throw e;

        } catch (Exception e) {
            log.error("[分布式锁] {}操作异常: resourceId={}, error={}", resourceType, resourceId, e.getMessage(), e);
            throw new ConsumeBusinessException("LOCK_OPERATION_FAILED",
                String.format("%s操作失败: %s", resourceType, e.getMessage()));

        }
    }

    /**
     * 尝试获取锁状态
     *
     * @param lockKey 锁键
     * @return 锁状态
     */
    public boolean isLocked(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        return lock.isLocked();
    }

    /**
     * 检查锁是否被当前线程持有
     *
     * @param lockKey 锁键
     * @return 是否持有锁
     */
    public boolean isHeldByCurrentThread(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        return lock.isHeldByCurrentThread();
    }

    /**
     * 获取锁等待队列长度
     *
     * @param lockKey 锁键
     * @return 等待队列长度
     */
    public int getLockQueueLength(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        return lock.getHoldCount();
    }
}