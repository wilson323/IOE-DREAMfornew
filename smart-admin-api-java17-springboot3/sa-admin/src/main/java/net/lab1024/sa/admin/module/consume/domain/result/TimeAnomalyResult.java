package net.lab1024.sa.admin.module.consume.domain.result;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 时间异常检测结果
 * 严格遵循repowiki规范：数据传输对象，包含时间异常检测的结果信息
 *
 * @author SmartAdmin Team
 * @date 2025/11/19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeAnomalyResult {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 消费记录ID
     */
    private Long consumeRecordId;

    /**
     * 消费时间
     */
    private LocalDateTime consumeTime;

    /**
     * 消费时段：MORNING-早晨，NOON-中午，AFTERNOON-下午，EVENING-晚上，NIGHT-深夜
     */
    private String timeSlot;

    /**
     * 正常消费时间段开始
     */
    private LocalTime normalTimeStart;

    /**
     * 正常消费时间段结束
     */
    private LocalTime normalTimeEnd;

    /**
     * 时间偏离分钟数
     */
    private Integer deviationMinutes;

    /**
     * 异常置信度（0-100）
     */
    private BigDecimal anomalyConfidence;

    /**
     * 风险等级：LOW-低风险，MEDIUM-中风险，HIGH-高风险
     */
    private String riskLevel;

    /**
     * 是否首次在该时段消费
     */
    private Boolean isFirstTimeSlot;

    /**
     * 历史该时段消费次数
     */
    private Integer historicalTimeSlotCount;

    /**
     * 距离上次消费小时数
     */
    private Integer hoursSinceLastConsume;

    /**
     * 时间模式：REGULAR-规律，IRREGULAR-不规律，BATCH-批量消费
     */
    private String timePattern;

    /**
     * 是否在用餐时间
     */
    private Boolean isMealTime;

    /**
     * 是否在工作时间
     */
    private Boolean isWorkingHours;

    /**
     * 检测时间
     */
    private LocalDateTime detectionTime;

    /**
     * 异常描述
     */
    private String anomalyDescription;

    /**
     * 建议处理方式
     */
    private String recommendedAction;

    /**
     * 是否需要人工审核
     */
    private Boolean requiresManualReview;

    /**
     * 匹配的规则ID
     */
    private Long ruleId;

    /**
     * 匹配的规则名称
     */
    private String ruleName;

    /**
     * 异常类型：EARLY-过早，LATE-过晚，UNUSUAL-异常时段，FREQUENT-过于频繁
     */
    private String anomalyType;

    /**
     * 时间可信度评分（0-100）
     */
    private BigDecimal timeTrustScore;

    /**
     * 24小时内消费次数
     */
    private Integer consumeCount24h;

    /**
     * 1小时内消费次数
     */
    private Integer consumeCount1h;

    /**
     * 检测模型版本
     */
    private String detectionModelVersion;

    /**
     * 扩展信息
     */
    private String extendedInfo;

    /**
     * 创建深夜消费异常结果
     */
    public static TimeAnomalyResult lateNightConsume(Long userId, Long consumeRecordId, LocalDateTime consumeTime) {
        return TimeAnomalyResult.builder()
                .userId(userId)
                .consumeRecordId(consumeRecordId)
                .consumeTime(consumeTime)
                .timeSlot("NIGHT")
                .anomalyConfidence(new BigDecimal("85.0"))
                .riskLevel("MEDIUM")
                .requiresManualReview(true)
                .detectionTime(LocalDateTime.now())
                .anomalyDescription("深夜时段消费")
                .recommendedAction("建议确认是否为本人操作")
                .anomalyType("UNUSUAL")
                .build();
    }

    /**
     * 创建过于频繁消费异常结果
     */
    public static TimeAnomalyResult frequentConsume(Long userId, Long consumeRecordId, LocalDateTime consumeTime, Integer consumeCount1h) {
        return TimeAnomalyResult.builder()
                .userId(userId)
                .consumeRecordId(consumeRecordId)
                .consumeTime(consumeTime)
                .consumeCount1h(consumeCount1h)
                .anomalyConfidence(new BigDecimal("75.0"))
                .riskLevel("MEDIUM")
                .requiresManualReview(false)
                .detectionTime(LocalDateTime.now())
                .anomalyDescription("短时间内消费次数过多")
                .recommendedAction("建议监控后续消费行为")
                .anomalyType("FREQUENT")
                .build();
    }

    /**
     * 创建非工作时间消费异常结果
     */
    public static TimeAnomalyResult nonWorkingHours(Long userId, Long consumeRecordId, LocalDateTime consumeTime) {
        return TimeAnomalyResult.builder()
                .userId(userId)
                .consumeRecordId(consumeRecordId)
                .consumeTime(consumeTime)
                .isWorkingHours(false)
                .anomalyConfidence(new BigDecimal("65.0"))
                .riskLevel("LOW")
                .requiresManualReview(false)
                .detectionTime(LocalDateTime.now())
                .anomalyDescription("非工作时间消费")
                .anomalyType("UNUSUAL")
                .build();
    }

    /**
     * 检查是否为高风险
     */
    public boolean isHighRisk() {
        return "HIGH".equals(riskLevel);
    }

    /**
     * 检查是否需要人工审核
     */
    public boolean needsReview() {
        return Boolean.TRUE.equals(requiresManualReview);
    }

    /**
     * 获取风险等级描述
     */
    public String getRiskLevelDescription() {
        switch (riskLevel) {
            case "LOW":
                return "低风险";
            case "MEDIUM":
                return "中风险";
            case "HIGH":
                return "高风险";
            default:
                return "未知风险";
        }
    }

    /**
     * 获取时段描述
     */
    public String getTimeSlotDescription() {
        switch (timeSlot) {
            case "MORNING":
                return "早晨";
            case "NOON":
                return "中午";
            case "AFTERNOON":
                return "下午";
            case "EVENING":
                return "晚上";
            case "NIGHT":
                return "深夜";
            default:
                return "未知时段";
        }
    }

    /**
     * 获取格式化的偏离时间
     */
    public String getFormattedDeviationTime() {
        if (deviationMinutes == null) {
            return "0分钟";
        }
        if (deviationMinutes < 60) {
            return deviationMinutes + "分钟";
        } else {
            int hours = deviationMinutes / 60;
            int minutes = deviationMinutes % 60;
            if (minutes == 0) {
                return hours + "小时";
            } else {
                return hours + "小时" + minutes + "分钟";
            }
        }
    }
}