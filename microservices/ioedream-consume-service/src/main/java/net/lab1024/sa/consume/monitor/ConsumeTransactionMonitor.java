package net.lab1024.sa.consume.monitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import net.lab1024.sa.consume.domain.vo.ConsumeTransactionVO;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 消费交易监控组件
 * <p>
 * 负责监控消费交易的各项指标，包括：
 * - 交易总量统计
 * - 交易金额统计
 * - 交易成功率监控
 * - 异常交易告警
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-22
 */
@Slf4j
@Component
public class ConsumeTransactionMonitor {

    /**
     * 交易计数器
     */
    private final AtomicLong transactionCount = new AtomicLong(0);

    /**
     * 成功交易计数器
     */
    private final AtomicLong successCount = new AtomicLong(0);

    /**
     * 失败交易计数器
     */
    private final AtomicLong failureCount = new AtomicLong(0);

    /**
     * 交易总金额
     */
    private final AtomicLong totalAmount = new AtomicLong(0);

    /**
     * 实时交易统计
     */
    private final ConcurrentHashMap<String, Object> realTimeStats = new ConcurrentHashMap<>();

    /**
     * 记录交易成功
     *
     * @param transactionId 交易ID
     * @param amount        交易金额
     * @param userId        用户ID
     */
    public void recordSuccess(String transactionId, BigDecimal amount, Long userId) {
        transactionCount.incrementAndGet();
        successCount.incrementAndGet();
        totalAmount.addAndGet(amount.multiply(BigDecimal.valueOf(100)).longValue()); // 转换为分

        log.info("[交易监控] 记录成功交易: transactionId={}, amount={}, userId={}",
                transactionId, amount, userId);

        // 更新实时统计
        updateRealTimeStats();
    }

    /**
     * 记录交易失败
     *
     * @param transactionId 交易ID
     * @param reason        失败原因
     * @param userId        用户ID
     */
    public void recordFailure(String transactionId, String reason, Long userId) {
        transactionCount.incrementAndGet();
        failureCount.incrementAndGet();

        log.warn("[交易监控] 记录失败交易: transactionId={}, reason={}, userId={}",
                transactionId, reason, userId);

        // 更新实时统计
        updateRealTimeStats();
    }

    /**
     * 获取交易统计信息
     *
     * @return 统计信息
     */
    public TransactionStatistics getStatistics() {
        TransactionStatistics stats = new TransactionStatistics();
        stats.setTotalTransactions(transactionCount.get());
        stats.setSuccessTransactions(successCount.get());
        stats.setFailureTransactions(failureCount.get());
        stats.setTotalAmount(BigDecimal.valueOf(totalAmount.get()).divide(BigDecimal.valueOf(100)));
        stats.setSuccessRate(calculateSuccessRate());
        stats.setLastUpdateTime(LocalDateTime.now());
        return stats;
    }

    /**
     * 重置统计数据
     */
    public void resetStatistics() {
        transactionCount.set(0);
        successCount.set(0);
        failureCount.set(0);
        totalAmount.set(0);
        realTimeStats.clear();

        log.info("[交易监控] 重置统计数据");
    }

    /**
     * 检查交易异常
     *
     * @param threshold 异常阈值
     * @return 是否异常
     */
    public boolean checkAnomaly(double threshold) {
        double currentFailureRate = calculateFailureRate();
        boolean isAnomaly = currentFailureRate > threshold;

        if (isAnomaly) {
            log.error("[交易监控] 检测到交易异常: 当前失败率={}%, 阈值={}%",
                    currentFailureRate * 100, threshold * 100);
        }

        return isAnomaly;
    }

    /**
     * 计算成功率
     *
     * @return 成功率
     */
    private double calculateSuccessRate() {
        long total = transactionCount.get();
        if (total == 0) {
            return 0.0;
        }
        return (double) successCount.get() / total;
    }

    /**
     * 计算失败率
     *
     * @return 失败率
     */
    private double calculateFailureRate() {
        long total = transactionCount.get();
        if (total == 0) {
            return 0.0;
        }
        return (double) failureCount.get() / total;
    }

    /**
     * 记录交易失败（详细信息）
     *
     * @param errorCode 错误代码
     * @param errorMsg  错误信息
     * @param amount    交易金额
     * @param deviceId  设备ID
     */
    public void recordTransactionFailure(String errorCode, String errorMsg, BigDecimal amount, String deviceId) {
        failureCount.incrementAndGet();
        transactionCount.incrementAndGet();

        log.warn("[交易监控] 交易失败: errorCode={}, errorMsg={}, amount={}, deviceId={}",
                errorCode, errorMsg, amount, deviceId);

        updateRealTimeStats();
    }

    /**
     * 记录账户查询时间
     *
     * @param duration   查询耗时
     * @param accountId  账户ID
     */
    public void recordAccountQueryTime(Duration duration, Long accountId) {
        log.debug("[交易监控] 账户查询耗时: duration={}, accountId={}", duration, accountId);

        // 可以添加性能统计逻辑
        if (duration.toMillis() > 100) {
            log.warn("[交易监控] 账户查询耗时过长: duration={}ms, accountId={}", duration.toMillis(), accountId);
        }
    }

    /**
     * 记录重复交易
     *
     * @param transactionId 交易ID
     * @param reason        重复原因
     */
    public void recordTransactionDuplicate(String transactionId, String reason) {
        log.warn("[交易监控] 检测到重复交易: transactionId={}, reason={}", transactionId, reason);

        // 可以添加重复交易统计
    }

    /**
     * 记录交易成功（详细信息）
     *
     * @param transactionVO 交易信息
     */
    public void recordTransactionSuccess(ConsumeTransactionVO transactionVO) {
        if (transactionVO != null) {
            successCount.incrementAndGet();
            transactionCount.incrementAndGet();

            log.info("[交易监控] 交易成功: transactionId={}, amount={}, userId={}",
                    transactionVO.getTransactionId(), transactionVO.getAmount(), transactionVO.getUserId());

            updateRealTimeStats();
        }
    }

    /**
     * 记录交易处理时长
     *
     * @param duration     处理时长
     * @param operation    操作类型
     */
    public void recordTransactionDuration(Duration duration, String operation) {
        log.debug("[交易监控] 操作耗时: operation={}, duration={}ms", operation, duration.toMillis());

        // 性能监控
        if (duration.toMillis() > 1000) {
            log.warn("[交易监控] 操作耗时过长: operation={}, duration={}ms", operation, duration.toMillis());
        }
    }

    /**
     * 更新实时统计
     */
    private void updateRealTimeStats() {
        realTimeStats.put("totalTransactions", transactionCount.get());
        realTimeStats.put("successRate", calculateSuccessRate());
        realTimeStats.put("lastUpdateTime", LocalDateTime.now());
    }

    /**
     * 交易统计数据类
     */
    public static class TransactionStatistics {
        private Long totalTransactions;
        private Long successTransactions;
        private Long failureTransactions;
        private BigDecimal totalAmount;
        private Double successRate;
        private LocalDateTime lastUpdateTime;

        // Getters and Setters
        public Long getTotalTransactions() {
            return totalTransactions;
        }

        public void setTotalTransactions(Long totalTransactions) {
            this.totalTransactions = totalTransactions;
        }

        public Long getSuccessTransactions() {
            return successTransactions;
        }

        public void setSuccessTransactions(Long successTransactions) {
            this.successTransactions = successTransactions;
        }

        public Long getFailureTransactions() {
            return failureTransactions;
        }

        public void setFailureTransactions(Long failureTransactions) {
            this.failureTransactions = failureTransactions;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
        }

        public Double getSuccessRate() {
            return successRate;
        }

        public void setSuccessRate(Double successRate) {
            this.successRate = successRate;
        }

        public LocalDateTime getLastUpdateTime() {
            return lastUpdateTime;
        }

        public void setLastUpdateTime(LocalDateTime lastUpdateTime) {
            this.lastUpdateTime = lastUpdateTime;
        }
    }
}