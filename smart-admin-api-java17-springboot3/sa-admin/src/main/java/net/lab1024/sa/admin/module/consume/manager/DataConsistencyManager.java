package net.lab1024.sa.admin.module.consume.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.consume.dao.AccountBalanceDao;
import net.lab1024.sa.admin.module.consume.domain.entity.AccountBalanceEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 数据一致性管理器
 * 负责分布式锁、乐观锁、数据一致性保障等功能
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Slf4j
@Component
public class DataConsistencyManager {

    @Resource
    private AccountBalanceDao accountBalanceDao;

    // 本地锁用于并发控制
    private final Lock localLock = new ReentrantLock();

    // 最大重试次数
    private static final int MAX_RETRY_COUNT = 3;

    /**
     * 使用分布式锁执行操作
     *
     * @param lockKey 锁的键
     * @param timeout 超时时间（秒）
     * @param action 要执行的操作
     * @param <T> 返回类型
     * @return 操作结果
     * @throws Exception 操作异常
     */
    public <T> T executeWithDistributedLock(String lockKey, long timeout, Supplier<T> action) throws Exception {
        // 1. 获取分布式锁
        DistributedLock lock = acquireDistributedLock(lockKey, timeout);

        try {
            if (lock == null) {
                throw new RuntimeException("无法获取分布式锁: " + lockKey);
            }

            // 2. 执行业务逻辑
            T result = action.get();

            log.debug("分布式锁执行成功: lockKey={}, resultType={}", lockKey, result.getClass().getSimpleName());
            return result;

        } finally {
            // 3. 释放锁
            if (lock != null) {
                lock.release();
                log.debug("分布式锁已释放: lockKey={}", lockKey);
            }
        }
    }

    /**
     * 余额更新操作接口
     */
    @FunctionalInterface
    public interface BalanceUpdateOperation {
        BalanceUpdateResult execute(AccountBalanceEntity balance);
    }

    /**
     * 使用乐观锁更新账户余额
     *
     * @param accountId 账户ID
     * @param operation 更新操作
     * @return 是否更新成功
     * @throws RuntimeException 操作异常
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateWithOptimisticLock(Long accountId, BalanceUpdateOperation operation) {
        localLock.lock();
        try {
            for (int i = 0; i < MAX_RETRY_COUNT; i++) {
                // 1. 读取当前版本
                AccountBalanceEntity balance = accountBalanceDao.selectById(accountId);
                if (balance == null) {
                    throw new RuntimeException("账户不存在: " + accountId);
                }

                // 2. 执行业务逻辑
                BalanceUpdateResult updateResult = operation.execute(balance);
                if (!updateResult.isUpdateNeeded()) {
                    return true; // 不需要更新
                }

                // 3. 乐观锁更新
                int currentVersion = balance.getVersion() != null ? balance.getVersion() : 0;
                balance.setVersion(currentVersion + 1);
                balance.setBalance(updateResult.getNewBalance());
                balance.setUpdateTime(LocalDateTime.now());
                balance.setUpdateUserId(getCurrentUserId());

                int updateCount = accountBalanceDao.updateById(balance);

                if (updateCount > 0) {
                    // 更新成功
                    log.info("乐观锁更新成功: accountId={}, oldBalance={}, newBalance={}, version={}",
                            accountId, balance.getBalance().subtract(updateResult.getAmountChange()),
                            updateResult.getNewBalance(), balance.getVersion());
                    return true;
                }

                // 版本冲突，重试
                log.warn("乐观锁版本冲突，准备重试: accountId={}, attempt={}, currentVersion={}",
                        accountId, i + 1, currentVersion);

                if (i < MAX_RETRY_COUNT - 1) {
                    try {
                        Thread.sleep(calculateBackoffDelay(i));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("乐观锁重试被中断", e);
                    }
                }
            }

            throw new RuntimeException("乐观锁更新失败，已达到最大重试次数: " + MAX_RETRY_COUNT);

        } finally {
            localLock.unlock();
        }
    }

    /**
     * 原子性扣减余额
     *
     * @param accountId 账户ID
     * @param amount 扣减金额
     * @param operationType 操作类型
     * @return 扣减结果
     * @throws Exception 操作异常
     */
    @Transactional(rollbackFor = Exception.class)
    public AccountBalanceDeductResult deductBalanceAtomic(Long accountId, java.math.BigDecimal amount, String operationType) {
        String lockKey = "account_balance_lock:" + accountId;

        try {
            boolean updateSuccess = executeWithDistributedLock(lockKey, 30, () -> {
                return updateWithOptimisticLock(accountId, balance -> {
                    java.math.BigDecimal currentBalance = balance.getBalance();
                    if (currentBalance.compareTo(amount) < 0) {
                        return BalanceUpdateResult.noUpdate("余额不足");
                    }

                    java.math.BigDecimal newBalance = currentBalance.subtract(amount);

                    // 更新冻结金额逻辑（如果有）
                    // newFrozenAmount = updateFrozenAmount(balance, operationType, amount);

                    return BalanceUpdateResult.success(newBalance, amount, "余额扣减成功");
                });
            });

            if (updateSuccess) {
                // 扣减成功，需要获取更新后的余额信息
                AccountBalanceEntity updatedBalance = accountBalanceDao.selectById(accountId);
                return AccountBalanceDeductResult.success(updatedBalance.getBalance(), amount, "余额扣减成功");
            } else {
                return AccountBalanceDeductResult.failure("余额扣减失败");
            }

        } catch (Exception e) {
            log.error("原子性扣减余额异常: accountId={}, amount={}", accountId, amount, e);
            return AccountBalanceDeductResult.failure("扣减过程异常: " + e.getMessage());
        }
    }

    /**
     * 验证数据一致性
     *
     * @param accountId 账户ID
     * @return 验证结果
     */
    public ConsistencyValidationResult validateDataConsistency(Long accountId) {
        try {
            // 1. 检查账户余额数据一致性
            AccountBalanceEntity balance = accountBalanceDao.selectById(accountId);
            if (balance == null) {
                return ConsistencyValidationResult.error("账户不存在");
            }

            // 2. 检查余额与消费记录的一致性
            java.math.BigDecimal totalConsumedAmount = calculateTotalConsumedAmount(accountId);
            java.math.BigDecimal expectedBalance = calculateExpectedBalance(accountId, totalConsumedAmount);

            java.math.BigDecimal actualBalance = balance.getBalance();
            java.math.BigDecimal difference = actualBalance.subtract(expectedBalance);

            if (difference.abs().compareTo(java.math.BigDecimal.ZERO) > 0.01) {
                log.warn("检测到数据不一致: accountId={}, actualBalance={}, expectedBalance={}, difference={}",
                        accountId, actualBalance, expectedBalance, difference);
                return ConsistencyValidationResult.warning(
                        "账户余额与消费记录不一致",
                        Map.of("actualBalance", actualBalance, "expectedBalance", expectedBalance, "difference", difference)
                );
            }

            // 3. 检查版本号合理性
            if (balance.getVersion() != null && balance.getVersion() < 0) {
                return ConsistencyValidationResult.error("版本号无效: " + balance.getVersion());
            }

            return ConsistencyValidationResult.success("数据一致性验证通过");

        } catch (Exception e) {
            log.error("数据一致性验证异常: accountId={}", accountId, e);
            return ConsistencyValidationResult.error("验证过程异常: " + e.getMessage());
        }
    }

    /**
     * 修复数据不一致
     *
     * @param accountId 账户ID
     * @return 修复结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ConsistencyRepairResult repairDataInconsistency(Long accountId) {
        try {
            AccountBalanceEntity balance = accountBalanceDao.selectById(accountId);
            if (balance == null) {
                return ConsistencyRepairResult.error("账户不存在，无法修复");
            }

            java.math.BigDecimal totalConsumedAmount = calculateTotalConsumedAmount(accountId);
            java.math.BigDecimal expectedBalance = calculateExpectedBalance(accountId, totalConsumedAmount);

            java.math.BigDecimal currentBalance = balance.getBalance();
            java.math.BigDecimal difference = currentBalance.subtract(expectedBalance);

            // 如果差异很小（1分钱以内），可能是正常的舍入误差，不需要修复
            if (difference.abs().compareTo(new java.math.BigDecimal("0.01")) <= 0) {
                return ConsistencyRepairResult.success("数据一致性正常，无需修复");
            }

            // 修复余额
            balance.setBalance(expectedBalance);
            balance.setVersion((balance.getVersion() != null ? balance.getVersion() : 0) + 1);
            balance.setUpdateTime(LocalDateTime.now());
            balance.setUpdateUserId(getCurrentUserId());

            int updateCount = accountBalanceDao.updateById(balance);
            if (updateCount > 0) {
                log.info("数据一致性修复成功: accountId={}, oldBalance={}, newBalance={}, difference={}",
                        accountId, currentBalance, expectedBalance, difference);
                return ConsistencyRepairResult.success(
                        "数据修复成功",
                        Map.of("oldBalance", currentBalance, "newBalance", expectedBalance, "difference", difference)
                );
            } else {
                return ConsistencyRepairResult.error("数据修复失败，数据库更新异常");
            }

        } catch (Exception e) {
            log.error("数据一致性修复异常: accountId={}", accountId, e);
            return ConsistencyRepairResult.error("修复过程异常: " + e.getMessage());
        }
    }

    /**
     * 获取分布式锁统计信息
     *
     * @return 统计信息
     */
    public Map<String, Object> getDistributedLockStatistics() {
        Map<String, Object> stats = new HashMap<>();

        try {
            // TODO: 实现分布式锁统计信息收集
            stats.put("activeLocks", 0);
            stats.put("totalOperations", 0);
            stats.put("averageWaitTime", 0.0);
            stats.put("lockTimeoutCount", 0);
            stats.put("optimisticLockRetries", 0);

        } catch (Exception e) {
            log.error("获取分布式锁统计信息失败", e);
            stats.put("error", "统计信息获取失败");
        }

        return stats;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 获取分布式锁
     */
    private DistributedLock acquireDistributedLock(String lockKey, long timeout) {
        try {
            // TODO: 实现分布式锁获取逻辑
            // 这里简化实现，返回null表示无法获取锁
            // 实际应该使用Redis或专门的分布式锁服务

            // 模拟锁获取成功
            DistributedLock lock = new DistributedLock(lockKey, timeout);

            // 模拟锁检查
            if (Math.random() > 0.1) { // 90%成功率
                return lock;
            }

            return null;
        } catch (Exception e) {
            log.error("获取分布式锁失败: lockKey={}", lockKey, e);
            return null;
        }
    }

    /**
     * 计算总消费金额
     */
    private java.math.BigDecimal calculateTotalConsumedAmount(Long accountId) {
        try {
            // TODO: 从消费记录表计算总消费金额
            // 这里简化实现，返回0
            return java.math.BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("计算总消费金额失败: accountId={}", accountId, e);
            return java.math.BigDecimal.ZERO;
        }
    }

    /**
     * 计算预期余额
     */
    private java.math.BigDecimal calculateExpectedBalance(Long accountId, java.math.BigDecimal totalConsumed) {
        try {
            // TODO: 基于充值记录和消费记录计算预期余额
            // 这里简化实现
            return totalConsumed.negate();
        } catch (Exception e) {
            log.error("计算预期余额失败: accountId={}, consumed={}", accountId, totalConsumed, e);
            return java.math.BigDecimal.ZERO;
        }
    }

    /**
     * 计算退避延迟
     */
    private long calculateBackoffDelay(int attempt) {
        // 指数退避策略：基础延迟 * 2^attempt
        long baseDelay = 10; // 10ms
        long maxDelay = 1000; // 最大1秒

        return Math.min(baseDelay * (1L << attempt), maxDelay);
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        // TODO: 从安全上下文获取当前用户ID
        // 这里简化实现
        return 1L;
    }

    /**
     * 分布式锁内部类
     */
    private static class DistributedLock {
        private final String key;
        private final long timeout;
        private final LocalDateTime createdAt;
        private volatile boolean locked = true;

        public DistributedLock(String key, long timeout) {
            this.key = key;
            this.timeout = timeout;
            this.createdAt = LocalDateTime.now();
        }

        public void release() {
            locked = false;
            log.debug("分布式锁已释放: key={}, heldTime={}ms",
                    key, java.time.Duration.between(createdAt, LocalDateTime.now()).toMillis());
        }

        public boolean isLocked() {
            return locked;
        }

        public String getKey() { return key; }
        public long getTimeout() { return timeout; }
        public LocalDateTime getCreatedAt() { return createdAt; }
    }

    /**
     * 余额更新结果
     */
    public static class BalanceUpdateResult {
        private final boolean updateNeeded;
        private final java.math.BigDecimal newBalance;
        private final java.math.BigDecimal amountChange;
        private final String message;

        public BalanceUpdateResult(boolean updateNeeded, java.math.BigDecimal newBalance,
                java.math.BigDecimal amountChange, String message) {
            this.updateNeeded = updateNeeded;
            this.newBalance = newBalance;
            this.amountChange = amountChange;
            this.message = message;
        }

        public static BalanceUpdateResult success(java.math.BigDecimal newBalance,
                java.math.BigDecimal amountChange, String message) {
            return new BalanceUpdateResult(true, newBalance, amountChange, message);
        }

        public static BalanceUpdateResult noUpdate(String message) {
            return new BalanceUpdateResult(false, null, null, message);
        }

        public boolean isUpdateNeeded() { return updateNeeded; }
        public java.math.BigDecimal getNewBalance() { return newBalance; }
        public java.math.BigDecimal getAmountChange() { return amountChange; }
        public String getMessage() { return message; }
    }

    /**
     * 余额扣减结果
     */
    public static class AccountBalanceDeductResult {
        private final boolean success;
        private final java.math.BigDecimal newBalance;
        private final java.math.BigDecimal deductedAmount;
        private final String message;

        public AccountBalanceDeductResult(boolean success, java.math.BigDecimal newBalance,
                java.math.BigDecimal deductedAmount, String message) {
            this.success = success;
            this.newBalance = newBalance;
            this.deductedAmount = deductedAmount;
            this.message = message;
        }

        public static AccountBalanceDeductResult success(java.math.BigDecimal newBalance,
                java.math.BigDecimal deductedAmount, String message) {
            return new AccountBalanceDeductResult(true, newBalance, deductedAmount, message);
        }

        public static AccountBalanceDeductResult failure(String message) {
            return new AccountBalanceDeductResult(false, null, null, message);
        }

        public boolean isSuccess() { return success; }
        public java.math.BigDecimal getNewBalance() { return newBalance; }
        public java.math.BigDecimal getDeductedAmount() { return deductedAmount; }
        public String getMessage() { return message; }
    }

    /**
     * 一致性验证结果
     */
    public static class ConsistencyValidationResult {
        private final String status;
        private final String message;
        private final Map<String, Object> details;

        public ConsistencyValidationResult(String status, String message) {
            this.status = status;
            this.message = message;
            this.details = new HashMap<>();
        }

        public ConsistencyValidationResult(String status, String message, Map<String, Object> details) {
            this.status = status;
            this.message = message;
            this.details = details;
        }

        public static ConsistencyValidationResult success(String message) {
            return new ConsistencyValidationResult("SUCCESS", message);
        }

        public static ConsistencyValidationResult warning(String message) {
            return new ConsistencyValidationResult("WARNING", message, new HashMap<>());
        }

        public static ConsistencyValidationResult warning(String message, Map<String, Object> details) {
            return new ConsistencyValidationResult("WARNING", message, details);
        }

        public static ConsistencyValidationResult error(String message) {
            return new ConsistencyValidationResult("ERROR", message);
        }

        public String getStatus() { return status; }
        public String getMessage() { return message; }
        public Map<String, Object> getDetails() { return details; }
    }

    /**
     * 一致性修复结果
     */
    public static class ConsistencyRepairResult {
        private final String status;
        private final String message;
        private final Map<String, Object> details;

        public ConsistencyRepairResult(String status, String message) {
            this.status = status;
            this.message = message;
            this.details = new HashMap<>();
        }

        public ConsistencyRepairResult(String status, String message, Map<String, Object> details) {
            this.status = status;
            this.message = message;
            this.details = details;
        }

        public static ConsistencyRepairResult success(String message) {
            return new ConsistencyRepairResult("SUCCESS", message);
        }

        public static ConsistencyRepairResult success(String message, Map<String, Object> details) {
            return new ConsistencyRepairResult("SUCCESS", message, details);
        }

        public static ConsistencyRepairResult error(String message) {
            return new ConsistencyRepairResult("ERROR", message);
        }

        public static ConsistencyRepairResult error(String message, Map<String, Object> details) {
            return new ConsistencyRepairResult("ERROR", message, details);
        }

        public String getStatus() { return status; }
        public String getMessage() { return message; }
        public Map<String, Object> getDetails() { return details; }
    }
}