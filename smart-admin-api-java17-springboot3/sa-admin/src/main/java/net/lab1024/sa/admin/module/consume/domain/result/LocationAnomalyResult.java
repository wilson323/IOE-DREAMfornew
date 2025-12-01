package net.lab1024.sa.admin.module.consume.domain.result;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 位置异常检测结果
 * 严格遵循repowiki规范：数据传输对象，包含位置异常检测的结果信息
 *
 * @author SmartAdmin Team
 * @date 2025/11/19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationAnomalyResult {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 消费记录ID
     */
    private Long consumeRecordId;

    /**
     * 异常位置ID
     */
    private Long locationId;

    /**
     * 异常位置名称
     */
    private String locationName;

    /**
     * 正常位置ID列表
     */
    private String normalLocationIds;

    /**
     * 正常位置名称列表
     */
    private String normalLocationNames;

    /**
     * 位置偏离距离（米）
     */
    private BigDecimal deviationDistance;

    /**
     * 异常置信度（0-100）
     */
    private BigDecimal anomalyConfidence;

    /**
     * 风险等级：LOW-低风险，MEDIUM-中风险，HIGH-高风险
     */
    private String riskLevel;

    /**
     * 是否首次在此位置消费
     */
    private Boolean isFirstTimeLocation;

    /**
     * 历史消费次数
     */
    private Integer historicalVisitCount;

    /**
     * 距离上次消费天数
     */
    private Integer daysSinceLastVisit;

    /**
     * 位置类型：WORK-工作地点，HOME-家庭住址，FREQUENT-经常访问，RARE-罕见位置
     */
    private String locationType;

    /**
     * GPS坐标纬度
     */
    private BigDecimal latitude;

    /**
     * GPS坐标经度
     */
    private BigDecimal longitude;

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
     * 异常类型：DISTANCE-距离异常，NEW_LOCATION-新位置，RARE_LOCATION-罕见位置
     */
    private String anomalyType;

    /**
     * 位置可信度评分（0-100）
     */
    private BigDecimal locationTrustScore;

    /**
     * 时间窗口内相同位置访问次数
     */
    private Integer sameLocationVisitCount;

    /**
     * 检测模型版本
     */
    private String detectionModelVersion;

    /**
     * 扩展信息
     */
    private String extendedInfo;

    /**
     * 创建低风险异常结果
     */
    public static LocationAnomalyResult lowRisk(Long userId, Long consumeRecordId, String locationName, BigDecimal deviationDistance) {
        return LocationAnomalyResult.builder()
                .userId(userId)
                .consumeRecordId(consumeRecordId)
                .locationName(locationName)
                .deviationDistance(deviationDistance)
                .riskLevel("LOW")
                .anomalyConfidence(new BigDecimal("60.0"))
                .requiresManualReview(false)
                .detectionTime(LocalDateTime.now())
                .anomalyDescription("位置轻微偏离，风险较低")
                .build();
    }

    /**
     * 创建高风险异常结果
     */
    public static LocationAnomalyResult highRisk(Long userId, Long consumeRecordId, String locationName, BigDecimal deviationDistance) {
        return LocationAnomalyResult.builder()
                .userId(userId)
                .consumeRecordId(consumeRecordId)
                .locationName(locationName)
                .deviationDistance(deviationDistance)
                .riskLevel("HIGH")
                .anomalyConfidence(new BigDecimal("90.0"))
                .requiresManualReview(true)
                .detectionTime(LocalDateTime.now())
                .anomalyDescription("位置严重偏离，风险较高")
                .recommendedAction("建议进行身份验证")
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
     * 获取格式化的偏离距离
     */
    public String getFormattedDeviationDistance() {
        if (deviationDistance == null) {
            return "0米";
        }
        if (deviationDistance.compareTo(new BigDecimal("1000")) < 0) {
            return deviationDistance.setScale(0, BigDecimal.ROUND_HALF_UP) + "米";
        } else {
            return deviationDistance.divide(new BigDecimal("1000"), 2, BigDecimal.ROUND_HALF_UP) + "公里";
        }
    }
}