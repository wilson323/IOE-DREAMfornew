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
 * 生物识别防伪检测结果视图对象
 * <p>
 * 生物识别防伪分析结果的数据传输对象
 * 严格遵循CLADE.md规范：
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
@Schema(description = "生物识别防伪检测结果")
public class BiometricAntiSpoofResultVO {

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    /**
     * 生物识别类型
     */
    @Schema(description = "生物识别类型", example = "FACE")
    private String biometricType;

    /**
     * 活体检测评分（0-100）
     */
    @Schema(description = "活体检测评分（0-100）", example = "96.8")
    private BigDecimal livenessScore;

    /**
     * 深度伪造检测评分（0-100）
     */
    @Schema(description = "深度伪造检测评分（0-100）", example = "94.2")
    private BigDecimal deepfakeScore;

    /**
     * 质量评估评分（0-100）
     */
    @Schema(description = "质量评估评分（0-100）", example = "91.5")
    private BigDecimal qualityScore;

    /**
     * 3D结构分析评分（0-100）
     */
    @Schema(description = "3D结构分析评分（0-100）", example = "89.7")
    private BigDecimal structureScore;

    /**
     * 综合防伪评分（0-100）
     */
    @Schema(description = "综合防伪评分（0-100）", example = "92.8")
    private BigDecimal overallScore;

    /**
     * 是否通过防伪检测
     */
    @Schema(description = "是否通过防伪检测", example = "true")
    private Boolean passedAntiSpoofing;

    /**
     * 风险等级
     * LOW - 低风险
     * MEDIUM - 中等风险
     * HIGH - 高风险
     * CRITICAL - 严重风险
     */
    @Schema(description = "风险等级", example = "LOW")
    private String riskLevel;

    /**
     * 检测详情
     */
    @Schema(description = "检测详情")
    private Map<String, Object> detectionDetails;

    /**
     * 检测到的攻击类型
     */
    @Schema(description = "检测到的攻击类型")
    private List<String> detectedAttackTypes;

    /**
     * 信任度分数
     */
    @Schema(description = "信任度分数", example = "0.98")
    private BigDecimal trustScore;

    /**
     * 处理建议
     */
    @Schema(description = "处理建议", example = "生物识别数据质量良好，可以正常使用")
    private String recommendation;

    /**
     * 检测耗时（毫秒）
     */
    @Schema(description = "检测耗时（毫秒）", example = "156")
    private Long detectionDuration;

    /**
     * 检测时间
     */
    @Schema(description = "检测时间", example = "2025-01-30T15:45:00")
    private LocalDateTime detectionTime;

    /**
     * 设备信息
     */
    @Schema(description = "设备信息", example = "iPhone 13 Pro")
    private String deviceInfo;

    /**
     * 环境条件
     */
    @Schema(description = "环境条件", example = "室内正常光照")
    private String environmentalConditions;

    /**
     * 检测指标详情内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "检测指标详情")
    public static class DetectionMetricVO {

        @Schema(description = "指标名称", example = "眨眼检测")
        private String metricName;

        @Schema(description = "指标值", example = "0.95")
        private BigDecimal metricValue;

        @Schema(description = "阈值", example = "0.7")
        private BigDecimal threshold;

        @Schema(description = "是否通过", example = "true")
        private Boolean passed;

        @Schema(description = "指标描述", example = "检测到自然的眨眼行为")
        private String description;

        @Schema(description = "权重", example = "0.25")
        private BigDecimal weight;
    }

    /**
     * 攻击类型详情内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "攻击类型详情")
    public static class AttackTypeVO {

        @Schema(description = "攻击类型", example = "PHOTO_ATTACK")
        private String attackType;

        @Schema(description = "置信度", example = "0.15")
        private BigDecimal confidence;

        @Schema(description = "描述", example = "检测到照片攻击的可能性")
        private String description;

        @Schema(description = "严重程度", example = "LOW")
        private String severity;

        @Schema(description = "防护建议", example = "要求用户提供更高质量的生物识别数据")
        private String mitigationAdvice;
    }

    /**
     * 环境分析内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "环境分析")
    public static class EnvironmentalAnalysisVO {

        @Schema(description = "光照条件", example = "NORMAL")
        private String lightingCondition;

        @Schema(description = "背景复杂度", example = "LOW")
        private String backgroundComplexity;

        @Schema(description = "设备角度", example = "NORMAL")
        private String deviceAngle;

        @Schema(description = "遮挡情况", example = "NONE")
        private String occlusionStatus;

        @Schema(description = "环境评分", example = "92.5")
        private BigDecimal environmentalScore;
    }
}