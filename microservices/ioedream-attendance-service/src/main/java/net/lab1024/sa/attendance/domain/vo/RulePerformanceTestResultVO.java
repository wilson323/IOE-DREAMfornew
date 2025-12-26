package net.lab1024.sa.attendance.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 规则性能测试结果视图对象
 * <p>
 * 展示单次性能测试的执行结果
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "规则性能测试结果")
public class RulePerformanceTestResultVO {

    @Schema(description = "测试ID")
    private Long testId;

    @Schema(description = "测试名称")
    private String testName;

    @Schema(description = "测试类型：SINGLE-单规则/BATCH-批量规则/CONCURRENT-并发测试")
    private String testType;

    @Schema(description = "测试状态：RUNNING-运行中/COMPLETED-已完成/CANCELLED-已取消/ERROR-错误")
    private String testStatus;

    // ========== 基本统计 ==========

    @Schema(description = "规则数量")
    private Integer ruleCount;

    @Schema(description = "并发用户数")
    private Integer concurrentUsers;

    @Schema(description = "总请求数")
    private Integer totalRequests;

    @Schema(description = "成功请求数")
    private Integer successRequests;

    @Schema(description = "失败请求数")
    private Integer failedRequests;

    @Schema(description = "成功率（百分比）")
    private Double successRate;

    // ========== 响应时间统计 ==========

    @Schema(description = "最小响应时间（毫秒）")
    private Long minResponseTime;

    @Schema(description = "最大响应时间（毫秒）")
    private Long maxResponseTime;

    @Schema(description = "平均响应时间（毫秒）")
    private Double avgResponseTime;

    @Schema(description = "P50响应时间（中位数，毫秒）")
    private Long p50ResponseTime;

    @Schema(description = "P95响应时间（95%请求，毫秒）")
    private Long p95ResponseTime;

    @Schema(description = "P99响应时间（99%请求，毫秒）")
    private Long p99ResponseTime;

    // ========== 吞吐量统计 ==========

    @Schema(description = "吞吐量（TPS-每秒事务数）")
    private Double throughput;

    @Schema(description = "QPS（每秒查询数）")
    private Double qps;

    // ========== 执行信息 ==========

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "测试开始时间")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "测试结束时间")
    private LocalDateTime endTime;

    @Schema(description = "测试时长（秒）")
    private Long durationSeconds;

    @Schema(description = "错误信息（如果有）")
    private String errorMessage;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
