package net.lab1024.sa.attendance.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 规则性能测试详情视图对象
 * <p>
 * 包含响应时间分布、慢请求等详细信息
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@Builder(builderMethodName = "detailBuilder", builderClassName = "RulePerformanceTestDetailVOBuilder")
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "规则性能测试详情")
public class RulePerformanceTestDetailVO extends RulePerformanceTestResultVO {

    // ========== 响应时间分布 ==========

    @Schema(description = "响应时间分布（区间->数量）")
    private Map<String, Integer> responseTimeDistribution;

    @Schema(description = "响应时间区间列表（百分比位值）")
    private List<ResponseTimePercentileVO> responseTimePercentiles;

    /**
     * 响应时间百分位
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "响应时间百分位")
    public static class ResponseTimePercentileVO {
        @Schema(description = "百分位（50/95/99）")
        private Integer percentile;

        @Schema(description = "响应时间（毫秒）")
        private Long responseTime;

        @Schema(description = "请求数量")
        private Integer requestCount;
    }

    // ========== 慢请求列表 ==========

    @Schema(description = "慢请求列表（超过P95的请求）")
    private List<SlowRequestVO> slowRequests;

    /**
     * 慢请求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "慢请求")
    public static class SlowRequestVO {
        @Schema(description = "规则ID")
        private Long ruleId;

        @Schema(description = "规则名称")
        private String ruleName;

        @Schema(description = "响应时间（毫秒）")
        private Long responseTime;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "执行时间")
        private String executeTime;

        @Schema(description = "是否成功")
        private Boolean success;

        @Schema(description = "错误信息")
        private String errorMessage;
    }

    // ========== 性能指标详情 ==========

    @Schema(description = "性能指标详情")
    private PerformanceMetricsVO performanceMetrics;

    /**
     * 性能指标
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "性能指标")
    public static class PerformanceMetricsVO {
        @Schema(description = "CPU使用率（%）")
        private Double cpuUsage;

        @Schema(description = "内存使用（MB）")
        private Long memoryUsage;

        @Schema(description = "内存峰值（MB）")
        private Long memoryPeak;

        @Schema(description = "线程数")
        private Integer threadCount;

        @Schema(description = "活跃线程数")
        private Integer activeThreadCount;

        @Schema(description = "GC次数")
        private Integer gcCount;

        @Schema(description = "GC时间（毫秒）")
        private Long gcTime;
    }

    // ========== 规则级别统计 ==========

    @Schema(description = "每个规则的性能统计")
    private List<RuleStatisticsVO> ruleStatistics;

    /**
     * 规则统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "规则统计")
    public static class RuleStatisticsVO {
        @Schema(description = "规则ID")
        private Long ruleId;

        @Schema(description = "规则名称")
        private String ruleName;

        @Schema(description = "总请求数")
        private Integer totalRequests;

        @Schema(description = "成功请求数")
        private Integer successRequests;

        @Schema(description = "失败请求数")
        private Integer failedRequests;

        @Schema(description = "平均响应时间（毫秒）")
        private Double avgResponseTime;

        @Schema(description = "最大响应时间（毫秒）")
        private Long maxResponseTime;

        @Schema(description = "最小响应时间（毫秒）")
        private Long minResponseTime;

        @Schema(description = "吞吐量（TPS）")
        private Double throughput;
    }
}
