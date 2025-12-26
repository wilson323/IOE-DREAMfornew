package net.lab1024.sa.consume.lock;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁管理器
 *
 * 职责：提供分布式锁能力，防止并发冲突
 *
 * 使用场景：
 * 1. 账户扣款：防止同一账户并发扣款
 * 2. 库存扣减：防止超卖
 * 3. 限流控制：防止流量突增
 *
 * 特性：
 * - 可重入锁
 * - 自动续期（看门狗机制）
 * - 超时释放
 * - 锁等待
 *
 * 性能目标：
 * - 加锁时间 ≤ 10ms
 * - 锁粒度：账户级别
 * - 锁超时：30秒（自动续期）
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-23
 */
@Slf4j
public class DistributedLockManager {

    private final RedissonClient redissonClient;

    /**
     * 默认锁超时时间（秒）
     */
    private static final long DEFAULT_LEASE_TIME = 30;

    /**
     * 默认等待时间（毫秒）
     */
    private static final long DEFAULT_WAIT_TIME = 3000;

    public DistributedLockManager(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 执行带锁的操作
     *
     * @param lockKey 锁键
     * @param operation 要执行的操作
     * @param <T> 返回类型
     * @return 操作结果
     */
    public <T> T executeWithLock(String lockKey, LockOperation<T> operation) {
        return executeWithLock(lockKey, DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME, TimeUnit.SECONDS, operation);
    }

    /**
     * 执行带锁的操作（自定义超时）
     *
     * @param lockKey 锁键
     * @param waitTime 等待时间
     * @param leaseTime 锁超时时间
     * @param unit 时间单位
     * @param operation 要执行的操作
     * @param <T> 返回类型
     * @return 操作结果
     */
    public <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit, LockOperation<T> operation) {
        RLock lock = redissonClient.getLock(lockKey);
        boolean acquired = false;

        try {
            // 尝试获取锁
            acquired = lock.tryLock(waitTime, leaseTime, unit);

            if (!acquired) {
                log.warn("[分布式锁] 获取锁失败: lockKey={}, waitTime={}, leaseTime={}",
                        lockKey, waitTime, leaseTime);
                throw new RuntimeException("获取锁失败: " + lockKey);
            }

            log.debug("[分布式锁] 获取锁成功: lockKey={}", lockKey);

            // 执行业务操作
            long startTime = System.currentTimeMillis();
            T result = operation.execute();
            long elapsedTime = System.currentTimeMillis() - startTime;

            log.debug("[分布式锁] 操作完成: lockKey={},耗时={}ms", lockKey, elapsedTime);

            return result;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[分布式锁] 获取锁中断: lockKey={}, error={}", lockKey, e.getMessage(), e);
            throw new RuntimeException("获取锁中断", e);
        } catch (Exception e) {
            log.error("[分布式锁] 执行异常: lockKey={}, error={}", lockKey, e.getMessage(), e);
            throw new RuntimeException("执行异常", e);
        } finally {
            // 释放锁
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("[分布式锁] 释放锁: lockKey={}", lockKey);
            }
        }
    }

    /**
     * 尝试获取锁（非阻塞）
     *
     * @param lockKey 锁键
     * @return 是否获取成功
     */
    public boolean tryLock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        return lock.tryLock();
    }

    /**
     * 释放锁
     *
     * @param lockKey 锁键
     */
    public void unlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
            log.debug("[分布式锁] 手动释放锁: lockKey={}", lockKey);
        }
    }

    /**
     * 检查锁是否被占用
     *
     * @param lockKey 锁键
     * @return 是否被占用
     */
    public boolean isLocked(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        return lock.isLocked();
    }

    /**
     * 生成账户锁键
     *
     * @param accountId 账户ID
     * @return 锁键
     */
    public static String accountLockKey(Long accountId) {
        return "lock:account:" + accountId;
    }

    /**
     * 生成用户锁键
     *
     * @param userId 用户ID
     * @return 锁键
     */
    public static String userLockKey(Long userId) {
        return "lock:user:" + userId;
    }

    /**
     * 生成区域锁键
     *
     * @param areaId 区域ID
     * @return 锁键
     */
    public static String areaLockKey(Long areaId) {
        return "lock:area:" + areaId;
    }

    /**
     * 生成设备锁键
     *
     * @param deviceId 设备ID
     * @return 锁键
     */
    public static String deviceLockKey(String deviceId) {
        return "lock:device:" + deviceId;
    }

    /**
     * 带锁操作接口
     *
     * @param <T> 返回类型
     */
    @FunctionalInterface
    public interface LockOperation<T> {
        /**
         * 执行操作
         *
         * @return 操作结果
         * @throws Exception 执行异常
         */
        T execute() throws Exception;
    }
}
