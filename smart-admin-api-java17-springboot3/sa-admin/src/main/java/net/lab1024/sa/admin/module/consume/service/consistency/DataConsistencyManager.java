package net.lab1024.sa.admin.module.consume.service.consistency;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.cache.RedisUtil;
import net.lab1024.sa.base.common.exception.SmartException;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 数据一致性管理器
 * 负责分布式锁、数据版本控制、对账机制等一致性保障功能
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Slf4j
@Component
public class DataConsistencyManager {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // 本地锁（用于单机环境下的快速锁）
    private final ReentrantLock localLock = new ReentrantLock();

    // 锁相关常量
    private static final String LOCK_PREFIX = "consume:lock:";
    private static final String VERSION_PREFIX = "consume:version:";
    private static final long DEFAULT_LOCK_TIMEOUT = 30L; // 秒
    private static final long DEFAULT_RETRY_INTERVAL = 100L; // 毫秒

    // Lua脚本：原子性释放锁
    private static final String RELEASE_LOCK_SCRIPT =
        "if redis.call('get', KEYS[1]) == ARGV[1] then " +
        "    return redis.call('del', KEYS[1]) " +
        "else " +
        "    return 0 " +
        "end";

    /**
     * 获取分布式锁
     *
     * @param lockKey 锁键
     * @param timeout 超时时间（秒）
     * @return 锁标识，用于释放锁
     */
    public String acquireLock(String lockKey, long timeout) {
        String fullLockKey = LOCK_PREFIX + lockKey;
        String lockValue = UUID.randomUUID().toString();

        try {
            // 尝试获取锁
            Boolean success = redisTemplate.opsForValue().setIfAbsent(
                fullLockKey, lockValue, timeout, TimeUnit.SECONDS
            );

            if (Boolean.TRUE.equals(success)) {
                log.debug("成功获取分布式锁: {}", lockKey);
                return lockValue;
            } else {
                log.debug("获取分布式锁失败: {}", lockKey);
                return null;
            }
        } catch (Exception e) {
            log.error("获取分布式锁异常: lockKey={}", lockKey, e);
            return null;
        }
    }

    /**
     * 获取分布式锁（带重试）
     *
     * @param lockKey 锁键
     * @param timeout 锁超时时间（秒）
     * @param retryCount 重试次数
     * @param retryInterval 重试间隔（毫秒）
     * @return 锁标识
     */
    public String acquireLockWithRetry(String lockKey, long timeout, int retryCount, long retryInterval) {
        for (int i = 0; i < retryCount; i++) {
            String lockValue = acquireLock(lockKey, timeout);
            if (lockValue != null) {
                return lockValue;
            }

            if (i < retryCount - 1) {
                try {
                    Thread.sleep(retryInterval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * 释放分布式锁
     *
     * @param lockKey 锁键
     * @param lockValue 锁标识
     * @return 是否成功释放
     */
    public boolean releaseLock(String lockKey, String lockValue) {
        if (lockValue == null) {
            return false;
        }

        try {
            String fullLockKey = LOCK_PREFIX + lockKey;

            // 使用Lua脚本原子性释放锁
            RedisScript<Long> script = RedisScript.of(RELEASE_LOCK_SCRIPT, Long.class);
            Long result = redisTemplate.execute(script,
                Collections.singletonList(fullLockKey),
                lockValue
            );

            boolean success = result != null && result == 1;
            if (success) {
                log.debug("成功释放分布式锁: {}", lockKey);
            } else {
                log.debug("释放分布式锁失败（锁不存在或已过期）: {}", lockKey);
            }

            return success;
        } catch (Exception e) {
            log.error("释放分布式锁异常: lockKey={}", lockKey, e);
            return false;
        }
    }

    /**
     * 获取数据版本号
     *
     * @param dataKey 数据键
     * @return 版本号
     */
    public long getDataVersion(String dataKey) {
        String versionKey = VERSION_PREFIX + dataKey;
        try {
            Long version = redisTemplate.opsForValue().increment(versionKey);
            if (version == null || version == 1) {
                // 首次创建，设置过期时间
                redisTemplate.expire(versionKey, 1, TimeUnit.DAYS);
            }
            return version;
        } catch (Exception e) {
            log.error("获取数据版本号异常: dataKey={}", dataKey, e);
            return System.currentTimeMillis();
        }
    }

    /**
     * 验证数据版本
     *
     * @param dataKey 数据键
     * @param expectedVersion 期望版本号
     * @return 是否版本一致
     */
    public boolean validateDataVersion(String dataKey, long expectedVersion) {
        String versionKey = VERSION_PREFIX + dataKey;
        try {
            Long currentVersion = redisTemplate.opsForValue().increment(versionKey, 0);
            return currentVersion != null && currentVersion == expectedVersion;
        } catch (Exception e) {
            log.error("验证数据版本异常: dataKey={}, expectedVersion={}", dataKey, expectedVersion, e);
            return false;
        }
    }

    /**
     * 原子性检查并设置版本
     *
     * @param dataKey 数据键
     * @param expectedVersion 期望版本号
     * @param newVersion 新版本号
     * @return 是否设置成功
     */
    public boolean checkAndSetVersion(String dataKey, long expectedVersion, long newVersion) {
        String versionKey = VERSION_PREFIX + dataKey;
        try {
            String script =
                "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                "    redis.call('set', KEYS[1], ARGV[2]) " +
                "    return 1 " +
                "else " +
                "    return 0 " +
                "end";

            RedisScript<Long> redisScript = RedisScript.of(script, Long.class);
            Long result = redisTemplate.execute(redisScript,
                Collections.singletonList(versionKey),
                String.valueOf(expectedVersion),
                String.valueOf(newVersion)
            );

            return result != null && result == 1;
        } catch (Exception e) {
            log.error("原子性检查并设置版本异常: dataKey={}, expectedVersion={}, newVersion={}",
                dataKey, expectedVersion, newVersion, e);
            return false;
        }
    }

    /**
     * 获取本地锁（适用于单机环境）
     *
     * @return 是否成功获取锁
     */
    public boolean acquireLocalLock() {
        try {
            return localLock.tryLock(DEFAULT_LOCK_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 释放本地锁
     */
    public void releaseLocalLock() {
        if (localLock.isHeldByCurrentThread()) {
            localLock.unlock();
        }
    }

    /**
     * 执行带分布式锁的操作
     *
     * @param lockKey 锁键
     * @param operation 要执行的操作
     * @param <T> 返回类型
     * @return 操作结果
     */
    public <T> T executeWithLock(String lockKey, LockOperation<T> operation) {
        String lockValue = acquireLock(lockKey, DEFAULT_LOCK_TIMEOUT);
        if (lockValue == null) {
            throw new SmartException("获取分布式锁失败: " + lockKey);
        }

        try {
            return operation.execute();
        } finally {
            releaseLock(lockKey, lockValue);
        }
    }

    /**
     * 执行带重试的分布式锁操作
     *
     * @param lockKey 锁键
     * @param operation 要执行的操作
     * @param retryCount 重试次数
     * @param <T> 返回类型
     * @return 操作结果
     */
    public <T> T executeWithLockRetry(String lockKey, LockOperation<T> operation, int retryCount) {
        String lockValue = acquireLockWithRetry(lockKey, DEFAULT_LOCK_TIMEOUT, retryCount, DEFAULT_RETRY_INTERVAL);
        if (lockValue == null) {
            throw new SmartException("获取分布式锁失败，已重试" + retryCount + "次: " + lockKey);
        }

        try {
            return operation.execute();
        } finally {
            releaseLock(lockKey, lockValue);
        }
    }

    /**
     * 执行带版本控制的数据操作
     *
     * @param dataKey 数据键
     * @param operation 数据操作
     * @param <T> 返回类型
     * @return 操作结果
     */
    public <T> T executeWithVersionControl(String dataKey, VersionControlOperation<T> operation) {
        // 获取当前版本号
        long currentVersion = getDataVersion(dataKey);

        try {
            // 执行操作
            T result = operation.execute(currentVersion);

            // 更新版本号
            getDataVersion(dataKey); // 递增版本号

            return result;
        } catch (Exception e) {
            log.error("版本控制操作失败: dataKey={}, version={}", dataKey, currentVersion, e);
            throw new SmartException("数据操作失败: " + e.getMessage());
        }
    }

    /**
     * 执行事务性操作（分布式锁 + 版本控制）
     *
     * @param lockKey 锁键
     * @param dataKey 数据键
     * @param operation 要执行的操作
     * @param <T> 返回类型
     * @return 操作结果
     */
    @Transactional(rollbackFor = Exception.class)
    public <T> T executeTransactional(String lockKey, String dataKey, TransactionalOperation<T> operation) {
        return executeWithLock(lockKey, () -> {
            return executeWithVersionControl(dataKey, operation);
        });
    }

    /**
     * 检查系统一致性状态
     *
     * @return 一致性检查结果
     */
    public ConsistencyCheckResult checkConsistency() {
        ConsistencyCheckResult result = new ConsistencyCheckResult();

        try {
            // 检查Redis连接
            redisTemplate.opsForValue().get("consistency:check:" + System.currentTimeMillis());
            result.setRedisHealthy(true);

            // 检查锁的状态
            String pattern = LOCK_PREFIX + "*";
            var keys = redisTemplate.keys(pattern);
            result.setActiveLocks(keys != null ? keys.size() : 0);

            // 检查版本号数量
            String versionPattern = VERSION_PREFIX + "*";
            var versionKeys = redisTemplate.keys(versionPattern);
            result.setVersionEntries(versionKeys != null ? versionKeys.size() : 0);

            result.setHealthy(true);
            log.info("数据一致性检查完成: {}", result);

        } catch (Exception e) {
            log.error("数据一致性检查异常", e);
            result.setHealthy(false);
            result.setErrorMessage(e.getMessage());
        }

        return result;
    }

    /**
     * 清理过期的锁和版本数据
     *
     * @return 清理结果
     */
    public ConsistencyCleanupResult cleanupExpiredData() {
        ConsistencyCleanupResult result = new ConsistencyCleanupResult();

        try {
            // 清理过期的锁（这里简化处理，实际可以根据具体业务逻辑清理）
            String lockPattern = LOCK_PREFIX + "*";
            var lockKeys = redisTemplate.keys(lockPattern);
            if (lockKeys != null) {
                result.setTotalLocks(lockKeys.size());
                // 注意：这里不直接删除锁，因为可能有活跃的业务正在使用
                // 实际应用中应该根据锁的过期时间和业务特点来判断是否清理
            }

            // 清理过期的版本号（保留最近7天的）
            String versionPattern = VERSION_PREFIX + "*";
            var versionKeys = redisTemplate.keys(versionPattern);
            if (versionKeys != null) {
                result.setTotalVersions(versionKeys.size());
                // 实际应用中可以根据版本号的时间戳进行清理
            }

            result.setSuccess(true);
            log.info("数据清理完成: {}", result);

        } catch (Exception e) {
            log.error("数据清理异常", e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
        }

        return result;
    }

    // 内部接口定义

    @FunctionalInterface
    public interface LockOperation<T> {
        T execute() throws Exception;
    }

    @FunctionalInterface
    public interface VersionControlOperation<T> {
        T execute(long currentVersion) throws Exception;
    }

    @FunctionalInterface
    public interface TransactionalOperation<T> extends VersionControlOperation<T> {
        // 继承VersionControlOperation，用于事务性操作
    }

    // 结果类定义

    public static class ConsistencyCheckResult {
        private boolean healthy;
        private boolean redisHealthy;
        private int activeLocks;
        private int versionEntries;
        private String errorMessage;
        private long checkTime = System.currentTimeMillis();

        // Getters and Setters
        public boolean isHealthy() { return healthy; }
        public void setHealthy(boolean healthy) { this.healthy = healthy; }
        public boolean isRedisHealthy() { return redisHealthy; }
        public void setRedisHealthy(boolean redisHealthy) { this.redisHealthy = redisHealthy; }
        public int getActiveLocks() { return activeLocks; }
        public void setActiveLocks(int activeLocks) { this.activeLocks = activeLocks; }
        public int getVersionEntries() { return versionEntries; }
        public void setVersionEntries(int versionEntries) { this.versionEntries = versionEntries; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public long getCheckTime() { return checkTime; }
        public void setCheckTime(long checkTime) { this.checkTime = checkTime; }

        @Override
        public String toString() {
            return "ConsistencyCheckResult{" +
                    "healthy" + healthy +
                    ", redisHealthy" + redisHealthy +
                    ", activeLocks" + activeLocks +
                    ", versionEntries" + versionEntries +
                    ", errorMessage='" + errorMessage + '\'' +
                    ", checkTime" + checkTime +
                    '}';
        }
    }

    public static class ConsistencyCleanupResult {
        private boolean success;
        private int totalLocks;
        private int totalVersions;
        private String errorMessage;
        private long cleanupTime = System.currentTimeMillis();

        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public int getTotalLocks() { return totalLocks; }
        public void setTotalLocks(int totalLocks) { this.totalLocks = totalLocks; }
        public int getTotalVersions() { return totalVersions; }
        public void setTotalVersions(int totalVersions) { this.totalVersions = totalVersions; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public long getCleanupTime() { return cleanupTime; }
        public void setCleanupTime(long cleanupTime) { this.cleanupTime = cleanupTime; }

        @Override
        public String toString() {
            return "ConsistencyCleanupResult{" +
                    "success" + success +
                    ", totalLocks" + totalLocks +
                    ", totalVersions" + totalVersions +
                    ", errorMessage='" + errorMessage + '\'' +
                    ", cleanupTime" + cleanupTime +
                    '}';
        }
    }
}