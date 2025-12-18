package net.lab1024.sa.common.lock;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.SystemException;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 分布式锁管理器
 *
 * 针对1000台设备、20000人规模优化的分布式锁实现
 *
 * @author IOE-DREAM Team
 * @date 2025-12-09
 * @description 提供分布式锁、读写锁、信号量等分布式同步原语
 */
@Slf4j
public class DistributedLockManager {

    private final RedissonClient redissonClient;

    // 默认锁超时时间
    private static final long DEFAULT_LOCK_TIMEOUT = 30;
    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;

    public DistributedLockManager(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    // ============================================================
    // 基础分布式锁
    // ============================================================

    /**
     * 执行带锁的操作 - 可重入锁
     */
    public <T> T executeWithLock(String lockKey, Supplier<T> action) {
        return executeWithLock(lockKey, action, DEFAULT_LOCK_TIMEOUT, DEFAULT_TIME_UNIT);
    }

    /**
     * 执行带锁的操作 - 指定超时时间
     */
    public <T> T executeWithLock(String lockKey, Supplier<T> action, long timeout, TimeUnit timeUnit) {
        return executeWithLock(lockKey, action, timeout, timeUnit, 5, timeUnit);
    }

    /**
     * 执行带锁的操作 - 完整参数
     */
    public <T> T executeWithLock(String lockKey, Supplier<T> action, long timeout, TimeUnit timeUnit,
                                  long waitTime, TimeUnit waitUnit) {
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 尝试获取锁
            boolean acquired = lock.tryLock(waitTime, timeout, timeUnit);
            if (!acquired) {
                log.warn("[分布式锁] 获取锁失败: lockKey={}, timeout={}", lockKey, timeout);
                throw new BusinessException("DISTRIBUTED_LOCK_ACQUIRE_FAILED", "获取分布式锁失败: " + lockKey);
            }

            log.debug("[分布式锁] 获取锁成功: {}", lockKey);

            try {
                // 执行业务逻辑
                T result = action.get();
                log.debug("[分布式锁] 业务执行完成: {}", lockKey);
                return result;

            } finally {
                // 释放锁
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                    log.debug("[分布式锁] 释放锁成功: {}", lockKey);
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[分布式锁] 获取锁被中断: {}", lockKey, e);
            throw new SystemException("DISTRIBUTED_LOCK_INTERRUPTED", "获取分布式锁被中断", e);
        }
    }

    /**
     * 异步执行带锁的操作
     */
    public <T> CompletableFuture<T> executeWithLockAsync(String lockKey, Supplier<T> action) {
        return executeWithLockAsync(lockKey, action, DEFAULT_LOCK_TIMEOUT, DEFAULT_TIME_UNIT);
    }

    /**
     * 异步执行带锁的操作 - 指定超时时间
     */
    public <T> CompletableFuture<T> executeWithLockAsync(String lockKey, Supplier<T> action,
                                                      long timeout, TimeUnit timeUnit) {
        return CompletableFuture.supplyAsync(() ->
            executeWithLock(lockKey, action, timeout, timeUnit));
    }

    // ============================================================
    // 公平分布式锁
    // ============================================================

    /**
     * 执行带公平锁的操作
     */
    public <T> T executeWithFairLock(String lockKey, Supplier<T> action) {
        return executeWithFairLock(lockKey, action, DEFAULT_LOCK_TIMEOUT, DEFAULT_TIME_UNIT);
    }

    /**
     * 执行带公平锁的操作 - 指定超时时间
     */
    public <T> T executeWithFairLock(String lockKey, Supplier<T> action, long timeout, TimeUnit timeUnit) {
        RLock lock = redissonClient.getFairLock(lockKey);

        try {
            boolean acquired = lock.tryLock(5, timeout, timeUnit);
            if (!acquired) {
                log.warn("[公平分布式锁] 获取锁失败, lockKey={}, timeout={}", lockKey, timeout);
                throw new BusinessException("FAIR_LOCK_ACQUIRE_FAILED", "获取公平分布式锁失败: " + lockKey);
            }

            log.debug("[公平分布式锁] 获取锁成功: {}", lockKey);

            try {
                T result = action.get();
                log.debug("[公平分布式锁] 业务执行完成: {}", lockKey);
                return result;
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                    log.debug("[公平分布式锁] 释放锁成功: {}", lockKey);
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[公平分布式锁] 获取锁被中断: {}", lockKey, e);
            throw new SystemException("FAIR_DISTRIBUTED_LOCK_INTERRUPTED", "获取公平分布式锁被中断", e);
        }
    }

    // ============================================================
    // 读写锁
    // ============================================================

    /**
     * 执行读锁操作
     */
    public <T> T executeWithReadLock(String lockKey, Supplier<T> action) {
        return executeWithReadLock(lockKey, action, DEFAULT_LOCK_TIMEOUT, DEFAULT_TIME_UNIT);
    }

    /**
     * 执行读锁操作 - 指定超时时间
     */
    public <T> T executeWithReadLock(String lockKey, Supplier<T> action, long timeout, TimeUnit timeUnit) {
        RLock readLock = redissonClient.getReadWriteLock(lockKey).readLock();

        try {
            boolean acquired = readLock.tryLock(5, timeout, timeUnit);
            if (!acquired) {
                log.warn("[读锁] 获取锁失败, lockKey={}, timeout={}", lockKey, timeout);
                throw new BusinessException("READ_LOCK_ACQUIRE_FAILED", "获取读锁失败: " + lockKey);
            }

            log.debug("[读锁] 获取成功: {}", lockKey);

            try {
                T result = action.get();
                log.debug("[读锁] 业务执行完成: {}", lockKey);
                return result;
            } finally {
                if (readLock.isHeldByCurrentThread()) {
                    readLock.unlock();
                    log.debug("[读锁] 释放成功: {}", lockKey);
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[读锁] 获取被中断: {}", lockKey, e);
            throw new SystemException("READ_LOCK_INTERRUPTED", "获取读锁被中断", e);
        }
    }

    /**
     * 执行写锁操作
     */
    public <T> T executeWithWriteLock(String lockKey, Supplier<T> action) {
        return executeWithWriteLock(lockKey, action, DEFAULT_LOCK_TIMEOUT, DEFAULT_TIME_UNIT);
    }

    /**
     * 执行写锁操作 - 指定超时时间
     */
    public <T> T executeWithWriteLock(String lockKey, Supplier<T> action, long timeout, TimeUnit timeUnit) {
        RLock writeLock = redissonClient.getReadWriteLock(lockKey).writeLock();

        try {
            boolean acquired = writeLock.tryLock(5, timeout, timeUnit);
            if (!acquired) {
                log.warn("[写锁] 获取锁失败, lockKey={}, timeout={}", lockKey, timeout);
                throw new BusinessException("WRITE_LOCK_ACQUIRE_FAILED", "获取写锁失败: " + lockKey);
            }

            log.debug("[写锁] 获取成功: {}", lockKey);

            try {
                T result = action.get();
                log.debug("[写锁] 业务执行完成: {}", lockKey);
                return result;
            } finally {
                if (writeLock.isHeldByCurrentThread()) {
                    writeLock.unlock();
                    log.debug("[写锁] 释放成功: {}", lockKey);
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[写锁] 获取被中断: {}", lockKey, e);
            throw new SystemException("WRITE_LOCK_INTERRUPTED", "获取写锁被中断", e);
        }
    }

    // ============================================================
    // 信号量
    // ============================================================

    /**
     * 执行信号量控制的操作
     */
    public <T> T executeWithSemaphore(String semaphoreKey, int permits, Supplier<T> action) {
        return executeWithSemaphore(semaphoreKey, permits, action, DEFAULT_LOCK_TIMEOUT, DEFAULT_TIME_UNIT);
    }

    /**
     * 执行信号量控制的操作 - 指定超时时间
     */
    public <T> T executeWithSemaphore(String semaphoreKey, int permits, Supplier<T> action,
                                     long timeout, TimeUnit timeUnit) {
        try {
            // 获取信号量许可
            boolean acquired = redissonClient.getSemaphore(semaphoreKey)
                    .trySetPermits(permits);
            if (!acquired) {
                log.warn("[信号量] 设置许可失败, semaphoreKey={}, permits={}", semaphoreKey, permits);
                throw new BusinessException("SEMAPHORE_SET_PERMITS_FAILED", "设置信号量许可失败: " + semaphoreKey);
            }

            // 获取许可
            redissonClient.getSemaphore(semaphoreKey).acquire();
            log.debug("[信号量] 获取许可成功: {}, permits={}", semaphoreKey, permits);

            try {
                T result = action.get();
                log.debug("[信号量] 业务执行完成: {}", semaphoreKey);
                return result;
            } finally {
                // 释放许可
                redissonClient.getSemaphore(semaphoreKey).release();
                log.debug("[信号量] 释放许可成功: {}", semaphoreKey);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[信号量] 获取许可被中断: {}", semaphoreKey, e);
            throw new SystemException("SEMAPHORE_ACQUIRE_INTERRUPTED", "获取信号量许可被中断", e);
        }
    }

        // ============================================================
    // 业务专用锁方法
    // ============================================================

    /**
     * 用户操作锁 - 针对20000人规模优化
     */
    public <T> T executeWithUserLock(Long userId, Supplier<T> action) {
        String lockKey = "user:lock:" + userId;
        return executeWithLock(lockKey, action, 10, TimeUnit.SECONDS); // 用户操作锁10秒
    }

    /**
     * 设备操作锁 - 针对1000台设备规模优化
     */
    public <T> T executeWithDeviceLock(String deviceId, Supplier<T> action) {
        String lockKey = "device:lock:" + deviceId;
        return executeWithLock(lockKey, action, 5, TimeUnit.SECONDS);  // 设备操作锁5秒
    }

    /**
     * 消费操作锁 - 针对金融交易优化
     */
    public <T> T executeWithConsumeLock(String accountId, Supplier<T> action) {
        String lockKey = "consume:lock:" + accountId;
        return executeWithLock(lockKey, action, 15, TimeUnit.SECONDS); // 消费操作锁15秒
    }

    /**
     * 考勤操作锁 - 针对打卡操作优化
     */
    public <T> T executeWithAttendanceLock(Long userId, String date, Supplier<T> action) {
        String lockKey = String.format("attendance:lock:%d:%s", userId, date);
        return executeWithLock(lockKey, action, 3, TimeUnit.SECONDS);   // 考勤操作锁3秒
    }

    /**
     * 访客操作锁
     */
    public <T> T executeWithVisitorLock(String visitorId, Supplier<T> action) {
        String lockKey = "visitor:lock:" + visitorId;
        return executeWithLock(lockKey, action, 8, TimeUnit.SECONDS);  // 访客操作锁8秒
    }

    /**
     * 批量操作锁 - 针对批量处理优化
     */
    public <T> T executeWithBatchLock(String batchType, Supplier<T> action) {
        String lockKey = "batch:lock:" + batchType + ":" + System.currentTimeMillis() / 60000; // 按分钟加锁
        return executeWithLock(lockKey, action, 60, TimeUnit.SECONDS); // 批量操作锁60秒
    }

    // ============================================================
    // 锁状态查询
    // ============================================================

    /**
     * 检查锁是否存在
     */
    public boolean isLocked(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        return lock.isLocked();
    }

    /**
     * 检查当前线程是否持有锁
     */
    public boolean isHeldByCurrentThread(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        return lock.isHeldByCurrentThread();
    }

    /**
     * 获取锁的重入次数
     */
    public int getHoldCount(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        return lock.getHoldCount();
    }

    /**
     * 强制释放锁（危险操作，仅在特殊情况下使用）
     */
    public void forceUnlock(String lockKey) {
        log.warn("[分布式锁] 强制释放锁: {}", lockKey);
        RLock lock = redissonClient.getLock(lockKey);
        lock.forceUnlock();
    }

    // ============================================================
    // 锁监控和统计
    // ============================================================

    /**
     * 锁统计信息
     */
    public LockStats getLockStats() {
        // 这里可以收集和返回锁的统计信息
        // 实际实现中可能需要维护一个统计器
        return LockStats.builder()
                .totalLockRequests(0)
                .successfulLockAcquisitions(0)
                .failedLockAcquisitions(0)
                .averageLockWaitTime(0)
                .build();
    }

    // ============================================================
    // 内部类
    // ============================================================

    /**
     * 锁统计信息
     */
    @lombok.Data
    @lombok.Builder
    public static class LockStats {
        private long totalLockRequests;
        private long successfulLockAcquisitions;
        private long failedLockAcquisitions;
        private long averageLockWaitTime;
    }
}
