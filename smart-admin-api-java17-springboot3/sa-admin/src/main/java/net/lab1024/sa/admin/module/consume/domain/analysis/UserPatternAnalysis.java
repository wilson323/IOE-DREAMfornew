package net.lab1024.sa.admin.module.consume.domain.analysis;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

/**
 * 用户行为模式分析
 *
 * @author SmartAdmin Team
 * @since 2025-11-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPatternAnalysis {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 分析时间
     */
    private LocalDateTime analysisTime;

    /**
     * 分析类型：CONSUMPTION-消费模式，LOCATION-位置模式，TIME-时间模式，DEVICE-设备模式
     */
    private String analysisType;

    /**
     * 模式稳定性评分（0-100）
     */
    private BigDecimal stabilityScore;

    /**
     * 模式复杂度评分（0-100）
     */
    private BigDecimal complexityScore;

    /**
     * 模式可预测性评分（0-100）
     */
    private BigDecimal predictabilityScore;

    /**
     * 主要模式特征
     */
    private Map<String, Object> primaryFeatures;

    /**
     * 次要模式特征
     */
    private Map<String, Object> secondaryFeatures;

    /**
     * 模式变化趋势
     */
    private List<BigDecimal> trendData;

    /**
     * 异常模式标识
     */
    private List<String> anomalyPatterns;

    /**
     * 周期性模式
     */
    private Map<String, Object> periodicPatterns;

    /**
     * 季节性模式
     */
    private Map<String, Object> seasonalPatterns;

    /**
     * 突发模式
     */
    private List<Map<String, Object>> burstPatterns;

    /**
     * 模式置信度
     */
    private BigDecimal confidence;

    /**
     * 数据样本量
     */
    private Integer sampleSize;

    /**
     * 分析窗口期（天）
     */
    private Integer analysisWindow;

    /**
     * 模式有效期
     */
    private LocalDateTime validUntil;

    /**
     * 需要更新基线
     */
    private Boolean needsBaselineUpdate;

    /**
     * 模式偏离警告
     */
    private List<String> deviationWarnings;

    /**
     * 建议监控指标
     */
    private List<String> recommendedMetrics;

    /**
     * 模式描述
     */
    private String patternDescription;

    /**
     * 业务影响评估
     */
    private String businessImpact;

    /**
     * 分析模型版本
     */
    private String analysisModelVersion;

    /**
     * 备注
     */
    private String remark;
}