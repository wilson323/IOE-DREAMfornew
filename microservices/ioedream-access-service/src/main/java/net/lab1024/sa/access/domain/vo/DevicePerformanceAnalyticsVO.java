package net.lab1024.sa.access.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设备性能分析视图对象
 * <p>
 * 设备性能分析结果的数据传输对象
 * 严格遵循CLAUDE.md规范：
 * - 使用@Data注解
 * - 完整的字段文档注解
 * - 构建者模式支持
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "设备性能分析信息")
public class DevicePerformanceAnalyticsVO {

    /**
     * 设备ID
     */
    @Schema(description = "设备ID", example = "1001")
    private Long deviceId;

    /**
     * 设备名称
     */
    @Schema(description = "设备名称", example = "主入口门禁控制器")
    private String deviceName;

    /**
     * 分析时间范围
     */
    @Schema(description = "分析时间范围", example = "30天")
    private String analysisPeriod;

    /**
     * 平均响应时间（毫秒）
     */
    @Schema(description = "平均响应时间（毫秒）", example = "156")
    private Long averageResponseTime;

    /**
     * 最大响应时间（毫秒）
     */
    @Schema(description = "最大响应时间（毫秒）", example = "1250")
    private Long maxResponseTime;

    /**
     * 最小响应时间（毫秒）
     */
    @Schema(description = "最小响应时间（毫秒）", example = "45")
    private Long minResponseTime;

    /**
     * P95响应时间（毫秒）
     */
    @Schema(description = "P95响应时间（毫秒）", example = "320")
    private Long p95ResponseTime;

    /**
     * P99响应时间（毫秒）
     */
    @Schema(description = "P99响应时间（毫秒）", example = "580")
    private Long p99ResponseTime;

    /**
     * 总请求次数
     */
    @Schema(description = "总请求次数", example = "125680")
    private Long totalRequests;

    /**
     * 成功请求数
     */
    @Schema(description = "成功请求数", example = "125432")
    private Long successfulRequests;

    /**
     * 成功率（%）
     */
    @Schema(description = "成功率（%）", example = "99.81")
    private BigDecimal successRate;

    /**
     * 平均吞吐量（TPS）
     */
    @Schema(description = "平均吞吐量（TPS）", example = "45.3")
    private BigDecimal averageThroughput;

    /**
     * 峰值吞吐量（TPS）
     */
    @Schema(description = "峰值吞吐量（TPS）", example = "126.8")
    private BigDecimal peakThroughput;

    /**
     * 错误分布
     */
    @Schema(description = "错误分布")
    private Map<String, Long> errorDistribution;

    /**
     * 性能趋势数据
     */
    @Schema(description = "性能趋势数据")
    private List<PerformanceTrendVO> performanceTrends;

    /**
     * 负载分析
     */
    @Schema(description = "负载分析")
    private LoadAnalysisVO loadAnalysis;

    /**
     * 瓶颈分析
     */
    @Schema(description = "瓶颈分析")
    private List<BottleneckAnalysisVO> bottlenecks;

    /**
     * 优化建议
     */
    @Schema(description = "优化建议")
    private List<OptimizationRecommendationVO> recommendations;

    /**
     * 分析生成时间
     */
    @Schema(description = "分析生成时间", example = "2025-01-30T15:45:00")
    private LocalDateTime analysisTime;

    /**
     * 性能趋势内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "性能趋势")
    public static class PerformanceTrendVO {

        @Schema(description = "时间点", example = "2025-01-30T10:00:00")
        private LocalDateTime timestamp;

        @Schema(description = "响应时间", example = "156")
        private Long responseTime;

        @Schema(description = "吞吐量", example = "45.3")
        private BigDecimal throughput;

        @Schema(description = "错误率", example = "0.2")
        private BigDecimal errorRate;
    }

    /**
     * 负载分析内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "负载分析")
    public static class LoadAnalysisVO {

        @Schema(description = "平均CPU使用率", example = "35.6")
        private BigDecimal averageCpuUsage;

        @Schema(description = "峰值CPU使用率", example = "78.2")
        private BigDecimal peakCpuUsage;

        @Schema(description = "平均内存使用率", example = "42.3")
        private BigDecimal averageMemoryUsage;

        @Schema(description = "峰值内存使用率", example = "85.7")
        private BigDecimal peakMemoryUsage;

        @Schema(description = "平均网络带宽使用", example = "2.3")
        private BigDecimal averageNetworkUsage;

        @Schema(description = "峰值网络带宽使用", example = "8.9")
        private BigDecimal peakNetworkUsage;
    }

    /**
     * 瓶颈分析内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "瓶颈分析")
    public static class BottleneckAnalysisVO {

        @Schema(description = "瓶颈类型", example = "RESPONSE_TIME")
        private String bottleneckType;

        @Schema(description = "影响程度", example = "HIGH")
        private String impactLevel;

        @Schema(description = "出现频率", example = "15.3%")
        private BigDecimal frequency;

        @Schema(description = "瓶颈描述", example = "高峰期响应时间超过500ms")
        private String description;

        @Schema(description = "建议解决方案", example = "增加缓存层或优化数据库查询")
        private String solution;
    }

    /**
     * 优化建议内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "优化建议")
    public static class OptimizationRecommendationVO {

        @Schema(description = "建议类型", example = "PERFORMANCE")
        private String recommendationType;

        @Schema(description = "优先级", example = "HIGH")
        private String priority;

        @Schema(description = "预计改善幅度", example = "30%")
        private BigDecimal expectedImprovement;

        @Schema(description = "建议描述", example = "优化数据库索引可减少查询时间")
        private String description;

        @Schema(description = "实施难度", example = "LOW")
        private String implementationDifficulty;
    }
}