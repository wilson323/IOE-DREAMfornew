package net.lab1024.sa.attendance.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 规则性能测试实体
 * <p>
 * 存储规则性能测试执行记录和统计结果
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
@TableName("t_attendance_rule_performance_test")
@Schema(description = "规则性能测试实体")
public class RulePerformanceTestEntity {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "测试ID")
    private Long testId;

    @TableField("test_name")
    @Schema(description = "测试名称")
    private String testName;

    @TableField("test_type")
    @Schema(description = "测试类型：SINGLE-单规则/BATCH-批量规则/CONCURRENT-并发测试")
    private String testType;

    @TableField("rule_ids")
    @Schema(description = "测试的规则ID列表（JSON）")
    private String ruleIds;

    @TableField("rule_count")
    @Schema(description = "规则数量")
    private Integer ruleCount;

    @TableField("concurrent_users")
    @Schema(description = "并发用户数")
    private Integer concurrentUsers;

    @TableField("total_requests")
    @Schema(description = "总请求数")
    private Integer totalRequests;

    @TableField("success_requests")
    @Schema(description = "成功请求数")
    private Integer successRequests;

    @TableField("failed_requests")
    @Schema(description = "失败请求数")
    private Integer failedRequests;

    @TableField("success_rate")
    @Schema(description = "成功率（百分比）")
    private Double successRate;

    // ========== 响应时间统计 ==========

    @TableField("min_response_time")
    @Schema(description = "最小响应时间（毫秒）")
    private Long minResponseTime;

    @TableField("max_response_time")
    @Schema(description = "最大响应时间（毫秒）")
    private Long maxResponseTime;

    @TableField("avg_response_time")
    @Schema(description = "平均响应时间（毫秒）")
    private Double avgResponseTime;

    @TableField("p50_response_time")
    @Schema(description = "P50响应时间（中位数，毫秒）")
    private Long p50ResponseTime;

    @TableField("p95_response_time")
    @Schema(description = "P95响应时间（95%请求，毫秒）")
    private Long p95ResponseTime;

    @TableField("p99_response_time")
    @Schema(description = "P99响应时间（99%请求，毫秒）")
    private Long p99ResponseTime;

    // ========== 吞吐量统计 ==========

    @TableField("throughput")
    @Schema(description = "吞吐量（TPS-每秒事务数）")
    private Double throughput;

    @TableField("qps")
    @Schema(description = "QPS（每秒查询数）")
    private Double qps;

    // ========== 执行信息 ==========

    @TableField("start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "测试开始时间")
    private LocalDateTime startTime;

    @TableField("end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "测试结束时间")
    private LocalDateTime endTime;

    @TableField("duration_seconds")
    @Schema(description = "测试时长（秒）")
    private Long durationSeconds;

    @TableField("test_status")
    @Schema(description = "测试状态：RUNNING-运行中/COMPLETED-已完成/CANCELLED-已取消/ERROR-错误")
    private String testStatus;

    @TableField("error_message")
    @Schema(description = "错误信息")
    private String errorMessage;

    // ========== 详细数据 ==========

    @TableField("response_time_distribution")
    @Schema(description = "响应时间分布（JSON）")
    private String responseTimeDistribution;

    @TableField("slow_requests")
    @Schema(description = "慢请求列表（JSON）")
    private String slowRequests;

    @TableField("performance_metrics")
    @Schema(description = "性能指标详情（JSON）")
    private String performanceMetrics;

    // ========== 审计字段 ==========

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @TableField("created_by")
    @Schema(description = "创建人")
    private Long createdBy;

    @TableField("created_by_name")
    @Schema(description = "创建人姓名")
    private String createdByName;
}
