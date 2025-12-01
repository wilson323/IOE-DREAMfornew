package net.lab1024.sa.admin.module.consume.domain.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

/**
 * 用户风险评分
 *
 * @author SmartAdmin Team
 * @since 2025-11-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRiskScore {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 风险评分（0-100）
     */
    private BigDecimal riskScore;

    /**
     * 风险等级：LOW-低风险（0-30），MEDIUM-中风险（31-60），HIGH-高风险（61-80），CRITICAL-严重风险（81-100）
     */
    private String riskLevel;

    /**
     * 评分计算时间
     */
    private LocalDateTime calculateTime;

    /**
     * 评分有效期至
     */
    private LocalDateTime validUntil;

    /**
     * 风险因子权重
     */
    private Map<String, BigDecimal> riskFactorWeights;

    /**
     * 各维度评分
     */
    private Map<String, BigDecimal> dimensionScores;

    /**
     * 历史评分趋势
     */
    private List<BigDecimal> historicalScores;

    /**
     * 异常行为次数（最近30天）
     */
    private Integer recentAnomalyCount;

    /**
     * 风险标签
     */
    private List<String> riskTags;

    /**
     * 评分说明
     */
    private String scoreDescription;

    /**
     * 风险原因
     */
    private List<String> riskReasons;

    /**
     * 建议措施
     */
    private List<String> recommendedMeasures;

    /**
     * 需要人工复核
     */
    private Boolean needsManualReview;

    /**
     * 限制措施
     */
    private List<String> restrictionMeasures;

    /**
     * 监控级别
     */
    private String monitorLevel;

    /**
     * 下次评估时间
     */
    private LocalDateTime nextEvaluationTime;

    /**
     * 更新频率（小时）
     */
    private Integer updateFrequency;

    /**
     * 评分模型版本
     */
    private String modelVersion;

    /**
     * 置信度
     */
    private BigDecimal confidence;

    /**
     * 备注
     */
    private String remark;
}