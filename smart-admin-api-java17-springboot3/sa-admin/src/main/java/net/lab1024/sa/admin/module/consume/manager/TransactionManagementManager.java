package net.lab1024.sa.admin.module.consume.manager;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import net.lab1024.sa.admin.module.consume.dao.ConsumeRecordDao;
import net.lab1024.sa.admin.module.consume.domain.entity.ConsumeRecordEntity;
import net.lab1024.sa.admin.module.consume.domain.entity.AccountEntity;
import net.lab1024.sa.admin.module.consume.dao.AccountDao;
import net.lab1024.sa.admin.module.consume.service.ConsumeCacheService;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.util.SmartDateUtil;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 交易管理核心功能管理器
 * 负责交易统计、对账、分区策略等核心交易管理功能
 *
 * @author SmartAdmin Team
 * @date 2025/11/25
 */
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Component
public class TransactionManagementManager {

    @Resource
    private ConsumeRecordDao consumeRecordDao;

    @Resource
    private AccountDao accountDao;

    @Resource
    private ConsumeCacheService consumeCacheService;

    // 缓存前缀
    private static final String STATISTICS_CACHE_PREFIX = "transaction_stats:";
    private static final String RECONCILIATION_CACHE_PREFIX = "reconciliation:";
    private static final String PARTITION_CACHE_PREFIX = "partition:";
    private static final long CACHE_EXPIRE_MINUTES = 10; // 统计缓存10分钟

    /**
     * 交易统计结果
     */
    public static class TransactionStatistics {
        private String period; // 统计周期
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private long totalCount;
        private BigDecimal totalAmount;
        private BigDecimal averageAmount;
        private Map<String, Long> modeDistribution; // 消费模式分布
        private Map<String, Long> paymentMethodDistribution; // 支付方式分布
        private Map<String, Long> hourlyDistribution; // 小时分布
        private Map<String, Long> dailyDistribution; // 日期分布

        // 构造函数和getter/setter
        public TransactionStatistics() {
            this.modeDistribution = new HashMap<>();
            this.paymentMethodDistribution = new HashMap<>();
            this.hourlyDistribution = new HashMap<>();
            this.dailyDistribution = new HashMap<>();
        }

        // Getters
        public String getPeriod() { return period; }
        public LocalDateTime getStartTime() { return startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public long getTotalCount() { return totalCount; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public BigDecimal getAverageAmount() { return averageAmount; }
        public Map<String, Long> getModeDistribution() { return modeDistribution; }
        public Map<String, Long> getPaymentMethodDistribution() { return paymentMethodDistribution; }
        public Map<String, Long> getHourlyDistribution() { return hourlyDistribution; }
        public Map<String, Long> getDailyDistribution() { return dailyDistribution; }

        // Setters
        public void setPeriod(String period) { this.period = period; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        public void setTotalCount(long totalCount) { this.totalCount = totalCount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        public void setAverageAmount(BigDecimal averageAmount) { this.averageAmount = averageAmount; }
    }

    /**
     * 对账结果
     */
    public static class ReconciliationResult {
        private String reconciliationDate;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private long systemTotalCount;
        private BigDecimal systemTotalAmount;
        private long thirdPartyTotalCount;
        private BigDecimal thirdPartyTotalAmount;
        private long mismatchedCount;
        private BigDecimal mismatchedAmount;
        private boolean reconciled;
        private List<String> errors;
        private Map<String, Object> details;

        // 构造函数
        public ReconciliationResult() {
            this.errors = new ArrayList<>();
            this.details = new HashMap<>();
        }

        // Getters
        public String getReconciliationDate() { return reconciliationDate; }
        public LocalDateTime getStartTime() { return startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public long getSystemTotalCount() { return systemTotalCount; }
        public BigDecimal getSystemTotalAmount() { return systemTotalAmount; }
        public long getThirdPartyTotalCount() { return thirdPartyTotalCount; }
        public BigDecimal getThirdPartyTotalAmount() { return thirdPartyTotalAmount; }
        public long getMismatchedCount() { return mismatchedCount; }
        public BigDecimal getMismatchedAmount() { return mismatchedAmount; }
        public boolean isReconciled() { return reconciled; }
        public List<String> getErrors() { return errors; }
        public Map<String, Object> getDetails() { return details; }

        // Setters
        public void setReconciled(boolean reconciled) { this.reconciled = reconciled; }
    }

    /**
     * 分区策略结果
     */
    public static class PartitionStrategyResult {
        private String strategyType;
        private Map<String, Object> strategyConfig;
        private List<String> partitions;
        private Map<String, String> partitionMapping;
        private boolean isActive;

        // 构造函数
        public PartitionStrategyResult() {
            this.strategyConfig = new HashMap<>();
            this.partitions = new ArrayList<>();
            this.partitionMapping = new HashMap<>();
        }

        // Getters
        public String getStrategyType() { return strategyType; }
        public Map<String, Object> getStrategyConfig() { return strategyConfig; }
        public List<String> getPartitions() { return partitions; }
        public Map<String, String> getPartitionMapping() { return partitionMapping; }
        public boolean isActive() { return isActive; }

        // Setters
        public void setStrategyType(String strategyType) { this.strategyType = strategyType; }
        public void setActive(boolean active) { this.isActive = active; }
    }

    /**
     * 获取交易统计
     */
    public TransactionStatistics getTransactionStatistics(String period, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            String cacheKey = STATISTICS_CACHE_PREFIX + period + ":" +
                             startTime.toString() + ":" + endTime.toString();

            // 尝试从缓存获取
            TransactionStatistics cachedStats = consumeCacheService.getValue(cacheKey, TransactionStatistics.class);
            if (cachedStats != null) {
                return cachedStats;
            }

            log.info("计算交易统计: period={}, startTime={}, endTime={}", period, startTime, endTime);

            TransactionStatistics stats = new TransactionStatistics();
            stats.setPeriod(period);
            stats.setStartTime(startTime);
            stats.setEndTime(endTime);

            // 1. 查询消费记录
            LambdaQueryWrapper<ConsumeRecordEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.between(ConsumeRecordEntity::getPayTime, startTime, endTime)
                   .eq(ConsumeRecordEntity::getStatus, "SUCCESS");

            List<ConsumeRecordEntity> records = consumeRecordDao.selectList(wrapper);

            if (records.isEmpty()) {
                log.info("统计周期内无交易记录");
                return stats;
            }

            // 2. 计算基础统计数据
            stats.setTotalCount(records.size());
            BigDecimal totalAmount = records.stream()
                    .map(ConsumeRecordEntity::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            stats.setTotalAmount(totalAmount);
            stats.setAverageAmount(totalAmount.divide(BigDecimal.valueOf(records.size()), 2, RoundingMode.HALF_UP));

            // 3. 计算消费模式分布
            Map<String, Long> modeStats = records.stream()
                    .collect(Collectors.groupingBy(
                        record -> record.getConsumeMode() != null ? record.getConsumeMode() : "UNKNOWN",
                        Collectors.counting()
                    ));
            stats.setModeDistribution(modeStats);

            // 4. 计算支付方式分布
            Map<String, Long> paymentStats = records.stream()
                    .collect(Collectors.groupingBy(
                        record -> record.getPayMethod() != null ? record.getPayMethod() : "UNKNOWN",
                        Collectors.counting()
                    ));
            stats.setPaymentMethodDistribution(paymentStats);

            // 5. 计算小时分布
            Map<String, Long> hourlyStats = records.stream()
                    .collect(Collectors.groupingBy(
                        record -> record.getPayTime().format(DateTimeFormatter.ofPattern("HH")),
                        Collectors.counting()
                    ));
            stats.setHourlyDistribution(hourlyStats);

            // 6. 计算日期分布
            Map<String, Long> dailyStats = records.stream()
                    .collect(Collectors.groupingBy(
                        record -> record.getPayTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        Collectors.counting()
                    ));
            stats.setDailyDistribution(dailyStats);

            // 7. 缓存统计结果
            consumeCacheService.setValue(cacheKey, stats, CACHE_EXPIRE_MINUTES * 60);

            log.info("交易统计计算完成: totalCount={}, totalAmount={}", stats.getTotalCount(), stats.getTotalAmount());
            return stats;

        } catch (Exception e) {
            log.error("获取交易统计失败: period={}, startTime={}, endTime={}", period, startTime, endTime, e);
            return new TransactionStatistics();
        }
    }

    /**
     * 执行对账
     */
    public ReconciliationResult performReconciliation(LocalDate date) {
        try {
            log.info("开始执行对账: date={}", date);

            String reconciliationDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
            String cacheKey = RECONCILIATION_CACHE_PREFIX + reconciliationDate;

            ReconciliationResult result = new ReconciliationResult();
            result.setReconciliationDate(reconciliationDate);
            result.setStartTime(date.atStartOfDay());
            result.setEndTime(date.atTime(LocalTime.MAX));

            // 1. 获取系统数据统计
            TransactionStatistics systemStats = getTransactionStatistics(
                "DAILY", date.atStartOfDay(), date.atTime(LocalTime.MAX));
            result.setSystemTotalCount(systemStats.getTotalCount());
            result.setSystemTotalAmount(systemStats.getTotalAmount());

            // 2. 模拟第三方支付平台数据（实际应该调用第三方API）
            Map<String, Object> thirdPartyData = getThirdPartyPaymentData(date);
            result.setThirdPartyTotalCount((Long) thirdPartyData.get("totalCount"));
            result.setThirdPartyTotalAmount((BigDecimal) thirdPartyData.get("totalAmount"));

            // 3. 执行对账比较
            boolean countMatch = result.getSystemTotalCount() == result.getThirdPartyTotalCount();
            boolean amountMatch = result.getSystemTotalAmount().compareTo(result.getThirdPartyTotalAmount()) == 0;

            if (countMatch && amountMatch) {
                result.setReconciled(true);
                result.getDetails().put("reconciliationStatus", "SUCCESS");
                result.getDetails().put("mismatchReason", null);
            } else {
                result.setReconciled(false);
                result.setMismatchedCount(Math.abs((int)(result.getSystemTotalCount() - result.getThirdPartyTotalCount())));
                result.setMismatchedAmount(result.getSystemTotalAmount().subtract(result.getThirdPartyTotalAmount()).abs());

                String mismatchReason = !countMatch ? "数量不匹配" : "";
                if (!amountMatch) {
                    mismatchReason += (mismatchReason.isEmpty() ? "" : "、") + "金额不匹配";
                }
                result.getDetails().put("mismatchReason", mismatchReason);
                result.getErrors().add("对账失败: " + mismatchReason);
            }

            // 4. 记录对账详情
            result.getDetails().put("systemData", Map.of(
                "count", result.getSystemTotalCount(),
                "amount", result.getSystemTotalAmount()
            ));
            result.getDetails().put("thirdPartyData", thirdPartyData);

            // 5. 缓存对账结果
            consumeCacheService.setValue(cacheKey, result, 24 * 60 * 60); // 缓存24小时

            log.info("对账完成: date={}, reconciled={}, systemCount={}, thirdPartyCount={}",
                    date, result.isReconciled(), result.getSystemTotalCount(), result.getThirdPartyTotalCount());

            return result;

        } catch (Exception e) {
            log.error("执行对账失败: date={}", date, e);
            ReconciliationResult result = new ReconciliationResult();
            result.setReconciled(false);
            result.getErrors().add("对账异常: " + e.getMessage());
            return result;
        }
    }

    /**
     * 获取分区策略
     */
    public PartitionStrategyResult getPartitionStrategy() {
        try {
            log.info("获取分区策略");

            String cacheKey = PARTITION_CACHE_PREFIX + "current_strategy";
            PartitionStrategyResult cachedStrategy = consumeCacheService.getValue(cacheKey, PartitionStrategyResult.class);
            if (cachedStrategy != null) {
                return cachedStrategy;
            }

            PartitionStrategyResult strategy = new PartitionStrategyResult();
            strategy.setStrategyType("DATE_RANGE"); // 按日期范围分区
            strategy.setActive(true);

            // 分区配置
            Map<String, Object> config = new HashMap<>();
            config.put("partitionKey", "pay_time");
            config.put("partitionFormat", "yyyy_MM");
            config.put("retentionMonths", 24); // 保留24个月
            config.put("autoCreate", true);
            strategy.setStrategyConfig(config);

            // 生成分区列表（过去12个月 + 未来3个月）
            List<String> partitions = new ArrayList<>();
            Map<String, String> partitionMapping = new HashMap<>();

            LocalDate currentDate = LocalDate.now();
            for (int i = -12; i <= 3; i++) {
                LocalDate partitionDate = currentDate.plusMonths(i);
                String partitionName = "p_" + partitionDate.format(DateTimeFormatter.ofPattern("yyyy_MM"));
                partitions.add(partitionName);
                partitionMapping.put(partitionName, partitionDate.toString());
            }

            strategy.setPartitions(partitions);
            strategy.setPartitionMapping(partitionMapping);

            // 缓存策略
            consumeCacheService.setValue(cacheKey, strategy, 60 * 60); // 缓存1小时

            log.info("分区策略获取完成: strategyType={}, partitionCount={}",
                    strategy.getStrategyType(), strategy.getPartitions().size());

            return strategy;

        } catch (Exception e) {
            log.error("获取分区策略失败", e);
            PartitionStrategyResult strategy = new PartitionStrategyResult();
            strategy.setStrategyType("ERROR");
            strategy.setActive(false);
            return strategy;
        }
    }

    /**
     * 获取用户交易统计
     */
    public Map<String, Object> getUserTransactionStatistics(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            Map<String, Object> stats = new HashMap<>();

            // 1. 查询用户交易记录
            LambdaQueryWrapper<ConsumeRecordEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ConsumeRecordEntity::getPersonId, userId)
                   .between(ConsumeRecordEntity::getPayTime, startTime, endTime)
                   .eq(ConsumeRecordEntity::getStatus, "SUCCESS");

            List<ConsumeRecordEntity> records = consumeRecordDao.selectList(wrapper);

            // 2. 计算基础统计
            long totalCount = records.size();
            BigDecimal totalAmount = records.stream()
                    .map(ConsumeRecordEntity::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal averageAmount = totalCount > 0 ?
                    totalAmount.divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

            // 3. 消费模式统计
            Map<String, Long> modeStats = records.stream()
                    .collect(Collectors.groupingBy(
                        record -> record.getConsumeMode() != null ? record.getConsumeMode() : "UNKNOWN",
                        Collectors.counting()
                    ));

            // 4. 支付方式统计
            Map<String, Long> paymentStats = records.stream()
                    .collect(Collectors.groupingBy(
                        record -> record.getPayMethod() != null ? record.getPayMethod() : "UNKNOWN",
                        Collectors.counting()
                    ));

            // 5. 时间分布统计
            Map<String, Long> hourlyStats = records.stream()
                    .collect(Collectors.groupingBy(
                        record -> record.getPayTime().format(DateTimeFormatter.ofPattern("HH")),
                        Collectors.counting()
                    ));

            // 6. 最大/最小消费金额
            Optional<BigDecimal> maxAmount = records.stream()
                    .map(ConsumeRecordEntity::getAmount)
                    .max(BigDecimal::compareTo);
            Optional<BigDecimal> minAmount = records.stream()
                    .map(ConsumeRecordEntity::getAmount)
                    .min(BigDecimal::compareTo);

            stats.put("userId", userId);
            stats.put("period", startTime.toString() + " 至 " + endTime.toString());
            stats.put("totalCount", totalCount);
            stats.put("totalAmount", totalAmount);
            stats.put("averageAmount", averageAmount);
            stats.put("maxAmount", maxAmount.orElse(BigDecimal.ZERO));
            stats.put("minAmount", minAmount.orElse(BigDecimal.ZERO));
            stats.put("modeDistribution", modeStats);
            stats.put("paymentMethodDistribution", paymentStats);
            stats.put("hourlyDistribution", hourlyStats);

            return stats;

        } catch (Exception e) {
            log.error("获取用户交易统计失败: userId={}", userId, e);
            return new HashMap<>();
        }
    }

    /**
     * 获取设备交易统计
     */
    public Map<String, Object> getDeviceTransactionStatistics(Long deviceId, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            Map<String, Object> stats = new HashMap<>();

            // 1. 查询设备交易记录
            LambdaQueryWrapper<ConsumeRecordEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ConsumeRecordEntity::getDeviceId, deviceId)
                   .between(ConsumeRecordEntity::getPayTime, startTime, endTime)
                   .eq(ConsumeRecordEntity::getStatus, "SUCCESS");

            List<ConsumeRecordEntity> records = consumeRecordDao.selectList(wrapper);

            // 2. 计算基础统计
            long totalCount = records.size();
            BigDecimal totalAmount = records.stream()
                    .map(ConsumeRecordEntity::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 3. 独立用户数
            long uniqueUserCount = records.stream()
                    .map(ConsumeRecordEntity::getPersonId)
                    .distinct()
                    .count();

            // 4. 平均每用户交易次数
            double avgTransactionsPerUser = uniqueUserCount > 0 ? (double) totalCount / uniqueUserCount : 0.0;

            stats.put("deviceId", deviceId);
            stats.put("period", startTime.toString() + " 至 " + endTime.toString());
            stats.put("totalCount", totalCount);
            stats.put("totalAmount", totalAmount);
            stats.put("uniqueUserCount", uniqueUserCount);
            stats.put("avgTransactionsPerUser", avgTransactionsPerUser);
            stats.put("avgAmount", totalCount > 0 ? totalAmount.divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO);

            return stats;

        } catch (Exception e) {
            log.error("获取设备交易统计失败: deviceId={}", deviceId, e);
            return new HashMap<>();
        }
    }

    /**
     * 模拟获取第三方支付平台数据（实际项目中应该调用真实的第三方API）
     */
    private Map<String, Object> getThirdPartyPaymentData(LocalDate date) {
        Map<String, Object> data = new HashMap<>();

        // 这里模拟返回数据，实际应该调用微信支付、支付宝等API
        data.put("totalCount", 0L);
        data.put("totalAmount", BigDecimal.ZERO);
        data.put("platform", "MOCK_THIRD_PARTY");
        data.put("apiCallTime", LocalDateTime.now().toString());

        return data;
    }

    /**
     * 清理过期的统计数据缓存
     */
    public void cleanupExpiredStatisticsCache() {
        try {
            log.info("清理过期的统计数据缓存");
            // TODO: 实现具体的缓存清理逻辑
            // 可以定期清理超过保留期的统计缓存数据
        } catch (Exception e) {
            log.error("清理统计缓存失败", e);
        }
    }

    /**
     * 预计算常用统计数据（可以定时任务调用）
     */
    public void precomputeCommonStatistics() {
        try {
            log.info("预计算常用统计数据");

            LocalDate today = LocalDate.now();

            // 预计算今日统计
            getTransactionStatistics("TODAY", today.atStartOfDay(), today.atTime(LocalTime.MAX));

            // 预计算本周统计
            LocalDateTime weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1).atStartOfDay();
            LocalDateTime weekEnd = today.atTime(LocalTime.MAX);
            getTransactionStatistics("WEEKLY", weekStart, weekEnd);

            // 预计算本月统计
            LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();
            LocalDateTime monthEnd = today.atTime(LocalTime.MAX);
            getTransactionStatistics("MONTHLY", monthStart, monthEnd);

            // 执行昨日对账
            if (today.getDayOfMonth() > 1) { // 避免月初对账第一天
                performReconciliation(today.minusDays(1));
            }

            log.info("预计算统计数据完成");

        } catch (Exception e) {
            log.error("预计算统计数据失败", e);
        }
    }
}