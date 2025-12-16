package net.lab1024.sa.attendance.engine.rule.model;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * 规则执行统计信息模型
 * <p>
 * 规则引擎执行性能和状态的详细统计
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleExecutionStatistics {

    /**
     * 统计开始时间
     */
    private Instant startTime;

    /**
     * 统计结束时间
     */
    private Instant endTime;

    /**
     * 总评估次数
     */
    private Long totalEvaluations;

    /**
     * 成功评估次数
     */
    private Long successEvaluations;

    /**
     * 失败评估次数
     */
    private Long failedEvaluations;

    /**
     * 缓存命中次数
     */
    private Long cacheHits;

    /**
     * 缓存未命中次数
     */
    private Long cacheMisses;

    /**
     * 平均评估时间（毫秒）
     */
    private Double averageEvaluationTime;

    /**
     * 最大评估时间（毫秒）
     */
    private Long maxEvaluationTime;

    /**
     * 最小评估时间（毫秒）
     */
    private Long minEvaluationTime;

    /**
     * 总规则数
     */
    private Long totalRules;

    /**
     * 活跃规则数
     */
    private Long activeRules;

    /**
     * 按规则类型统计
     */
    private Map<String, RuleTypeStatistics> ruleTypeStatistics;

    /**
     * 按规则分类统计
     */
    private Map<String, CategoryStatistics> categoryStatistics;

    /**
     * 按小时统计
     */
    private Map<String, HourlyStatistics> hourlyStatistics;

    /**
     * 性能指标
     */
    private PerformanceMetrics performanceMetrics;

    /**
     * 错误统计
     */
    private ErrorStatistics errorStatistics;

    /**
     * 系统资源使用情况
     */
    private ResourceUsage resourceUsage;

    /**
     * 计算成功率
     */
    public Double getSuccessRate() {
        if (totalEvaluations == null || totalEvaluations == 0) {
            return 0.0;
        }
        return (double) (successEvaluations != null ? successEvaluations : 0) / totalEvaluations * 100;
    }

    /**
     * 计算缓存命中率
     */
    public Double getCacheHitRate() {
        long totalCacheRequests = (cacheHits != null ? cacheHits : 0) + (cacheMisses != null ? cacheMisses : 0);
        if (totalCacheRequests == 0) {
            return 0.0;
        }
        return (double) (cacheHits != null ? cacheHits : 0) / totalCacheRequests * 100;
    }

    /**
     * 计算活跃率
     */
    public Double getActiveRate() {
        if (totalRules == null || totalRules == 0) {
            return 0.0;
        }
        return (double) (activeRules != null ? activeRules : 0) / totalRules * 100;
    }

    /**
     * 规则类型统计内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RuleTypeStatistics {
        private String ruleType;
        private Long evaluationCount;
        private Long successCount;
        private Long failureCount;
        private Double averageTime;
        private Double successRate;
    }

    /**
     * 分类统计内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryStatistics {
        private String category;
        private Long ruleCount;
        private Long evaluationCount;
        private Long successCount;
        private Long failureCount;
        private Double averageTime;
        private Double successRate;
    }

    /**
     * 按小时统计内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HourlyStatistics {
        private String hour;
        private Long evaluationCount;
        private Long successCount;
        private Long failureCount;
        private Double averageTime;
        private Double successRate;
    }

    /**
     * 性能指标内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceMetrics {
        private Double throughput; // 吞吐量（次/秒）
        private Double latencyP50; // 50%延迟
        private Double latencyP90; // 90%延迟
        private Double latencyP95; // 95%延迟
        private Double latencyP99; // 99%延迟
        private Double cpuUsage; // CPU使用率
        private Double memoryUsage; // 内存使用率
    }

    /**
     * 错误统计内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorStatistics {
        private Long totalErrors;
        private Map<String, Long> errorTypes; // 错误类型统计
        private Map<String, Long> errorCodes; // 错误码统计
        private Double errorRate; // 错误率
        private Long lastErrorTime; // 最后错误时间
    }

    /**
     * 系统资源使用情况内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourceUsage {
        private Double cpuUsage; // CPU使用率
        private Long memoryUsed; // 内存使用量（字节）
        private Long memoryTotal; // 总内存（字节）
        private Double memoryUsagePercent; // 内存使用百分比
        private Long diskUsed; // 磁盘使用量（字节）
        private Long diskTotal; // 总磁盘空间（字节）
        private Double diskUsagePercent; // 磁盘使用百分比
        private Long networkInbound; // 网络入流量（字节）
        private Long networkOutbound; // 网络出流量（字节）
    }

    /**
     * 创建默认统计对象
     */
    public static RuleExecutionStatistics createDefault() {
        return RuleExecutionStatistics.builder()
            .startTime(Instant.now())
            .totalEvaluations(0L)
            .successEvaluations(0L)
            .failedEvaluations(0L)
            .cacheHits(0L)
            .cacheMisses(0L)
            .averageEvaluationTime(0.0)
            .maxEvaluationTime(0L)
            .minEvaluationTime(Long.MAX_VALUE)
            .totalRules(0L)
            .activeRules(0L)
            .ruleTypeStatistics(new java.util.HashMap<>())
            .categoryStatistics(new java.util.HashMap<>())
            .hourlyStatistics(new java.util.HashMap<>())
            .performanceMetrics(PerformanceMetrics.builder().build())
            .errorStatistics(ErrorStatistics.builder().build())
            .resourceUsage(ResourceUsage.builder().build())
            .build();
    }

    /**
     * 获取统计摘要
     */
    public String getSummary() {
        return String.format(
            "规则执行统计 - 总评估: %d, 成功: %d, 失败: %d, 成功率: %.2f%%, " +
            "缓存命中率: %.2f%%, 平均耗时: %.2fms",
            totalEvaluations != null ? totalEvaluations : 0,
            successEvaluations != null ? successEvaluations : 0,
            failedEvaluations != null ? failedEvaluations : 0,
            getSuccessRate(),
            getCacheHitRate(),
            averageEvaluationTime != null ? averageEvaluationTime : 0.0
        );
    }
}