package net.lab1024.sa.admin.module.consume.domain.result;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 用户模式分析VO (Value Object)
 *
 * <p>用户行为模式分析的数据传输对象，包含用户行为模式、异常评分、
 * 分析结果、建议措施等详细信息。用于智能风控和用户画像分析。</p>
 *
 * <p>严格遵循repowiki规范：</p>
 * <ul>
 *   <li>使用完整的验证注解确保数据完整性</li>
 *   <li>包含详细的Swagger文档注解</li>
 *   <li>使用JsonInclude忽略null值</li>
 *   <li>提供Builder模式支持灵活构造</li>
 *   <li>包含丰富的业务分析方法</li>
 * </ul>
 *
 * @author SmartAdmin Team
 * @since 2025-11-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "用户模式分析VO")
public class UserPatternAnalysis {

    /**
     * 分析ID
     */
    @Schema(description = "分析ID", example = "ANALYSIS_001")
    private Long analysisId;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    @Positive(message = "用户ID必须为正数")
    @Schema(description = "用户ID", example = "10086", required = true)
    private Long userId;

    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    /**
     * 异常评分（0-100）
     *
     * <p>异常评分越高，用户行为越偏离正常模式</p>
     */
    @DecimalMin(value = "0.0", message = "异常评分不能小于0")
    @DecimalMax(value = "100.0", message = "异常评分不能大于100")
    @Schema(description = "异常评分（0-100）", example = "42.5")
    private BigDecimal anomalyScore;

    /**
     * 异常等级
     *
     * <p>NORMAL-正常（0-20）, SLIGHT_ABNORMAL-轻微异常（21-40）, MODERATE_ABNORMAL-中度异常（41-70）, SEVERE_ABNORMAL-严重异常（71-100）</p>
     */
    @Pattern(regexp = "^(NORMAL|SLIGHT_ABNORMAL|MODERATE_ABNORMAL|SEVERE_ABNORMAL)$", message = "异常等级无效")
    @Schema(description = "异常等级", example = "MODERATE_ABNORMAL", allowableValues = {"NORMAL", "SLIGHT_ABNORMAL", "MODERATE_ABNORMAL", "SEVERE_ABNORMAL"})
    private String anomalyLevel;

    /**
     * 分析日期
     */
    @Schema(description = "分析日期", example = "2025-11-28")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime analysisDate;

    /**
     * 分析时段开始时间
     */
    @Schema(description = "分析时段开始时间", example = "2025-11-27 00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime analysisStartTime;

    /**
     * 分析时段结束时间
     */
    @Schema(description = "分析时段结束时间", example = "2025-11-28 23:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime analysisEndTime;

    /**
     * 置信度（0-1）
     *
     * <p>分析结果的可信度，越接近1越可信</p>
     */
    @DecimalMin(value = "0.0", message = "置信度不能小于0")
    @DecimalMax(value = "1.0", message = "置信度不能大于1")
    @Schema(description = "置信度（0-1）", example = "0.85")
    private BigDecimal confidence;

    /**
     * 分析方法
     *
     * <p>STATISTICAL-统计分析, MACHINE_LEARNING-机器学习, RULE_BASED-基于规则, HYBRID-混合方法</p>
     */
    @Pattern(regexp = "^(STATISTICAL|MACHINE_LEARNING|RULE_BASED|HYBRID)$", message = "分析方法无效")
    @Schema(description = "分析方法", example = "MACHINE_LEARNING", allowableValues = {"STATISTICAL", "MACHINE_LEARNING", "RULE_BASED", "HYBRID"})
    private String analysisMethod;

    /**
     * 建议措施
     */
    @Schema(description = "建议措施")
    private List<String> recommendations;

    /**
     * 预警信息
     */
    @Schema(description = "预警信息")
    private List<AlertInfo> alerts;

    /**
     * 分析报告摘要
     */
    @Size(max = 1000, message = "分析报告摘要长度不能超过1000个字符")
    @Schema(description = "分析报告摘要", example = "用户消费模式基本稳定，但存在轻微的时间分布异常")
    private String summary;

    /**
     * 详细分析报告
     */
    @Schema(description = "详细分析报告")
    private String detailedReport;

    /**
     * 扩展信息
     */
    @Schema(description = "扩展信息")
    private Map<String, Object> extensions;

    // ========== 兼容旧版本字段 ==========

    /**
     * 分析周期（兼容字段）
     */
    @Schema(description = "分析周期")
    private String analysisPeriod;

    /**
     * 消费模式类型（兼容字段）
     */
    @Schema(description = "消费模式类型")
    private String consumptionPattern;

    /**
     * 时间偏好（兼容字段）
     */
    @Schema(description = "时间偏好")
    private List<String> timePreferences;

    /**
     * 地点偏好（兼容字段）
     */
    @Schema(description = "地点偏好")
    private List<String> locationPreferences;

    /**
     * 金额偏好（兼容字段）
     */
    @Schema(description = "金额偏好")
    private AmountPreference amountPreference;

    /**
     * 频率分析（兼容字段）
     */
    @Schema(description = "频率分析")
    private FrequencyAnalysis frequencyAnalysis;

    /**
     * 异常指标（兼容字段）
     */
    @Schema(description = "异常指标")
    private List<AnomalyIndicator> anomalyIndicators;

    /**
     * 分析时间（兼容字段）
     */
    @Schema(description = "分析时间")
    private LocalDateTime analysisTime;

    // ==================== 业务方法 ====================

    /**
     * 判断是否存在异常
     *
     * @return true如果存在异常
     */
    public boolean hasAnomaly() {
        return anomalyScore != null && anomalyScore.compareTo(new BigDecimal("20")) > 0;
    }

    /**
     * 判断是否为高风险用户
     *
     * @return true如果是高风险用户
     */
    public boolean isHighRisk() {
        return "SEVERE_ABNORMAL".equals(anomalyLevel) ||
               (anomalyScore != null && anomalyScore.compareTo(new BigDecimal("70")) > 0);
    }

    /**
     * 判断是否需要关注
     *
     * @return true如果需要关注
     */
    public boolean needsAttention() {
        return hasAnomaly() || (alerts != null && !alerts.isEmpty());
    }

    /**
     * 获取异常等级描述
     *
     * @return 异常等级描述
     */
    public String getAnomalyLevelDescription() {
        switch (anomalyLevel) {
            case "NORMAL":
                return "正常";
            case "SLIGHT_ABNORMAL":
                return "轻微异常";
            case "MODERATE_ABNORMAL":
                return "中度异常";
            case "SEVERE_ABNORMAL":
                return "严重异常";
            default:
                return "未知";
        }
    }

    /**
     * 获取分析方法描述
     *
     * @return 分析方法描述
     */
    public String getAnalysisMethodDescription() {
        switch (analysisMethod) {
            case "STATISTICAL":
                return "统计分析";
            case "MACHINE_LEARNING":
                return "机器学习";
            case "RULE_BASED":
                return "基于规则";
            case "HYBRID":
                return "混合方法";
            default:
                return "未知方法";
        }
    }

    /**
     * 获取预警数量
     *
     * @return 预警数量
     */
    public int getAlertCount() {
        return alerts != null ? alerts.size() : 0;
    }

    /**
     * 获取建议数量
     *
     * @return 建议数量
     */
    public int getRecommendationCount() {
        return recommendations != null ? recommendations.size() : 0;
    }

    /**
     * 获取异常指标数量
     *
     * @return 异常指标数量
     */
    public int getAnomalyIndicatorCount() {
        return anomalyIndicators != null ? anomalyIndicators.size() : 0;
    }

    /**
     * 验证结果数据的完整性
     *
     * @return 验证结果
     */
    public boolean isValid() {
        if (userId == null) {
            return false;
        }
        if (anomalyScore != null && (anomalyScore.compareTo(new BigDecimal("0")) < 0 || anomalyScore.compareTo(new BigDecimal("100")) > 0)) {
            return false;
        }
        if (confidence != null && (confidence.compareTo(new BigDecimal("0")) < 0 || confidence.compareTo(new BigDecimal("1")) > 0)) {
            return false;
        }
        return true;
    }

    /**
     * 创建正常用户模式分析结果
     *
     * @param userId 用户ID
     * @param analysisDate 分析日期
     * @return 正常分析结果对象
     */
    public static UserPatternAnalysis createNormal(Long userId, LocalDateTime analysisDate) {
        return UserPatternAnalysis.builder()
                .analysisId(System.currentTimeMillis())
                .userId(userId)
                .anomalyScore(new BigDecimal("10.5"))
                .anomalyLevel("NORMAL")
                .analysisDate(analysisDate)
                .analysisTime(analysisDate)
                .confidence(new BigDecimal("0.95"))
                .analysisMethod("MACHINE_LEARNING")
                .summary("用户行为模式正常，未发现异常")
                .build();
    }

    /**
     * 创建异常用户模式分析结果
     *
     * @param userId 用户ID
     * @param anomalyScore 异常评分
     * @param alertMessage 预警消息
     * @return 异常分析结果对象
     */
    public static UserPatternAnalysis createAbnormal(Long userId, BigDecimal anomalyScore, String alertMessage) {
        return UserPatternAnalysis.builder()
                .analysisId(System.currentTimeMillis())
                .userId(userId)
                .anomalyScore(anomalyScore)
                .anomalyLevel(anomalyScore.compareTo(new BigDecimal("40")) > 0 ? "MODERATE_ABNORMAL" : "SLIGHT_ABNORMAL")
                .analysisDate(LocalDateTime.now())
                .analysisTime(LocalDateTime.now())
                .confidence(new BigDecimal("0.82"))
                .analysisMethod("HYBRID")
                .summary("检测到用户行为异常，需要进一步关注")
                .recommendations(List.of("加强监控", "设置预警阈值", "人工审核"))
                .build();
    }

    /**
     * 预警信息内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "预警信息")
    public static class AlertInfo {

        /**
         * 预警ID
         */
        @Schema(description = "预警ID", example = "ALERT_001")
        private String alertId;

        /**
         * 预警级别
         *
         * <p>INFO-信息, WARNING-警告, ERROR-错误, CRITICAL-严重</p>
         */
        @Pattern(regexp = "^(INFO|WARNING|ERROR|CRITICAL)$", message = "预警级别无效")
        @Schema(description = "预警级别", example = "WARNING", allowableValues = {"INFO", "WARNING", "ERROR", "CRITICAL"})
        private String alertLevel;

        /**
         * 预警消息
         */
        @NotBlank(message = "预警消息不能为空")
        @Size(max = 500, message = "预警消息长度不能超过500个字符")
        @Schema(description = "预警消息", example = "检测到消费时间异常变化", required = true)
        private String alertMessage;

        /**
         * 预警时间
         */
        @Schema(description = "预警时间", example = "2025-11-28 14:25:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime alertTime;

        /**
         * 预警类型
         *
         * <p>ANOMALY-异常, TREND-趋势, PATTERN-模式, SECURITY-安全</p>
         */
        @Pattern(regexp = "^(ANOMALY|TREND|PATTERN|SECURITY)$", message = "预警类型无效")
        @Schema(description = "预警类型", example = "ANOMALY", allowableValues = {"ANOMALY", "TREND", "PATTERN", "SECURITY"})
        private String alertType;

        /**
         * 处理状态
         *
         * <p>NEW-新建, ACKNOWLEDGED-已确认, PROCESSING-处理中, RESOLVED-已解决</p>
         */
        @Pattern(regexp = "^(NEW|ACKNOWLEDGED|PROCESSING|RESOLVED)$", message = "处理状态无效")
        @Schema(description = "处理状态", example = "NEW", allowableValues = {"NEW", "ACKNOWLEDGED", "PROCESSING", "RESOLVED"})
        private String status;
    }

    /**
     * 金额偏好内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "金额偏好")
    public static class AmountPreference {
        @Schema(description = "平均消费金额")
        private Double averageAmount;

        @Schema(description = "中位数金额")
        private Double medianAmount;

        @Schema(description = "最大金额")
        private Double maxAmount;

        @Schema(description = "最小金额")
        private Double minAmount;

        @Schema(description = "金额分布")
        private Map<String, Integer> amountDistribution;
    }

    /**
     * 频率分析内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "频率分析")
    public static class FrequencyAnalysis {
        @Schema(description = "日均交易次数")
        private Double dailyAverage;

        @Schema(description = "周活跃天数")
        private Integer weeklyActiveDays;

        @Schema(description = "月活跃天数")
        private Integer monthlyActiveDays;

        @Schema(description = "高峰时段")
        private List<String> peakHours;

        @Schema(description = "频率趋势")
        private String frequencyTrend;
    }

    /**
     * 异常指标内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "异常指标")
    public static class AnomalyIndicator {
        @Schema(description = "指标名称")
        private String indicatorName;

        @Schema(description = "当前值")
        private Double currentValue;

        @Schema(description = "正常范围")
        private String normalRange;

        @Schema(description = "偏离程度")
        private String deviationLevel;

        @Schema(description = "是否显著异常")
        private Boolean isSignificant;
    }
}