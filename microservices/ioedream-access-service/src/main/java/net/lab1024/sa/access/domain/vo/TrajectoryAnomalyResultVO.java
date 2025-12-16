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
 * 轨迹异常检测结果视图对象
 * <p>
 * 用户访问轨迹异常分析结果的数据传输对象
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
@Schema(description = "轨迹异常检测结果")
public class TrajectoryAnomalyResultVO {

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    /**
     * 轨迹ID
     */
    @Schema(description = "轨迹ID", example = "TRAJ_20250130_001")
    private String trajectoryId;

    /**
     * 分析时间范围（小时）
     */
    @Schema(description = "分析时间范围（小时）", example = "24")
    private Integer analysisTimeRange;

    /**
     * 异常评分（0-100）
     */
    @Schema(description = "异常评分（0-100）", example = "78.5")
    private BigDecimal anomalyScore;

    /**
     * 是否检测到异常
     */
    @Schema(description = "是否检测到异常", example = "true")
    private Boolean anomalyDetected;

    /**
     * 异常等级
     * LOW - 低级异常
     * MEDIUM - 中级异常
     * HIGH - 高级异常
     * CRITICAL - 严重异常
     */
    @Schema(description = "异常等级", example = "MEDIUM")
    private String anomalyLevel;

    /**
     * 异常类型
     */
    @Schema(description = "异常类型", example = "TIME_PATTERN_ANOMALY")
    private List<String> anomalyTypes;

    /**
     * 时间模式异常
     */
    @Schema(description = "时间模式异常")
    private TimePatternAnomalyVO timePatternAnomaly;

    /**
     * 空间模式异常
     */
    @Schema(description = "空间模式异常")
    private SpatialPatternAnomalyVO spatialPatternAnomaly;

    /**
     * 频率异常
     */
    @Schema(description = "频率异常")
    private FrequencyAnomalyVO frequencyAnomaly;

    /**
     * 行为序列异常
     */
    @Schema(description = "行为序列异常")
    private List<BehaviorSequenceAnomalyVO> behaviorAnomalies;

    /**
     * 风险评估
     */
    @Schema(description = "风险评估")
    private RiskAssessmentVO riskAssessment;

    /**
     * 处理建议
     */
    @Schema(description = "处理建议", example = "建议进一步验证用户身份，并监控后续访问行为")
    private String recommendation;

    /**
     * 分析时间
     */
    @Schema(description = "分析时间", example = "2025-01-30T15:45:00")
    private LocalDateTime analysisTime;

    /**
     * 时间模式异常内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "时间模式异常")
    public static class TimePatternAnomalyVO {

        @Schema(description = "异常类型", example = "ABNORMAL_TIME_ACCESS")
        private String anomalyType;

        @Schema(description = "异常时间列表", example = "02:30, 04:45")
        private List<String> abnormalTimes;

        @Schema(description = "偏离程度", example = "3.5")
        private BigDecimal deviationScore;

        @Schema(description = "频率", example = "HIGH")
        private String frequency;

        @Schema(description = "异常描述", example = "用户在非工作时间段有多次访问记录")
        private String description;
    }

    /**
     * 空间模式异常内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "空间模式异常")
    public static class SpatialPatternAnomalyVO {

        @Schema(description = "异常类型", example = "UNUSUAL_AREA_PATTERN")
        private String anomalyType;

        @Schema(description = "异常区域", example = "服务器机房, 高级管理区")
        private List<String> unusualAreas;

        @Schema(description = "访问频率", example = "FIRST_TIME_ACCESS")
        private String accessFrequency;

        @Schema(description = "偏离程度", example = "4.2")
        private BigDecimal deviationScore;

        @Schema(description = "异常描述", example = "用户首次访问高安全等级区域")
        private String description;
    }

    /**
     * 频率异常内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "频率异常")
    public static class FrequencyAnomalyVO {

        @Schema(description = "异常类型", example = "EXCESSIVE_FREQUENCY")
        private String anomalyType;

        @Schema(description = "正常频率", example = "15")
        private BigDecimal normalFrequency;

        @Schema(description = "实际频率", example = "85")
        private BigDecimal actualFrequency;

        @Schema(description = "频率倍数", example = "5.7")
        private BigDecimal frequencyMultiplier;

        @Schema(description = "时间窗口", example = "24小时")
        private String timeWindow;

        @Schema(description = "异常描述", example = "用户访问频率远超正常水平")
        private String description;
    }

    /**
     * 行为序列异常内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "行为序列异常")
    public static class BehaviorSequenceAnomalyVO {

        @Schema(description = "序列ID", example = "SEQ_001")
        private String sequenceId;

        @Schema(description = "异常类型", example = "RAPID_SUCCESSIVE_ACCESS")
        private String anomalyType;

        @Schema(description = "访问点序列", example = "DEVICE_001->DEVICE_002->DEVICE_003")
        private List<String> accessSequence;

        @Schema(description = "时间间隔", example = "30秒, 45秒, 20秒")
        private List<String> timeIntervals;

        @Schema(description = "异常评分", example = "8.5")
        private BigDecimal anomalyScore;

        @Schema(description = "置信度", example = "0.92")
        private BigDecimal confidence;

        @Schema(description = "描述", example = "检测到快速的连续访问模式")
        private String description;
    }

    /**
     * 风险评估内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "风险评估")
    public static class RiskAssessmentVO {

        @Schema(description = "风险等级", example = "MEDIUM")
        private String riskLevel;

        @Schema(description = "风险评分", example = "65.5")
        private BigDecimal riskScore;

        @Schema(description = "潜在威胁", example = "可能存在身份冒用或异常访问行为")
        private String potentialThreat;

        @Schema(description = "影响范围", example = "区域安全、数据访问")
        private String impactScope;

        @Schema(description = "建议措施", example = "增加二次验证，监控后续行为")
        private List<String> recommendedMeasures;

        @Schema(description = "紧急程度", example = "MEDIUM")
        private String urgency;
    }
}