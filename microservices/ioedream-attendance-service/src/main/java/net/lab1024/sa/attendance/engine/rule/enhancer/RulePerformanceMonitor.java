package net.lab1024.sa.attendance.engine.rule.enhancer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 考勤规则性能监控器
 * <p>
 * 严格遵循四层架构规范（Controller→Service→Manager→DAO）
 * Manager层负责业务编排和复杂业务逻辑处理
 * </p>
 * <p>
 * 核心职责：
 * - 实时监控规则执行性能
 * - 收集详细的性能指标
 * - 识别性能瓶颈
 * - 生成性能报告
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Component
@Slf4j
public class RulePerformanceMonitor {

    // 性能指标存储
    private final Map<Long, RulePerformanceMetrics> ruleMetrics = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> globalCounters = new ConcurrentHashMap<>();

    // 慢查询阈值（毫秒）
    private static final long SLOW_QUERY_THRESHOLD_MS = 1000;
    private static final long VERY_SLOW_QUERY_THRESHOLD_MS = 3000;

    /**
     * 记录规则执行
     */
    public void recordRuleExecution(Long ruleId, boolean success, long duration, String result) {
        // 1. 获取或创建规则指标
        RulePerformanceMetrics metrics = ruleMetrics.computeIfAbsent(ruleId, k -> new RulePerformanceMetrics(ruleId));

        // 2. 更新执行次数
        metrics.incrementExecutionCount();

        // 3. 更新成功/失败次数
        if (success) {
            metrics.incrementSuccessCount();
        } else {
            metrics.incrementFailureCount();
        }

        // 4. 更新执行时间
        metrics.addExecutionTime(duration);

        // 5. 检查慢查询
        if (duration > VERY_SLOW_QUERY_THRESHOLD_MS) {
            metrics.incrementVerySlowCount();
            log.warn("[性能监控] 检测到极慢规则执行: ruleId={}, duration={}ms, result={}",
                    ruleId, duration, result);
        } else if (duration > SLOW_QUERY_THRESHOLD_MS) {
            metrics.incrementSlowCount();
            log.warn("[性能监控] 检测到慢规则执行: ruleId={}, duration={}ms, result={}",
                    ruleId, duration, result);
        }

        // 6. 更新全局计数器
        incrementGlobalCounter("total_executions");
        if (success) {
            incrementGlobalCounter("total_success");
        } else {
            incrementGlobalCounter("total_failures");
        }

        log.debug("[性能监控] 记录规则执行: ruleId={}, success={}, duration={}ms",
                ruleId, success, duration);
    }

    /**
     * 记录规则缓存命中
     */
    public void recordCacheHit(Long ruleId) {
        RulePerformanceMetrics metrics = ruleMetrics.get(ruleId);
        if (metrics != null) {
            metrics.incrementCacheHits();
        }

        incrementGlobalCounter("total_cache_hits");
    }

    /**
     * 记录规则缓存未命中
     */
    public void recordCacheMiss(Long ruleId) {
        RulePerformanceMetrics metrics = ruleMetrics.get(ruleId);
        if (metrics != null) {
            metrics.incrementCacheMisses();
        }

        incrementGlobalCounter("total_cache_misses");
    }

    /**
     * 获取规则性能指标
     */
    public RulePerformanceMetrics getRuleMetrics(Long ruleId) {
        return ruleMetrics.get(ruleId);
    }

    /**
     * 获取所有规则性能指标
     */
    public Collection<RulePerformanceMetrics> getAllMetrics() {
        return ruleMetrics.values();
    }

    /**
     * 获取性能最差的规则
     */
    public List<RulePerformanceMetrics> getSlowestRules(int limit) {
        return ruleMetrics.values().stream()
                .sorted((m1, m2) -> Long.compare(m2.getAverageExecutionTime(), m1.getAverageExecutionTime()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 获取失败率最高的规则
     */
    public List<RulePerformanceMetrics> getHighestFailureRateRules(int limit) {
        return ruleMetrics.values().stream()
                .filter(m -> m.getExecutionCount() > 10) // 至少执行10次
                .sorted((m1, m2) -> Double.compare(m2.getFailureRate(), m1.getFailureRate()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 获取缓存命中率最低的规则
     */
    public List<RulePerformanceMetrics> getLowestCacheHitRateRules(int limit) {
        return ruleMetrics.values().stream()
                .filter(m -> m.getTotalCacheAccesses() > 10) // 至少10次缓存访问
                .sorted((m1, m2) -> Double.compare(m1.getCacheHitRate(), m2.getCacheHitRate()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 生成性能报告
     */
    public PerformanceReport generatePerformanceReport() {
        PerformanceReport report = new PerformanceReport();
        report.setReportTime(LocalDateTime.now());
        report.setTotalExecutions(getGlobalCounterValue("total_executions"));
        report.setTotalSuccess(getGlobalCounterValue("total_success"));
        report.setTotalFailures(getGlobalCounterValue("total_failures"));
        report.setTotalCacheHits(getGlobalCounterValue("total_cache_hits"));
        report.setTotalCacheMisses(getGlobalCounterValue("total_cache_misses"));

        // 计算全局缓存命中率
        long totalCacheAccesses = report.getTotalCacheHits() + report.getTotalCacheMisses();
        double globalCacheHitRate = totalCacheAccesses > 0 ?
            (report.getTotalCacheHits() * 100.0 / totalCacheAccesses) : 0.0;
        report.setGlobalCacheHitRate(Math.round(globalCacheHitRate * 100.0) / 100.0);

        // 计算全局成功率
        double globalSuccessRate = report.getTotalExecutions() > 0 ?
            (report.getTotalSuccess() * 100.0 / report.getTotalExecutions()) : 0.0;
        report.setGlobalSuccessRate(Math.round(globalSuccessRate * 100.0) / 100.0);

        // 添加性能最差的规则
        report.setSlowestRules(getSlowestRules(5));
        report.setHighestFailureRateRules(getHighestFailureRateRules(5));
        report.setLowestCacheHitRateRules(getLowestCacheHitRateRules(5));

        log.info("[性能监控] 生成性能报告: 总执行次数={}, 成功率={}%, 缓存命中率={}%",
                report.getTotalExecutions(), report.getGlobalSuccessRate(), report.getGlobalCacheHitRate());

        return report;
    }

    /**
     * 清除所有性能指标
     */
    public void clearAllMetrics() {
        log.info("[性能监控] 清除所有性能指标");
        ruleMetrics.clear();
        globalCounters.clear();
    }

    /**
     * 清除指定规则的性能指标
     */
    public void clearRuleMetrics(Long ruleId) {
        log.info("[性能监控] 清除规则性能指标: ruleId={}", ruleId);
        ruleMetrics.remove(ruleId);
    }

    /**
     * 增加全局计数器
     */
    private void incrementGlobalCounter(String key) {
        globalCounters.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
    }

    /**
     * 获取全局计数器值
     */
    private long getGlobalCounterValue(String key) {
        AtomicLong counter = globalCounters.get(key);
        return counter != null ? counter.get() : 0L;
    }

    // ==================== 内部类 ====================

    /**
     * 规则性能指标
     */
    public static class RulePerformanceMetrics {
        private final Long ruleId;
        private final AtomicLong executionCount = new AtomicLong(0);
        private final AtomicLong successCount = new AtomicLong(0);
        private final AtomicLong failureCount = new AtomicLong(0);
        private final AtomicLong totalExecutionTime = new AtomicLong(0);
        private final AtomicLong slowCount = new AtomicLong(0);
        private final AtomicLong verySlowCount = new AtomicLong(0);
        private final AtomicLong cacheHits = new AtomicLong(0);
        private final AtomicLong cacheMisses = new AtomicLong(0);

        public RulePerformanceMetrics(Long ruleId) {
            this.ruleId = ruleId;
        }

        public void incrementExecutionCount() {
            executionCount.incrementAndGet();
        }

        public void incrementSuccessCount() {
            successCount.incrementAndGet();
        }

        public void incrementFailureCount() {
            failureCount.incrementAndGet();
        }

        public void addExecutionTime(long duration) {
            totalExecutionTime.addAndGet(duration);
        }

        public void incrementSlowCount() {
            slowCount.incrementAndGet();
        }

        public void incrementVerySlowCount() {
            verySlowCount.incrementAndGet();
        }

        public void incrementCacheHits() {
            cacheHits.incrementAndGet();
        }

        public void incrementCacheMisses() {
            cacheMisses.incrementAndGet();
        }

        // Getters
        public Long getRuleId() {
            return ruleId;
        }

        public Long getExecutionCount() {
            return executionCount.get();
        }

        public Long getSuccessCount() {
            return successCount.get();
        }

        public Long getFailureCount() {
            return failureCount.get();
        }

        public Long getTotalExecutionTime() {
            return totalExecutionTime.get();
        }

        public Long getAverageExecutionTime() {
            long count = executionCount.get();
            return count > 0 ? totalExecutionTime.get() / count : 0;
        }

        public Long getSlowCount() {
            return slowCount.get();
        }

        public Long getVerySlowCount() {
            return verySlowCount.get();
        }

        public Long getTotalCacheAccesses() {
            return cacheHits.get() + cacheMisses.get();
        }

        public Long getCacheHits() {
            return cacheHits.get();
        }

        public Long getCacheMisses() {
            return cacheMisses.get();
        }

        public Double getCacheHitRate() {
            long total = getTotalCacheAccesses();
            return total > 0 ? (cacheHits.get() * 100.0 / total) : 0.0;
        }

        public Double getFailureRate() {
            long count = executionCount.get();
            return count > 0 ? (failureCount.get() * 100.0 / count) : 0.0;
        }
    }

    /**
     * 性能报告
     */
    public static class PerformanceReport {
        private LocalDateTime reportTime;
        private Long totalExecutions;
        private Long totalSuccess;
        private Long totalFailures;
        private Long totalCacheHits;
        private Long totalCacheMisses;
        private Double globalCacheHitRate;
        private Double globalSuccessRate;
        private List<RulePerformanceMetrics> slowestRules;
        private List<RulePerformanceMetrics> highestFailureRateRules;
        private List<RulePerformanceMetrics> lowestCacheHitRateRules;

        // Getters and Setters
        public LocalDateTime getReportTime() {
            return reportTime;
        }

        public void setReportTime(LocalDateTime reportTime) {
            this.reportTime = reportTime;
        }

        public Long getTotalExecutions() {
            return totalExecutions;
        }

        public void setTotalExecutions(Long totalExecutions) {
            this.totalExecutions = totalExecutions;
        }

        public Long getTotalSuccess() {
            return totalSuccess;
        }

        public void setTotalSuccess(Long totalSuccess) {
            this.totalSuccess = totalSuccess;
        }

        public Long getTotalFailures() {
            return totalFailures;
        }

        public void setTotalFailures(Long totalFailures) {
            this.totalFailures = totalFailures;
        }

        public Long getTotalCacheHits() {
            return totalCacheHits;
        }

        public void setTotalCacheHits(Long totalCacheHits) {
            this.totalCacheHits = totalCacheHits;
        }

        public Long getTotalCacheMisses() {
            return totalCacheMisses;
        }

        public void setTotalCacheMisses(Long totalCacheMisses) {
            this.totalCacheMisses = totalCacheMisses;
        }

        public Double getGlobalCacheHitRate() {
            return globalCacheHitRate;
        }

        public void setGlobalCacheHitRate(Double globalCacheHitRate) {
            this.globalCacheHitRate = globalCacheHitRate;
        }

        public Double getGlobalSuccessRate() {
            return globalSuccessRate;
        }

        public void setGlobalSuccessRate(Double globalSuccessRate) {
            this.globalSuccessRate = globalSuccessRate;
        }

        public List<RulePerformanceMetrics> getSlowestRules() {
            return slowestRules;
        }

        public void setSlowestRules(List<RulePerformanceMetrics> slowestRules) {
            this.slowestRules = slowestRules;
        }

        public List<RulePerformanceMetrics> getHighestFailureRateRules() {
            return highestFailureRateRules;
        }

        public void setHighestFailureRateRules(List<RulePerformanceMetrics> highestFailureRateRules) {
            this.highestFailureRateRules = highestFailureRateRules;
        }

        public List<RulePerformanceMetrics> getLowestCacheHitRateRules() {
            return lowestCacheHitRateRules;
        }

        public void setLowestCacheHitRateRules(List<RulePerformanceMetrics> lowestCacheHitRateRules) {
            this.lowestCacheHitRateRules = lowestCacheHitRateRules;
        }

        /**
         * 生成文本报告
         */
        public String generateTextReport() {
            StringBuilder sb = new StringBuilder();
            sb.append("\n==================== 考勤规则性能报告 ====================\n");
            sb.append(String.format("报告时间: %s\n", reportTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            sb.append(String.format("总执行次数: %d\n", totalExecutions));
            sb.append(String.format("成功次数: %d\n", totalSuccess));
            sb.append(String.format("失败次数: %d\n", totalFailures));
            sb.append(String.format("全局成功率: %.2f%%\n", globalSuccessRate));
            sb.append(String.format("缓存命中次数: %d\n", totalCacheHits));
            sb.append(String.format("缓存未命中次数: %d\n", totalCacheMisses));
            sb.append(String.format("缓存命中率: %.2f%%\n", globalCacheHitRate));

            if (slowestRules != null && !slowestRules.isEmpty()) {
                sb.append("\n【性能最差的规则】\n");
                for (int i = 0; i < slowestRules.size(); i++) {
                    RulePerformanceMetrics metrics = slowestRules.get(i);
                    sb.append(String.format("%d. 规则ID: %d, 平均执行时间: %dms, 执行次数: %d\n",
                            i + 1, metrics.getRuleId(), metrics.getAverageExecutionTime(), metrics.getExecutionCount()));
                }
            }

            if (highestFailureRateRules != null && !highestFailureRateRules.isEmpty()) {
                sb.append("\n【失败率最高的规则】\n");
                for (int i = 0; i < highestFailureRateRules.size(); i++) {
                    RulePerformanceMetrics metrics = highestFailureRateRules.get(i);
                    sb.append(String.format("%d. 规则ID: %d, 失败率: %.2f%%, 执行次数: %d\n",
                            i + 1, metrics.getRuleId(), metrics.getFailureRate(), metrics.getExecutionCount()));
                }
            }

            if (lowestCacheHitRateRules != null && !lowestCacheHitRateRules.isEmpty()) {
                sb.append("\n【缓存命中率最低的规则】\n");
                for (int i = 0; i < lowestCacheHitRateRules.size(); i++) {
                    RulePerformanceMetrics metrics = lowestCacheHitRateRules.get(i);
                    sb.append(String.format("%d. 规则ID: %d, 缓存命中率: %.2f%%\n",
                            i + 1, metrics.getRuleId(), metrics.getCacheHitRate()));
                }
            }

            sb.append("\n======================================================\n");
            return sb.toString();
        }
    }
}
