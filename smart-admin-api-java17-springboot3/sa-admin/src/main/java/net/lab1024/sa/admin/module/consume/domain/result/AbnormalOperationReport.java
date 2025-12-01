package net.lab1024.sa.admin.module.consume.domain.result;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 异常操作报告
 *
 * @author SmartAdmin Team
 * @since 2025-11-22
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "异常操作报告")
public class AbnormalOperationReport {

    @Schema(description = "报告ID")
    private Long reportId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "报告类型")
    private String reportType;

    @Schema(description = "报告期间开始时间")
    private LocalDateTime periodStart;

    @Schema(description = "报告期间结束时间")
    private LocalDateTime periodEnd;

    @Schema(description = "总操作数")
    private Integer totalOperations;

    @Schema(description = "异常操作数")
    private Integer abnormalOperations;

    @Schema(description = "异常率")
    private Double abnormalityRate;

    @Schema(description = "风险等级")
    private String riskLevel;

    @Schema(description = "异常摘要")
    private String abnormalitySummary;

    @Schema(description = "异常详情")
    private List<AbnormalityDetail> abnormalityDetails;

    @Schema(description = "统计分析")
    private StatisticalAnalysis statisticalAnalysis;

    @Schema(description = "建议措施")
    private List<String> recommendedActions;

    @Schema(description = "生成时间")
    private LocalDateTime generatedTime;

    @Schema(description = "报告状态")
    private String reportStatus;

    /**
     * 异常详情内部类
     */
    
    
    
    
    @Schema(description = "异常详情")
    public static class AbnormalityDetail {
        @Schema(description = "详情ID")
        private Long detailId;

        @Schema(description = "异常类型")
        private String anomalyType;

        @Schema(description = "异常描述")
        private String description;

        @Schema(description = "发生时间")
        private LocalDateTime occurTime;

        @Schema(description = "涉及金额")
        private Double amount;

        @Schema(description = "风险等级")
        private String severity;

        @Schema(description = "是否已处理")
        private Boolean isHandled;

        @Schema(description = "处理措施")
        private String handlingAction;
    }

    /**
     * 统计分析内部类
     */
    
    
    
    
    @Schema(description = "统计分析")
    public static class StatisticalAnalysis {
        @Schema(description = "异常类型分布")
        private Map<String, Integer> anomalyTypeDistribution;

        @Schema(description = "时间分布分析")
        private Map<String, Integer> timeDistribution;

        @Schema(description = "金额分布分析")
        private Map<String, Integer> amountDistribution;

        @Schema(description = "地点分布分析")
        private Map<String, Integer> locationDistribution;

        @Schema(description = "趋势分析")
        private String trendAnalysis;

        @Schema(description = "同比变化")
        private Double yearOverYearChange;

        @Schema(description = "环比变化")
        private Double monthOverMonthChange;
    }

    /**
     * 创建异常操作报告实例
     */
    public static AbnormalOperationReport create(Long userId, String reportType, 
                                                  LocalDateTime start, LocalDateTime end,
                                                  int totalOps, int abnormalOps) {
        double abnormalityRate = totalOps > 0 ? (double) abnormalOps / totalOps * 100 : 0.0;
        return AbnormalOperationReport.builder()
                .reportId(System.currentTimeMillis())
                .userId(userId)
                .reportType(reportType)
                .periodStart(start)
                .periodEnd(end)
                .totalOperations(totalOps)
                .abnormalOperations(abnormalOps)
                .abnormalityRate(abnormalityRate)
                .riskLevel(determineRiskLevel(abnormalityRate))
                .abnormalitySummary(generateSummary(abnormalityRate, abnormalOps))
                .generatedTime(LocalDateTime.now())
                .reportStatus("COMPLETED")
                .build();
    }

    /**
     * 创建高风险报告实例
     */
    public static AbnormalOperationReport highRiskReport(Long userId, List<AbnormalityDetail> details) {
        return AbnormalOperationReport.builder()
                .reportId(System.currentTimeMillis())
                .userId(userId)
                .reportType("HIGH_RISK_ALERT")
                .abnormalOperations(details.size())
                .abnormalityRate(100.0)
                .riskLevel("HIGH")
                .abnormalitySummary("检测到高风险异常操作")
                .abnormalityDetails(details)
                .generatedTime(LocalDateTime.now())
                .reportStatus("URGENT")
                .build();
    }

    /**
     * 创建常规报告实例
     */
    public static AbnormalOperationReport routineReport(Long userId, int totalOps, int abnormalOps) {
        double abnormalityRate = totalOps > 0 ? (double) abnormalOps / totalOps * 100 : 0.0;
        return AbnormalOperationReport.builder()
                .reportId(System.currentTimeMillis())
                .userId(userId)
                .reportType("ROUTINE_ANALYSIS")
                .periodStart(LocalDateTime.now().minusDays(30))
                .periodEnd(LocalDateTime.now())
                .totalOperations(totalOps)
                .abnormalOperations(abnormalOps)
                .abnormalityRate(abnormalityRate)
                .riskLevel(determineRiskLevel(abnormalityRate))
                .abnormalitySummary(generateSummary(abnormalityRate, abnormalOps))
                .generatedTime(LocalDateTime.now())
                .reportStatus("COMPLETED")
                .build();
    }

    /**
     * 生成异常摘要
     */
    private static String generateSummary(double abnormalityRate, int abnormalOps) {
        if (abnormalOps == 0) {
            return "未检测到异常操作";
        } else if (abnormalityRate >= 20.0) {
            return String.format("检测到%d个异常操作，异常率%.1f%%，情况严重", abnormalOps, abnormalityRate);
        } else if (abnormalityRate >= 10.0) {
            return String.format("检测到%d个异常操作，异常率%.1f%%，需要关注", abnormalOps, abnormalityRate);
        } else {
            return String.format("检测到%d个异常操作，异常率%.1f%%，整体可控", abnormalOps, abnormalityRate);
        }
    }

    /**
     * 确定风险等级
     */
    private static String determineRiskLevel(double abnormalityRate) {
        if (abnormalityRate >= 20.0) return "HIGH";
        if (abnormalityRate >= 10.0) return "MEDIUM";
        if (abnormalityRate >= 5.0) return "LOW";
        return "MINIMAL";
    }

    /**
     * 添加异常详情
     */
    public AbnormalOperationReport addAbnormalityDetail(AbnormalityDetail detail) {
        if (this.abnormalityDetails == null) {
            this.abnormalityDetails = new java.util.ArrayList<>();
        }
        this.abnormalityDetails.add(detail);
        return this;
    }

    /**
     * 添加建议措施
     */
    public AbnormalOperationReport addRecommendedAction(String action) {
        if (this.recommendedActions == null) {
            this.recommendedActions = new java.util.ArrayList<>();
        }
        this.recommendedActions.add(action);
        return this;
    }

    /**
     * 检查报告是否完整
     */
    public boolean isComplete() {
        return "COMPLETED".equals(reportStatus) && abnormalityDetails != null && !abnormalityDetails.isEmpty();
    }

    /**
     * 检查是否需要人工审核
     */
    public boolean requiresManualReview() {
        return "HIGH".equals(riskLevel) || "URGENT".equals(reportStatus);
    }

}