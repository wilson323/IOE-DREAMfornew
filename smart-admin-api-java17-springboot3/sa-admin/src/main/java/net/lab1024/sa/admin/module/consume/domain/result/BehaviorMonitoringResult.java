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
 * 行为监控结果VO (Value Object)
 *
 * <p>用户行为监控和分析的结果数据传输对象，包含风险评分、可疑活动、
 * 异常行为检测、安全建议等详细信息。用于消费系统的风控和安全管理。</p>
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
@Schema(description = "行为监控结果VO")
public class BehaviorMonitoringResult {

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
     * 风险评分（0-100）
     *
     * <p>风险评分越高，用户行为越可疑，需要更严格的监控</p>
     */
    @DecimalMin(value = "0.0", message = "风险评分不能小于0")
    @DecimalMax(value = "100.0", message = "风险评分不能大于100")
    @Schema(description = "风险评分（0-100）", example = "75.5")
    private BigDecimal riskScore;

    /**
     * 风险等级
     *
     * <p>LOW-低风险（0-30）, MEDIUM-中风险（31-60）, HIGH-高风险（61-80）, CRITICAL-严重风险（81-100）</p>
     */
    @Pattern(regexp = "^(LOW|MEDIUM|HIGH|CRITICAL)$", message = "风险等级无效")
    @Schema(description = "风险等级", example = "HIGH", allowableValues = {"LOW", "MEDIUM", "HIGH", "CRITICAL"})
    private String riskLevel;

    /**
     * 可疑活动列表
     */
    @Schema(description = "可疑活动列表")
    private List<SuspiciousActivity> suspiciousActivities;

    /**
     * 异常行为模式
     */
    @Schema(description = "异常行为模式")
    private List<AbnormalBehaviorPattern> abnormalBehaviorPatterns;

    /**
     * 安全建议
     */
    @Schema(description = "安全建议")
    private List<String> recommendations;

    /**
     * 监控开始时间
     */
    @Schema(description = "监控开始时间", example = "2025-11-28 00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime monitoringStartTime;

    /**
     * 监控结束时间
     */
    @Schema(description = "监控结束时间", example = "2025-11-28 23:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime monitoringEndTime;

    /**
     * 分析时间
     */
    @Schema(description = "分析时间", example = "2025-11-28 12:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime analysisTime;

    /**
     * 监控状态
     *
     * <p>ACTIVE-活跃监控, PAUSED-暂停监控, STOPPED-停止监控, REVIEW-待审核</p>
     */
    @Pattern(regexp = "^(ACTIVE|PAUSED|STOPPED|REVIEW)$", message = "监控状态无效")
    @Schema(description = "监控状态", example = "ACTIVE", allowableValues = {"ACTIVE", "PAUSED", "STOPPED", "REVIEW"})
    private String monitoringStatus;

    /**
     * 是否触发告警
     */
    @Schema(description = "是否触发告警", example = "true")
    private Boolean alertTriggered;

    /**
     * 告警级别
     *
     * <p>INFO-信息, WARNING-警告, ERROR-错误, CRITICAL-严重</p>
     */
    @Pattern(regexp = "^(INFO|WARNING|ERROR|CRITICAL)$", message = "告警级别无效")
    @Schema(description = "告警级别", example = "WARNING", allowableValues = {"INFO", "WARNING", "ERROR", "CRITICAL"})
    private String alertLevel;

    /**
     * 告警消息
     */
    @Size(max = 500, message = "告警消息长度不能超过500个字符")
    @Schema(description = "告警消息", example = "检测到异常登录行为")
    private String alertMessage;

    /**
     * 是否需要人工审核
     */
    @Schema(description = "是否需要人工审核", example = "true")
    private Boolean requiresManualReview;

    /**
     * 处理状态
     *
     * <p>PENDING-待处理, PROCESSING-处理中, RESOLVED-已解决, ESCALATED-已升级</p>
     */
    @Pattern(regexp = "^(PENDING|PROCESSING|RESOLVED|ESCALATED)$", message = "处理状态无效")
    @Schema(description = "处理状态", example = "PENDING", allowableValues = {"PENDING", "PROCESSING", "RESOLVED", "ESCALATED"})
    private String processingStatus;

    /**
     * 处理人员ID
     */
    @Schema(description = "处理人员ID", example = "20001")
    private Long processedBy;

    /**
     * 处理时间
     */
    @Schema(description = "处理时间", example = "2025-11-28 14:20:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime processedTime;

    /**
     * 处理备注
     */
    @Size(max = 1000, message = "处理备注长度不能超过1000个字符")
    @Schema(description = "处理备注", example = "已联系用户确认，行为正常")
    private String processingRemark;

    /**
     * 统计数据
     */
    @Schema(description = "统计数据")
    private BehaviorStatistics statistics;

    /**
     * 扩展信息
     */
    @Schema(description = "扩展信息")
    private Map<String, Object> extensions;

    // ========== 兼容旧版本字段 ==========

    /**
     * 监控时间（兼容字段）
     */
    @Schema(description = "监控时间")
    private LocalDateTime monitorTime;

    /**
     * 是否异常（兼容字段）
     */
    @Schema(description = "是否异常")
    private Boolean isAnomalous;

    /**
     * 异常类型（兼容字段）
     */
    @Schema(description = "异常类型")
    private String anomalyType;

    /**
     * 异常描述（兼容字段）
     */
    @Schema(description = "异常描述")
    private String anomalyDescription;

    /**
     * 异常分数（兼容字段）
     */
    @Schema(description = "异常分数")
    private BigDecimal anomalyScore;

    /**
     * 置信度（兼容字段）
     */
    @Schema(description = "置信度")
    private BigDecimal confidence;

    /**
     * 基线数据（兼容字段）
     */
    @Schema(description = "基线数据")
    private Map<String, Object> baselineData;

    /**
     * 当前行为数据（兼容字段）
     */
    @Schema(description = "当前行为数据")
    private Map<String, Object> currentBehavior;

    /**
     * 偏差度量（兼容字段）
     */
    @Schema(description = "偏差度量")
    private Map<String, BigDecimal> deviations;

    /**
     * 触发的规则列表（兼容字段）
     */
    @Schema(description = "触发的规则列表")
    private List<String> triggeredRules;

    /**
     * 建议行动（兼容字段）
     */
    @Schema(description = "建议行动")
    private List<String> recommendedActions;

    /**
     * 详细分析数据（兼容字段）
     */
    @Schema(description = "详细分析数据")
    private Map<String, Object> analysisDetails;

    /**
     * 需要进一步调查（兼容字段）
     */
    @Schema(description = "需要进一步调查")
    private Boolean needsInvestigation;

    /**
     * 自动处理建议（兼容字段）
     */
    @Schema(description = "自动处理建议")
    private String autoHandlingRecommendation;

    /**
     * 通知配置（兼容字段）
     */
    @Schema(description = "通知配置")
    private Map<String, Object> notificationConfig;

    /**
     * 可疑活动内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "可疑活动")
    public static class SuspiciousActivity {

        /**
         * 活动ID
         */
        @Schema(description = "活动ID", example = "ACT_001")
        private String activityId;

        /**
         * 活动类型
         *
         * <p>LOGIN-登录异常, TRANSACTION-交易异常, LOCATION-位置异常, TIME-时间异常,
         * FREQUENCY-频率异常, AMOUNT-金额异常, DEVICE-设备异常</p>
         */
        @Pattern(regexp = "^(LOGIN|TRANSACTION|LOCATION|TIME|FREQUENCY|AMOUNT|DEVICE)$", message = "活动类型无效")
        @Schema(description = "活动类型", example = "LOGIN", allowableValues = {"LOGIN", "TRANSACTION", "LOCATION", "TIME", "FREQUENCY", "AMOUNT", "DEVICE"})
        private String activityType;

        /**
         * 活动描述
         */
        @NotBlank(message = "活动描述不能为空")
        @Size(max = 200, message = "活动描述长度不能超过200个字符")
        @Schema(description = "活动描述", example = "异地登录尝试", required = true)
        private String description;

        /**
         * 风险权重（0-1）
         */
        @DecimalMin(value = "0.0", message = "风险权重不能小于0")
        @DecimalMax(value = "1.0", message = "风险权重不能大于1")
        @Schema(description = "风险权重", example = "0.8")
        private BigDecimal riskWeight;

        /**
         * 发生时间
         */
        @Schema(description = "发生时间", example = "2025-11-28 10:15:30")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime occurredTime;

        /**
         * IP地址
         */
        @Pattern(regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$",
                 message = "IP地址格式不正确")
        @Schema(description = "IP地址", example = "192.168.1.100")
        private String ipAddress;

        /**
         * 设备信息
         */
        @Schema(description = "设备信息", example = "iPhone 13 Pro")
        private String deviceInfo;

        /**
         * 地理位置
         */
        @Schema(description = "地理位置", example = "北京市")
        private String location;

        /**
         * 详细数据
         */
        @Schema(description = "详细数据")
        private Map<String, Object> details;
    }

    /**
     * 异常行为模式内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "异常行为模式")
    public static class AbnormalBehaviorPattern {

        /**
         * 模式ID
         */
        @Schema(description = "模式ID", example = "PATTERN_001")
        private String patternId;

        /**
         * 模式名称
         */
        @NotBlank(message = "模式名称不能为空")
        @Size(max = 100, message = "模式名称长度不能超过100个字符")
        @Schema(description = "模式名称", example = "深夜高频消费", required = true)
        private String patternName;

        /**
         * 模式描述
         */
        @Size(max = 500, message = "模式描述长度不能超过500个字符")
        @Schema(description = "模式描述", example = "用户在深夜时段（23:00-05:00）进行高频消费操作")
        private String description;

        /**
         * 检测到的次数
         */
        @Min(value = 1, message = "检测次数必须大于0")
        @Schema(description = "检测到的次数", example = "5")
        private Integer detectionCount;

        /**
         * 风险等级
         */
        @Pattern(regexp = "^(LOW|MEDIUM|HIGH|CRITICAL)$", message = "风险等级无效")
        @Schema(description = "风险等级", example = "HIGH", allowableValues = {"LOW", "MEDIUM", "HIGH", "CRITICAL"})
        private String riskLevel;

        /**
         * 首次检测时间
         */
        @Schema(description = "首次检测时间", example = "2025-11-25 02:30:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime firstDetectedTime;

        /**
         * 最近检测时间
         */
        @Schema(description = "最近检测时间", example = "2025-11-28 01:45:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastDetectedTime;

        /**
         * 影响因子列表
         */
        @Schema(description = "影响因子列表")
        private List<String> influencingFactors;
    }

    /**
     * 行为统计数据内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "行为统计数据")
    public static class BehaviorStatistics {

        /**
         * 总活动次数
         */
        @Min(value = 0, message = "总活动次数不能为负数")
        @Schema(description = "总活动次数", example = "156")
        private Integer totalActivities;

        /**
         * 正常活动次数
         */
        @Min(value = 0, message = "正常活动次数不能为负数")
        @Schema(description = "正常活动次数", example = "142")
        private Integer normalActivities;

        /**
         * 可疑活动次数
         */
        @Min(value = 0, message = "可疑活动次数不能为负数")
        @Schema(description = "可疑活动次数", example = "14")
        private Integer suspiciousActivities;

        /**
         * 异常活动次数
         */
        @Min(value = 0, message = "异常活动次数不能为负数")
        @Schema(description = "异常活动次数", example = "3")
        private Integer abnormalActivities;

        /**
         * 平均风险评分
         */
        @DecimalMin(value = "0.0", message = "平均风险评分不能小于0")
        @DecimalMax(value = "100.0", message = "平均风险评分不能大于100")
        @Schema(description = "平均风险评分", example = "25.8")
        private BigDecimal averageRiskScore;

        /**
         * 最高风险评分
         */
        @DecimalMin(value = "0.0", message = "最高风险评分不能小于0")
        @DecimalMax(value = "100.0", message = "最高风险评分不能大于100")
        @Schema(description = "最高风险评分", example = "85.2")
        private BigDecimal highestRiskScore;

        /**
         * 监控时长（小时）
         */
        @Min(value = 0, message = "监控时长不能为负数")
        @Schema(description = "监控时长（小时）", example = "24.0")
        private BigDecimal monitoringDuration;

        /**
         * 活动频率（次/小时）
         */
        @Min(value = 0, message = "活动频率不能为负数")
        @Schema(description = "活动频率（次/小时）", example = "6.5")
        private BigDecimal activityFrequency;
    }

    // ==================== 业务方法 ====================

    /**
     * 判断是否为高风险用户
     *
     * @return true如果是高风险用户
     */
    public boolean isHighRisk() {
        return riskScore != null && riskScore.compareTo(new BigDecimal("60")) >= 0;
    }

    /**
     * 判断是否需要立即处理
     *
     * @return true如果需要立即处理
     */
    public boolean requiresImmediateAttention() {
        return isHighRisk() && Boolean.TRUE.equals(alertTriggered) && "CRITICAL".equals(alertLevel);
    }

    /**
     * 判断是否有可疑活动
     *
     * @return true如果有可疑活动
     */
    public boolean hasSuspiciousActivities() {
        return suspiciousActivities != null && !suspiciousActivities.isEmpty();
    }

    /**
     * 判断是否有异常行为模式
     *
     * @return true如果有异常行为模式
     */
    public boolean hasAbnormalPatterns() {
        return abnormalBehaviorPatterns != null && !abnormalBehaviorPatterns.isEmpty();
    }

    /**
     * 获取风险等级描述
     *
     * @return 风险等级描述
     */
    public String getRiskLevelDescription() {
        switch (riskLevel) {
            case "LOW":
                return "低风险";
            case "MEDIUM":
                return "中风险";
            case "HIGH":
                return "高风险";
            case "CRITICAL":
                return "严重风险";
            default:
                return "未知";
        }
    }

    /**
     * 获取监控状态描述
     *
     * @return 监控状态描述
     */
    public String getMonitoringStatusDescription() {
        switch (monitoringStatus) {
            case "ACTIVE":
                return "活跃监控";
            case "PAUSED":
                return "暂停监控";
            case "STOPPED":
                return "停止监控";
            case "REVIEW":
                return "待审核";
            default:
                return "未知状态";
        }
    }

    /**
     * 获取告警级别描述
     *
     * @return 告警级别描述
     */
    public String getAlertLevelDescription() {
        switch (alertLevel) {
            case "INFO":
                return "信息";
            case "WARNING":
                return "警告";
            case "ERROR":
                return "错误";
            case "CRITICAL":
                return "严重";
            default:
                return "未知";
        }
    }

    /**
     * 获取处理状态描述
     *
     * @return 处理状态描述
     */
    public String getProcessingStatusDescription() {
        switch (processingStatus) {
            case "PENDING":
                return "待处理";
            case "PROCESSING":
                return "处理中";
            case "RESOLVED":
                return "已解决";
            case "ESCALATED":
                return "已升级";
            default:
                return "未知状态";
        }
    }

    /**
     * 获取可疑活动数量
     *
     * @return 可疑活动数量
     */
    public int getSuspiciousActivityCount() {
        return suspiciousActivities != null ? suspiciousActivities.size() : 0;
    }

    /**
     * 获取异常模式数量
     *
     * @return 异常模式数量
     */
    public int getAbnormalPatternCount() {
        return abnormalBehaviorPatterns != null ? abnormalBehaviorPatterns.size() : 0;
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
     * 验证结果数据的完整性
     *
     * @return 验证结果
     */
    public boolean isValid() {
        if (userId == null) {
            return false;
        }
        if (riskScore != null && (riskScore.compareTo(new BigDecimal("0")) < 0 || riskScore.compareTo(new BigDecimal("100")) > 0)) {
            return false;
        }
        return true;
    }

    /**
     * 创建高风险监控结果
     *
     * @param userId 用户ID
     * @param riskScore 风险评分
     * @param alertMessage 告警消息
     * @return 高风险监控结果对象
     */
    public static BehaviorMonitoringResult createHighRisk(Long userId, BigDecimal riskScore, String alertMessage) {
        return BehaviorMonitoringResult.builder()
                .userId(userId)
                .riskScore(riskScore)
                .riskLevel("HIGH")
                .alertTriggered(true)
                .alertLevel("WARNING")
                .alertMessage(alertMessage)
                .monitoringStatus("ACTIVE")
                .requiresManualReview(true)
                .processingStatus("PENDING")
                .analysisTime(LocalDateTime.now())
                .build();
    }

    /**
     *创建正常监控结果
     *
     * @param userId 用户ID
     * @param riskScore 风险评分
     * @return 正常监控结果对象
     */
    public static BehaviorMonitoringResult createNormal(Long userId, BigDecimal riskScore) {
        return BehaviorMonitoringResult.builder()
                .userId(userId)
                .riskScore(riskScore)
                .riskLevel("LOW")
                .alertTriggered(false)
                .monitoringStatus("ACTIVE")
                .requiresManualReview(false)
                .analysisTime(LocalDateTime.now())
                .build();
    }
}