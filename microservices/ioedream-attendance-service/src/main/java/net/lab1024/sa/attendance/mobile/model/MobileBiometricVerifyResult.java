package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 移动端生物识别验证结果
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "移动端生物识别验证结果")
public class MobileBiometricVerifyResult {

    @Schema(description = "是否验证成功", required = true, example = "true")
    private Boolean success;

    @Schema(description = "验证分数", required = true, example = "0.95")
    private Double verificationScore;

    @Schema(description = "生物识别类型", required = true,
            example = "FACE", allowableValues = {"FACE", "FINGERPRINT", "IRIS", "VOICE"})
    private String biometricType;

    @Schema(description = "匹配的用户ID", example = "1001")
    private Long matchedUserId;

    @Schema(description = "匹配的模板ID", example = "TEMPLATE_001")
    private String matchedTemplateId;

    @Schema(description = "验证状态", required = true,
            example = "VERIFIED", allowableValues = {"VERIFIED", "NOT_VERIFIED", "TEMPLATE_NOT_FOUND", "QUALITY_POOR"})
    private String verificationStatus;

    @Schema(description = "失败原因", example = "TEMPLATE_NOT_FOUND")
    private String failureReason;

    @Schema(description = "活体检测结果", example = "true")
    private Boolean livenessResult;

    @Schema(description = "防欺骗检测结果", example = "true")
    private Boolean antiSpoofingResult;

    @Schema(description = "验证耗时（毫秒）", example = "1250")
    private Long verificationTimeMs;

    @Schema(description = "验证时间戳", example = "1703020800000")
    private Long verificationTimestamp;

    @Schema(description = "详细验证结果")
    private DetailedVerificationResult detailedResult;

    @Schema(description = "建议信息", example = "验证通过")
    private String recommendation;

    @Schema(description = "错误码", example = "BIOMETRIC_VERIFICATION_SUCCESS")
    private String errorCode;

    @Schema(description = "扩展属性")
    private Map<String, Object> extendedAttributes;

    /**
     * 详细验证结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "详细验证结果")
    public static class DetailedVerificationResult {

        @Schema(description = "特征匹配分数", example = "0.98")
        private Double featureMatchScore;

        @Schema(description = "图像质量分数", example = "0.95")
        private Double imageQualityScore;

        @Schema(description = "活体检测分数", example = "0.99")
        private Double livenessScore;

        @Schema(description = "防欺骗分数", example = "0.97")
        private Double antiSpoofingScore;

        @Schema(description = "置信度", example = "HIGH", allowableValues = {"LOW", "MEDIUM", "HIGH"})
        private String confidence;

        @Schema(description = "数据质量评估")
        private QualityAssessment qualityAssessment;

        @Schema(description = "匹配详情")
        private MatchingDetails matchingDetails;

        @Schema(description = "处理步骤")
        private List<ProcessingStep> processingSteps;
    }

    /**
     * 数据质量评估
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "数据质量评估")
    public static class QualityAssessment {

        @Schema(description = "整体质量等级", example = "EXCELLENT", allowableValues = {"POOR", "FAIR", "GOOD", "EXCELLENT"})
        private String overallQuality;

        @Schema(description = "清晰度分数", example = "0.94")
        private Double clarityScore;

        @Schema(description = "亮度分数", example = "0.88")
        private Double brightnessScore;

        @Schema(description = "对比度分数", example = "0.92")
        private Double contrastScore;

        @Schema(description = "噪声分数", example = "0.15")
        private Double noiseLevel;

        @Schema(description = "是否有遮挡", example = "false")
        private Boolean hasOcclusion;

        @Schema(description = "遮挡类型", example = "NONE", allowableValues = {"NONE", "MASK", "GLASSES", "HAND"})
        private String occlusionType;

        @Schema(description = "是否满足最低质量要求", example = "true")
        private Boolean meetsMinimumQuality;
    }

    /**
     * 匹配详情
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "匹配详情")
    public static class MatchingDetails {

        @Schema(description = "模板距离", example = "0.05")
        private Double templateDistance;

        @Schema(description = "相似度阈值", example = "0.85")
        private Double similarityThreshold;

        @Schema(description = "匹配候选数量", example = "5")
        private Integer candidateCount;

        @Schema(description = "最佳匹配分数", example = "0.98")
        private Double bestMatchScore;

        @Schema(description = "次佳匹配分数", example = "0.76")
        private Double secondBestScore;

        @Schema(description = "匹配差异", example = "0.22")
        private Double matchDifference;

        @Schema(description = "匹配算法", example = "DEEP_FACE")
        private String matchingAlgorithm;

        @Schema(description = "模板版本", example = "v2.1")
        private String templateVersion;
    }

    /**
     * 处理步骤
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "处理步骤")
    public static class ProcessingStep {

        @Schema(description = "步骤名称", example = "FACE_DETECTION")
        private String stepName;

        @Schema(description = "步骤状态", example = "SUCCESS", allowableValues = {"SUCCESS", "FAILED", "SKIPPED"})
        private String stepStatus;

        @Schema(description = "处理耗时（毫秒）", example = "150")
        private Long processingTimeMs;

        @Schema(description = "步骤详情", example = "检测到1张人脸")
        private String stepDetails;

        @Schema(description = "输出结果", example = "face_detected=true")
        private String outputResult;

        @Schema(description = "错误信息", example = "")
        private String errorMessage;
    }
}
