package net.lab1024.sa.attendance.engine.optimization;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 考勤规则性能监控器
 * <p>
 * 监控规则执行性能，提供性能统计、告警和优化建议
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-12-26
 */
@Slf4j
@Component
public class AttendanceRulePerformanceMonitor {

    /**
     * 性能指标
     */
    public static class PerformanceMetrics {
        private Long ruleId;
        private String ruleName;
        private Long totalExecutions;
        private Long successExecutions;
        private Long failureExecutions;
        private Long totalExecutionTime; // 总执行时间（毫秒）
        private Long minExecutionTime;
        private Long maxExecutionTime;
        private Double averageExecutionTime;
        private Long p50ExecutionTime; // 中位数
        private Long p95ExecutionTime; // 95分位
        private Long p99ExecutionTime; // 99分位
        private Double successRate;
        private LocalDateTime lastExecutionTime;
        private LocalDateTime firstExecutionTime;

        // Getters and Setters
        public Long getRuleId() { return ruleId; }
        public void setRuleId(Long ruleId) { this.ruleId = ruleId; }

        public String getRuleName() { return ruleName; }
        public void setRuleName(String ruleName) { this.ruleName = ruleName; }

        public Long getTotalExecutions() { return totalExecutions; }
        public void setTotalExecutions(Long totalExecutions) { this.totalExecutions = totalExecutions; }

        public Long getSuccessExecutions() { return successExecutions; }
        public void setSuccessExecutions(Long successExecutions) { this.successExecutions = successExecutions; }

        public Long getFailureExecutions() { return failureExecutions; }
        public void setFailureExecutions(Long failureExecutions) { this.failureExecutions = failureExecutions; }

        public Long getTotalExecutionTime() { return totalExecutionTime; }
        public void setTotalExecutionTime(Long totalExecutionTime) { this.totalExecutionTime = totalExecutionTime; }

        public Long getMinExecutionTime() { return minExecutionTime; }
        public void setMinExecutionTime(Long minExecutionTime) { this.minExecutionTime = minExecutionTime; }

        public Long getMaxExecutionTime() { return maxExecutionTime; }
        public void setMaxExecutionTime(Long maxExecutionTime) { this.maxExecutionTime = maxExecutionTime; }

        public Double getAverageExecutionTime() { return averageExecutionTime; }
        public void setAverageExecutionTime(Double averageExecutionTime) { this.averageExecutionTime = averageExecutionTime; }

        public Long getP50ExecutionTime() { return p50ExecutionTime; }
        public void setP50ExecutionTime(Long p50ExecutionTime) { this.p50ExecutionTime = p50ExecutionTime; }

        public Long getP95ExecutionTime() { return p95ExecutionTime; }
        public void setP95ExecutionTime(Long p95ExecutionTime) { this.p95ExecutionTime = p95ExecutionTime; }

        public Long getP99ExecutionTime() { return p99ExecutionTime; }
        public void setP99ExecutionTime(Long p99ExecutionTime) { this.p99ExecutionTime = p99ExecutionTime; }

        public Double getSuccessRate() { return successRate; }
        public void setSuccessRate(Double successRate) { this.successRate = successRate; }

        public LocalDateTime getLastExecutionTime() { return lastExecutionTime; }
        public void setLastExecutionTime(LocalDateTime lastExecutionTime) { this.lastExecutionTime = lastExecutionTime; }

        public LocalDateTime getFirstExecutionTime() { return firstExecutionTime; }
        public void setFirstExecutionTime(LocalDateTime firstExecutionTime) { this.firstExecutionTime = firstExecutionTime; }
    }

    /**
     * 性能告警
     */
    public static class PerformanceAlert {
        private Long alertId;
        private Long ruleId;
        private String alertType; // SLOW_EXECUTION, HIGH_FAILURE_RATE, TIMEOUT
        private String alertMessage;
        private String severity; // LOW, MEDIUM, HIGH, CRITICAL
        private LocalDateTime alertTime;
        private Map<String, Object> alertData;

        // Getters and Setters
        public Long getAlertId() { return alertId; }
        public void setAlertId(Long alertId) { this.alertId = alertId; }

        public Long getRuleId() { return ruleId; }
        public void setRuleId(Long ruleId) { this.ruleId = ruleId; }

        public String getAlertType() { return alertType; }
        public void setAlertType(String alertType) { this.alertType = alertType; }

        public String getAlertMessage() { return alertMessage; }
        public void setAlertMessage(String alertMessage) { this.alertMessage = alertMessage; }

        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }

        public LocalDateTime getAlertTime() { return alertTime; }
        public void setAlertTime(LocalDateTime alertTime) { this.alertTime = alertTime; }

        public Map<String, Object> getAlertData() { return alertData; }
        public void setAlertData(Map<String, Object> alertData) { this.alertData = alertData; }
    }

    // 执行记录存储
    private final Map<Long, List<Long>> executionTimes = new ConcurrentHashMap<>();
    private final Map<Long, AtomicLong> successCount = new ConcurrentHashMap<>();
    private final Map<Long, AtomicLong> failureCount = new ConcurrentHashMap<>();
    private final Map<Long, LocalDateTime> lastExecutionTime = new ConcurrentHashMap<>();
    private final Map<Long, LocalDateTime> firstExecutionTime = new ConcurrentHashMap<>();

    // 告警存储
    private final List<PerformanceAlert> alerts = new ArrayList<>();
    private Long alertIdSequence = 1L;

    // 性能阈值配置
    private final Map<String, Long> performanceThresholds = new ConcurrentHashMap<>();

    public AttendanceRulePerformanceMonitor() {
        // 默认性能阈值
        performanceThresholds.put("slowExecutionTime", 1000L); // 慢执行阈值：1秒
        performanceThresholds.put("highFailureRate", 10L); // 高失败率阈值：10%
        performanceThresholds.put("timeout", 5000L); // 超时阈值：5秒
    }

    /**
     * 记录规则执行
     *
     * @param ruleId 规则ID
     * @param ruleName 规则名称
     * @param executionTime 执行时间（毫秒）
     * @param success 是否成功
     */
    public void recordExecution(Long ruleId, String ruleName, Long executionTime, Boolean success) {
        log.debug("[规则性能监控] 记录执行: ruleId={}, ruleName={}, executionTime={}ms, success={}",
                ruleId, ruleName, executionTime, success);

        // 1. 记录执行时间
        executionTimes.computeIfAbsent(ruleId, k -> new ArrayList<>()).add(executionTime);

        // 2. 更新成功/失败计数
        if (success) {
            successCount.computeIfAbsent(ruleId, k -> new AtomicLong(0)).incrementAndGet();
        } else {
            failureCount.computeIfAbsent(ruleId, k -> new AtomicLong(0)).incrementAndGet();
        }

        // 3. 更新最后执行时间
        lastExecutionTime.put(ruleId, LocalDateTime.now());

        // 4. 更新首次执行时间
        firstExecutionTime.putIfAbsent(ruleId, LocalDateTime.now());

        // 5. 检查性能告警
        checkPerformanceAlerts(ruleId, ruleName, executionTime, success);
    }

    /**
     * 检查性能告警
     */
    private void checkPerformanceAlerts(Long ruleId, String ruleName, Long executionTime, Boolean success) {
        // 检查慢执行
        if (executionTime > performanceThresholds.get("slowExecutionTime")) {
            createAlert(ruleId, "SLOW_EXECUTION", "规则执行慢",
                    executionTime > performanceThresholds.get("timeout") ? "HIGH" : "MEDIUM",
                    Map.of("executionTime", executionTime, "threshold", performanceThresholds.get("slowExecutionTime")));
        }

        // 检查超时
        if (executionTime > performanceThresholds.get("timeout")) {
            createAlert(ruleId, "TIMEOUT", "规则执行超时", "CRITICAL",
                    Map.of("executionTime", executionTime, "threshold", performanceThresholds.get("timeout")));
        }

        // 检查高失败率
        PerformanceMetrics metrics = getPerformanceMetrics(ruleId, ruleName);
        if (metrics != null && metrics.getTotalExecutions() > 10) {
            if (metrics.getSuccessRate() < performanceThresholds.get("highFailureRate")) {
                createAlert(ruleId, "HIGH_FAILURE_RATE", "规则失败率过高",
                        metrics.getSuccessRate() < 5 ? "CRITICAL" : "HIGH",
                        Map.of("successRate", metrics.getSuccessRate(),
                               "threshold", performanceThresholds.get("highFailureRate")));
            }
        }
    }

    /**
     * 创建告警
     */
    private void createAlert(Long ruleId, String alertType, String message, String severity, Map<String, Object> data) {
        PerformanceAlert alert = new PerformanceAlert();
        alert.setAlertId(alertIdSequence++);
        alert.setRuleId(ruleId);
        alert.setAlertType(alertType);
        alert.setAlertMessage(message);
        alert.setSeverity(severity);
        alert.setAlertTime(LocalDateTime.now());
        alert.setAlertData(data);

        alerts.add(alert);

        log.warn("[规则性能监控] 性能告警: ruleId={}, type={}, message={}, severity={}",
                ruleId, alertType, message, severity);
    }

    /**
     * 获取性能指标
     *
     * @param ruleId 规则ID
     * @param ruleName 规则名称
     * @return 性能指标
     */
    public PerformanceMetrics getPerformanceMetrics(Long ruleId, String ruleName) {
        PerformanceMetrics metrics = new PerformanceMetrics();
        metrics.setRuleId(ruleId);
        metrics.setRuleName(ruleName);

        List<Long> times = executionTimes.get(ruleId);
        if (times == null || times.isEmpty()) {
            return metrics;
        }

        // 基本统计
        Long totalSuccess = successCount.getOrDefault(ruleId, new AtomicLong(0)).get();
        Long totalFailure = failureCount.getOrDefault(ruleId, new AtomicLong(0)).get();
        Long totalExec = totalSuccess + totalFailure;

        metrics.setTotalExecutions(totalExec);
        metrics.setSuccessExecutions(totalSuccess);
        metrics.setFailureExecutions(totalFailure);
        metrics.setSuccessRate(totalExec > 0 ? (totalSuccess * 100.0 / totalExec) : 0.0);

        // 执行时间统计
        List<Long> sortedTimes = times.stream().sorted().collect(Collectors.toList());
        metrics.setMinExecutionTime(sortedTimes.get(0));
        metrics.setMaxExecutionTime(sortedTimes.get(sortedTimes.size() - 1));

        long sum = 0;
        for (Long time : times) {
            sum += time;
        }
        metrics.setTotalExecutionTime(sum);
        metrics.setAverageExecutionTime(sum * 1.0 / times.size());

        // 分位数计算
        metrics.setP50ExecutionTime(sortedTimes.get((int) (sortedTimes.size() * 0.5)));
        metrics.setP95ExecutionTime(sortedTimes.get((int) (sortedTimes.size() * 0.95)));
        metrics.setP99ExecutionTime(sortedTimes.get((int) (sortedTimes.size() * 0.99)));

        // 时间信息
        metrics.setLastExecutionTime(lastExecutionTime.get(ruleId));
        metrics.setFirstExecutionTime(firstExecutionTime.get(ruleId));

        return metrics;
    }

    /**
     * 获取所有性能指标
     *
     * @return 性能指标映射
     */
    public Map<Long, PerformanceMetrics> getAllPerformanceMetrics() {
        Map<Long, PerformanceMetrics> allMetrics = new HashMap<>();

        for (Long ruleId : executionTimes.keySet()) {
            PerformanceMetrics metrics = getPerformanceMetrics(ruleId, "Rule-" + ruleId);
            allMetrics.put(ruleId, metrics);
        }

        return allMetrics;
    }

    /**
     * 获取告警列表
     *
     * @param ruleId 规则ID（可选）
     * @param severity 告警级别（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 告警列表
     */
    public List<PerformanceAlert> getAlerts(Long ruleId, String severity, LocalDateTime startTime, LocalDateTime endTime) {
        return alerts.stream()
                .filter(alert -> ruleId == null || alert.getRuleId().equals(ruleId))
                .filter(alert -> severity == null || alert.getSeverity().equals(severity))
                .filter(alert -> startTime == null || alert.getAlertTime().isAfter(startTime))
                .filter(alert -> endTime == null || alert.getAlertTime().isBefore(endTime))
                .collect(Collectors.toList());
    }

    /**
     * 清除执行记录
     *
     * @param beforeTime 在此时间之前的记录
     */
    public void clearExecutionRecords(LocalDateTime beforeTime) {
        log.info("[规则性能监控] 清除执行记录: beforeTime={}", beforeTime);

        executionTimes.entrySet().removeIf(entry ->
                lastExecutionTime.get(entry.getKey()) != null &&
                lastExecutionTime.get(entry.getKey()).isBefore(beforeTime)
        );

        successCount.entrySet().removeIf(entry ->
                lastExecutionTime.get(entry.getKey()) != null &&
                lastExecutionTime.get(entry.getKey()).isBefore(beforeTime)
        );

        failureCount.entrySet().removeIf(entry ->
                lastExecutionTime.get(entry.getKey()) != null &&
                lastExecutionTime.get(entry.getKey()).isBefore(beforeTime)
        );

        lastExecutionTime.entrySet().removeIf(entry ->
                entry.getValue().isBefore(beforeTime)
        );

        firstExecutionTime.entrySet().removeIf(entry ->
                entry.getValue().isBefore(beforeTime)
        );

        log.info("[规则性能监控] 执行记录清除完成");
    }

    /**
     * 重置规则统计
     *
     * @param ruleId 规则ID
     */
    public void resetRuleStatistics(Long ruleId) {
        log.info("[规则性能监控] 重置规则统计: ruleId={}", ruleId);

        executionTimes.remove(ruleId);
        successCount.remove(ruleId);
        failureCount.remove(ruleId);
        lastExecutionTime.remove(ruleId);
        // firstExecutionTime保留，不重置

        log.info("[规则性能监控] 规则统计重置完成: ruleId={}", ruleId);
    }

    /**
     * 获取性能优化建议
     *
     * @param ruleId 规则ID
     * @return 优化建议列表
     */
    public List<String> getOptimizationSuggestions(Long ruleId) {
        List<String> suggestions = new ArrayList<>();

        PerformanceMetrics metrics = getPerformanceMetrics(ruleId, "Rule-" + ruleId);
        if (metrics == null || metrics.getTotalExecutions() == 0) {
            suggestions.add("暂无足够数据生成优化建议");
            return suggestions;
        }

        // 平均执行时间建议
        if (metrics.getAverageExecutionTime() > 500) {
            suggestions.add("平均执行时间较长，建议优化规则表达式逻辑");
        }

        // 成功率建议
        if (metrics.getSuccessRate() < 90) {
            suggestions.add("成功率较低，建议检查规则配置和数据源");
        }

        // P99执行时间建议
        if (metrics.getP99ExecutionTime() > 2000) {
            suggestions.add("P99执行时间过长，存在长尾效应，建议优化极端场景");
        }

        // 执行频率建议
        if (metrics.getTotalExecutions() > 10000) {
            long daysBetween = ChronoUnit.DAYS.between(
                    metrics.getFirstExecutionTime(),
                    metrics.getLastExecutionTime()
            );
            if (daysBetween > 0) {
                long dailyExecutions = metrics.getTotalExecutions() / daysBetween;
                if (dailyExecutions > 1000) {
                    suggestions.add("规则执行频率较高，建议增加缓存或预热规则");
                }
            }
        }

        return suggestions;
    }
}
