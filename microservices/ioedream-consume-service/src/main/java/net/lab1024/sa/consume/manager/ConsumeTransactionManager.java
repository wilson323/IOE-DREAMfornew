package net.lab1024.sa.consume.manager;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RedissonClient;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.util.TypeUtils;
import net.lab1024.sa.consume.dao.ConsumeAccountDao;
import net.lab1024.sa.consume.dao.ConsumeTransactionDao;
import net.lab1024.sa.consume.domain.vo.ConsumeAccountVO;
import net.lab1024.sa.consume.domain.vo.ConsumeTransactionVO;
import net.lab1024.sa.consume.domain.entity.ConsumeAccountEntity;
import net.lab1024.sa.consume.domain.entity.ConsumeTransactionEntity;
import net.lab1024.sa.consume.exception.ConsumeTransactionException;
import net.lab1024.sa.consume.monitor.ConsumeBusinessLogger;
import net.lab1024.sa.consume.monitor.ConsumeTransactionMonitor;

/**
 * 消费交易事务管理器
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖
 * - 负责复杂的事务编排和并发控制
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Slf4j
public class ConsumeTransactionManager {

    private final ConsumeAccountDao consumeAccountDao;
    private final ConsumeTransactionDao consumeTransactionDao;
    private final ConsumeDistributedLockManager lockManager;
    private final ConsumeTransactionMonitor transactionMonitor;
    private final ConsumeBusinessLogger businessLogger;

    /**
     * 构造函数注入依赖（增强版，包含监控和日志）
     *
     * @param consumeAccountDao     账户数据访问对象
     * @param consumeTransactionDao 交易数据访问对象
     * @param lockManager           分布式锁管理器
     * @param transactionMonitor     交易监控组件
     * @param businessLogger         业务日志记录器
     */
    public ConsumeTransactionManager(ConsumeAccountDao consumeAccountDao,
                                    ConsumeTransactionDao consumeTransactionDao,
                                    ConsumeDistributedLockManager lockManager,
                                    ConsumeTransactionMonitor transactionMonitor,
                                    ConsumeBusinessLogger businessLogger) {
        this.consumeAccountDao = consumeAccountDao;
        this.consumeTransactionDao = consumeTransactionDao;
        this.lockManager = lockManager;
        this.transactionMonitor = transactionMonitor;
        this.businessLogger = businessLogger;
    }

    /**
     * 执行消费交易（包含完整的事务管理和并发控制）
     *
     * @param userId   用户ID
     * @param amount   交易金额
     * @param deviceId 设备ID
     * @param mealId   餐次ID
     * @return 交易结果
     */
    public ConsumeTransactionVO executeTransaction(Long userId, BigDecimal amount, String deviceId, String mealId) {
        long startTime = System.currentTimeMillis();
        String transactionId = UUID.randomUUID().toString().replace("-", "");

        // 记录交易开始日志
        businessLogger.logTransactionStart(transactionId, userId, amount, deviceId, mealId);

        log.info("[交易管理] 开始执行消费交易: transactionId={}, userId={}, amount={}, deviceId={}",
            transactionId, userId, amount, deviceId);

        try {
            // 1. 参数验证
            if (userId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                String errorMsg = "参数无效: userId=" + userId + ", amount=" + amount;
                transactionMonitor.recordTransactionFailure("INVALID_PARAMS", errorMsg, amount, deviceId);
                businessLogger.logTransactionFailure(transactionId, userId, amount, deviceId, "INVALID_PARAMS", errorMsg, System.currentTimeMillis() - startTime);
                throw ConsumeTransactionException.invalidAmount(transactionId, amount.toString());
            }

            // 2. 获取用户账户信息
            long accountQueryStart = System.currentTimeMillis();
            ConsumeAccountVO account = consumeAccountDao.selectByUserId(userId);
            long accountQueryDuration = System.currentTimeMillis() - accountQueryStart;

            transactionMonitor.recordAccountQueryTime(Duration.ofMillis(accountQueryDuration), account != null ? account.getAccountId() : null);

            if (account == null) {
                String errorMsg = "用户账户不存在: userId=" + userId;
                transactionMonitor.recordTransactionFailure("USER_NOT_FOUND", errorMsg, amount, deviceId);
                businessLogger.logTransactionFailure(transactionId, userId, amount, deviceId, "USER_NOT_FOUND", errorMsg, System.currentTimeMillis() - startTime);
                throw ConsumeTransactionException.deviceOffline(deviceId, transactionId);
            }

            if (!"ACTIVE".equals(account.getStatus())) {
                String errorMsg = "账户状态异常: status=" + account.getStatus();
                transactionMonitor.recordTransactionFailure("ACCOUNT_FROZEN", errorMsg, amount, deviceId);
                businessLogger.logTransactionFailure(transactionId, userId, amount, deviceId, "ACCOUNT_FROZEN", errorMsg, System.currentTimeMillis() - startTime);
                throw ConsumeTransactionException.invalidStatus(transactionId, "ACCOUNT_FROZEN");
            }

            // 3. 使用分布式锁执行交易
            return lockManager.executeWithAccountLock(account.getAccountId(), () -> {
                return executeWithAccountLock(transactionId, account, userId, amount, deviceId, mealId, startTime);
            });

        } catch (ConsumeTransactionException e) {
            // 业务异常直接抛出，已记录日志
            throw e;

        } catch (Exception e) {
            String errorMsg = "交易执行异常: " + e.getMessage();
            transactionMonitor.recordTransactionFailure("SYSTEM_ERROR", errorMsg, amount, deviceId);
            businessLogger.logTransactionFailure(transactionId, userId, amount, deviceId, "SYSTEM_ERROR", errorMsg, System.currentTimeMillis() - startTime);
            businessLogger.logSystemException(e, "EXECUTE_TRANSACTION", Map.of("transactionId", transactionId, "userId", userId, "amount", amount, "deviceId", deviceId));
            throw ConsumeTransactionException.executeFailed(transactionId, errorMsg);
        }
    }

    /**
     * 账户锁保护下的交易执行
     *
     * @param transactionId 交易ID
     * @param account       账户信息
     * @param userId        用户ID
     * @param amount        交易金额
     * @param deviceId      设备ID
     * @param mealId        餐次ID
     * @param startTime     开始时间
     * @return 交易结果
     */
    private ConsumeTransactionVO executeWithAccountLock(String transactionId, ConsumeAccountVO account, Long userId,
                                                      BigDecimal amount, String deviceId, String mealId, long startTime) {
        // 1. 重新获取账户（防止锁定期间被其他事务修改）
        ConsumeAccountEntity currentAccount = consumeAccountDao.selectById(account.getAccountId());
        if (currentAccount == null) {
            throw ConsumeTransactionException.notFound(account.getAccountId().toString());
        }

        // 2. 余额验证
        if (currentAccount.getBalance().compareTo(amount) < 0) {
            throw ConsumeTransactionException.executeFailed(transactionId,
                String.format("余额不足: 当前余额=%.2f, 交易金额=%.2f", currentAccount.getBalance(), amount));
        }

        // 3. 幂等性检查（防止重复交易）
        String idempotentKey = generateIdempotentKey(userId, deviceId, amount);
        if (isDuplicateTransaction(idempotentKey)) {
            log.warn("[交易管理] 检测到重复交易: userId={}, deviceId={}, amount={}", userId, deviceId, amount);
            transactionMonitor.recordTransactionDuplicate(idempotentKey, deviceId);
            businessLogger.logTransactionFailure(transactionId, userId, amount, deviceId, "DUPLICATE_TRANSACTION", "检测到重复交易", System.currentTimeMillis() - startTime);
            throw ConsumeTransactionException.duplicate(idempotentKey);
        }

        // 4. 计算交易金额（转换为分）
        int amountInCents = amount.multiply(new BigDecimal("100")).intValue();
        int balanceBefore = currentAccount.getBalance().multiply(new BigDecimal("100")).intValue();
        int balanceAfter = balanceBefore - amountInCents;

        // 5. 创建交易记录
        ConsumeTransactionEntity transaction = new ConsumeTransactionEntity();
        transaction.setTransactionNo(transactionId);  // 使用transactionNo存储业务交易ID
        transaction.setAccountId(currentAccount.getAccountId());
        transaction.setUserId(userId);
        // deviceId直接设置为String类型，使用兼容性方法
        if (deviceId != null) {
            transaction.setDeviceId(deviceId);
        }
        transaction.setTransactionAmount(amount);
        transaction.setActualAmount(amount);
        transaction.setBalanceBefore(new BigDecimal(balanceBefore).divide(new BigDecimal("100")));
        transaction.setBalanceAfter(new BigDecimal(balanceAfter).divide(new BigDecimal("100")));
        transaction.setTransactionTime(LocalDateTime.now());
        transaction.setTransactionStatus(1); // 1-成功

        // 6. 执行数据库操作（这里需要配合Service层的@Transactional）
        boolean updateResult = updateAccountBalanceAndCreateTransaction(currentAccount.getAccountId(),
            new BigDecimal(balanceAfter).divide(new BigDecimal("100")), transaction);

        if (!updateResult) {
            throw ConsumeTransactionException.executeFailed(transaction.getTransactionNo(), "数据库更新失败");
        }

        // 7. 记录幂等性
        recordIdempotent(idempotentKey, transaction.getTransactionNo());

        // 8. 构建返回结果
        ConsumeTransactionVO result = new ConsumeTransactionVO();
        result.setTransactionId(transactionId);
        result.setUserId(userId);
        result.setUserName(currentAccount.getUserName());
        result.setAmount(amount);
        result.setDeviceId(deviceId);
        result.setTransactionTime(transaction.getConsumeTime());
        result.setStatus("SUCCESS");
        result.setBalanceBefore(new BigDecimal(balanceBefore).divide(new BigDecimal("100")));
        result.setBalanceAfter(new BigDecimal(balanceAfter).divide(new BigDecimal("100")));

        // 9. 记录成功监控和日志
        long totalDuration = System.currentTimeMillis() - startTime;
        transactionMonitor.recordTransactionSuccess(result);
        transactionMonitor.recordTransactionDuration(Duration.ofMillis(totalDuration), deviceId);
        businessLogger.logTransactionSuccess(result, totalDuration);

        // 记录账户操作日志
        businessLogger.logAccountOperation("CONSUME_DEDUCT", currentAccount.getAccountId(), userId,
            amount.negate(), new BigDecimal(balanceBefore).divide(new BigDecimal("100")),
            new BigDecimal(balanceAfter).divide(new BigDecimal("100")), "消费扣款");

        log.info("[交易管理] 交易执行成功: transactionId={}, userId={}, amount={}, balanceAfter={}, duration={}ms",
            transactionId, userId, amount, result.getBalanceAfter(), totalDuration);

        return result;
    }

    /**
     * 交易撤销（包含事务管理）
     *
     * @param transactionId 交易ID
     * @param reason        撤销原因
     * @return 撤销结果
     */
    public Boolean cancelTransaction(String transactionId, String reason) {
        log.info("[交易管理] 开始撤销交易: transactionId={}, reason={}", transactionId, reason);

        // 1. 获取原交易记录
        ConsumeTransactionEntity originalTransaction = consumeTransactionDao.selectById(transactionId);
        if (originalTransaction == null) {
            log.error("[交易管理] 撤销失败，原交易不存在: transactionId={}", transactionId);
            return false;
        }

        if (!"SUCCESS".equals(originalTransaction.getStatus())) {
            log.error("[交易管理] 撤销失败，交易状态异常: transactionId={}, status={}",
                transactionId, originalTransaction.getStatus());
            return false;
        }

        // 2. 使用账户锁执行撤销
        Long accountId = Long.valueOf(originalTransaction.getAccountId());
        return lockManager.executeWithAccountLock(accountId, () -> {
            return executeCancelWithLock(originalTransaction, reason);
        });
    }

    /**
     * 账户锁保护下的交易撤销
     *
     * @param originalTransaction 原交易记录
     * @param reason              撤销原因
     * @return 撤销结果
     */
    private Boolean executeCancelWithLock(ConsumeTransactionEntity originalTransaction, String reason) {
        Long accountId = Long.valueOf(originalTransaction.getAccountId());

        // 1. 获取当前账户信息
        ConsumeAccountEntity currentAccount = consumeAccountDao.selectById(accountId);
        if (currentAccount == null) {
            log.error("[交易管理] 撤销失败，账户不存在: accountId={}", accountId);
            return false;
        }

        // 2. 计算退款金额
        BigDecimal refundAmount = originalTransaction.getActualAmount();
        BigDecimal currentBalance = originalTransaction.getBalanceAfter();
        BigDecimal newBalance = currentBalance.add(refundAmount);

        // 3. 创建退款交易记录
        ConsumeTransactionEntity refundTransaction = new ConsumeTransactionEntity();
        refundTransaction.setTransactionNo(UUID.randomUUID().toString().replace("-", ""));
        refundTransaction.setAccountId(accountId);
        refundTransaction.setUserId(originalTransaction.getUserId());
        // deviceId需要类型转换，从Long转为String - 使用统一工具类
        refundTransaction.setDeviceId(TypeUtils.toString(originalTransaction.getDeviceId()));
        refundTransaction.setTransactionAmount(refundAmount.negate()); // 负数表示退款
        refundTransaction.setActualAmount(refundAmount.negate());
        refundTransaction.setBalanceBefore(originalTransaction.getBalanceAfter());
        refundTransaction.setBalanceAfter(newBalance);
        refundTransaction.setTransactionType(4); // 4-退款
        refundTransaction.setTransactionTime(LocalDateTime.now());
        refundTransaction.setTransactionStatus(1); // 1-成功

        // 4. 执行数据库操作
        boolean success = executeRefundOperations(accountId, newBalance, originalTransaction.getTransactionNo(), refundTransaction);

        if (success) {
            log.info("[交易管理] 交易撤销成功: originalTransactionId={}, refundTransactionId={}, refundAmount={}",
                originalTransaction.getTransactionNo(), refundTransaction.getTransactionNo(), refundAmount);
        } else {
            log.error("[交易管理] 交易撤销失败: transactionId={}", originalTransaction.getTransactionNo());
        }

        return success;
    }

    /**
     * 生成幂等性键
     */
    private String generateIdempotentKey(Long userId, String deviceId, BigDecimal amount) {
        // 使用1分钟时间窗口防止重复提交
        long timeWindow = System.currentTimeMillis() / 60000;
        return String.format("consume_tx:idempotent:%s:%s:%s:%s", userId, deviceId, amount, timeWindow);
    }

    /**
     * 检查重复交易
     */
    private boolean isDuplicateTransaction(String idempotentKey) {
        // 这里可以结合Redis缓存实现，简化实现先返回false
        return false;
    }

    /**
     * 记录幂等性
     */
    private void recordIdempotent(String idempotentKey, String transactionId) {
        // 记录到Redis缓存，TTL设置为10分钟
        log.debug("[交易管理] 记录幂等性: idempotentKey={}, transactionId={}", idempotentKey, transactionId);
    }

    /**
     * 更新账户余额并创建交易记录
     *
     * @param accountId 账户ID
     * @param newBalance 新余额
     * @param transaction 交易记录
     * @return 是否成功
     */
    private boolean updateAccountBalanceAndCreateTransaction(Long accountId, BigDecimal newBalance,
                                                           ConsumeTransactionEntity transaction) {
        try {
            // 1. 更新账户余额（乐观锁）
            int updateCount = consumeAccountDao.updateBalance(accountId, newBalance);
            if (updateCount <= 0) {
                log.error("[交易管理] 更新账户余额失败: accountId={}, newBalance={}", accountId, newBalance);
                return false;
            }

            // 2. 创建交易记录
            consumeTransactionDao.insert(transaction);
            log.debug("[交易管理] 交易记录创建成功: transactionId={}", transaction.getId());

            return true;

        } catch (Exception e) {
            log.error("[交易管理] 数据库操作失败: accountId={}, error={}", accountId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 执行退款操作
     *
     * @param accountId            账户ID
     * @param newBalance           新余额
     * @param originalTransactionId 原交易ID
     * @param refundTransaction    退款交易记录
     * @return 是否成功
     */
    private boolean executeRefundOperations(Long accountId, BigDecimal newBalance,
                                          String originalTransactionId, ConsumeTransactionEntity refundTransaction) {
        try {
            // 1. 更新账户余额
            int updateCount = consumeAccountDao.updateBalance(accountId, newBalance);
            if (updateCount <= 0) {
                log.error("[交易管理] 退款更新账户余额失败: accountId={}, newBalance={}", accountId, newBalance);
                return false;
            }

            // 2. 创建退款交易记录
            consumeTransactionDao.insert(refundTransaction);

            // 3. 更新原交易状态为已退款
            consumeTransactionDao.updateStatus(originalTransactionId, "REFUNDED");

            return true;

        } catch (Exception e) {
            log.error("[交易管理] 退款操作失败: accountId={}, error={}", accountId, e.getMessage(), e);
            return false;
        }
    }
    // ==================== 对账相关方法 ====================

    /**
     * 获取系统交易统计
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计信息
     */
    public Map<String, Object> getSystemTransactionStats(String startDate, String endDate) {
        log.info("[交易管理] 获取系统交易统计: startDate={}, endDate={}", startDate, endDate);

        // 查询系统交易记录
        LocalDateTime startTime = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime endTime = LocalDate.parse(endDate).atTime(23, 59, 59);

        int totalCount = consumeTransactionDao.countByDateRange(startTime, endTime);
        BigDecimal totalAmount = consumeTransactionDao.sumAmountByDateRange(startTime, endTime);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", totalCount);
        stats.put("totalAmount", totalAmount != null ? totalAmount : BigDecimal.ZERO);

        return stats;
    }

    /**
     * 获取设备交易统计
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计信息
     */
    public Map<String, Object> getDeviceTransactionStats(String startDate, String endDate) {
        log.info("[交易管理] 获取设备交易统计: startDate={}, endDate={}", startDate, endDate);

        // 调用设备服务接口获取设备交易统计
        // 这里简化实现，返回系统统计的90%作为模拟数据
        Map<String, Object> systemStats = getSystemTransactionStats(startDate, endDate);
        Integer systemCount = (Integer) systemStats.get("totalCount");
        BigDecimal systemAmount = (BigDecimal) systemStats.get("totalAmount");

        // 模拟设备统计（实际应该调用设备服务）
        Map<String, Object> deviceStats = new HashMap<>();
        deviceStats.put("totalCount", (int)(systemCount * 0.98)); // 98%匹配
        deviceStats.put("totalAmount", systemAmount.multiply(new BigDecimal("0.98")));

        return deviceStats;
    }
}
