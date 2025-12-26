package net.lab1024.sa.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 多因子认证结果视图对象
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用OpenAPI 3.0注解（@Schema）
 * - 提供完整的认证结果信息
 * - 包含详细的验证日志
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@Schema(description = "多因子认证结果")
public class MultiFactorAuthenticationResultVO {

    /**
     * 认证是否通过
     */
    @Schema(description = "认证是否通过", example = "true")
    private Boolean authenticated;

    /**
     * 认证评分（0-100）
     */
    @Schema(description = "认证评分", example = "95")
    private Integer score;

    /**
     * 认证模式
     */
    @Schema(description = "认证模式", example = "STRICT")
    private String authenticationMode;

    /**
     * 认证耗时（毫秒）
     */
    @Schema(description = "认证耗时（毫秒）", example = "520")
    private Long duration;

    /**
     * 失败原因
     */
    @Schema(description = "失败原因", example = "人脸认证失败")
    private String failureReason;

    /**
     * 各因子认证结果
     */
    @Schema(description = "各因子认证结果")
    private List<FactorResult> factorResults;

    /**
     * 认证时间
     */
    @Schema(description = "认证时间", example = "2025-12-26T10:30:00")
    private LocalDateTime authenticatedAt;

    /**
     * 因子认证结果
     */
    @Data
    @Schema(description = "因子认证结果")
    public static class FactorResult {

        /**
         * 认证类型
         */
        @Schema(description = "认证类型", example = "FACE")
        private String type;

        /**
         * 是否通过
         */
        @Schema(description = "是否通过", example = "true")
        private Boolean passed;

        /**
         * 置信度（0-1）
         */
        @Schema(description = "置信度", example = "0.95")
        private Double confidence;

        /**
         * 验证耗时（毫秒）
         */
        @Schema(description = "验证耗时（毫秒）", example = "250")
        private Long duration;

        /**
         * 失败原因
         */
        @Schema(description = "失败原因", example = "特征不匹配")
        private String failureReason;

        /**
         * 验证时间
         */
        @Schema(description = "验证时间", example = "2025-12-26T10:30:00")
        private LocalDateTime verifiedAt;
    }

    /**
     * 创建成功结果
     */
    public static MultiFactorAuthenticationResultVO success(String authenticationMode, List<FactorResult> factorResults, Long duration) {
        MultiFactorAuthenticationResultVO result = new MultiFactorAuthenticationResultVO();
        result.setAuthenticated(true);
        result.setAuthenticationMode(authenticationMode);
        result.setFactorResults(factorResults);
        result.setDuration(duration);
        result.setAuthenticatedAt(LocalDateTime.now());

        // 计算平均分数
        double avgScore = factorResults.stream()
                .filter(fr -> fr.getConfidence() != null)
                .mapToDouble(FactorResult::getConfidence)
                .average()
                .orElse(0.0);
        result.setScore((int) (avgScore * 100));

        return result;
    }

    /**
     * 创建失败结果
     */
    public static MultiFactorAuthenticationResultVO failure(String authenticationMode, List<FactorResult> factorResults, String failureReason, Long duration) {
        MultiFactorAuthenticationResultVO result = new MultiFactorAuthenticationResultVO();
        result.setAuthenticated(false);
        result.setAuthenticationMode(authenticationMode);
        result.setFactorResults(factorResults);
        result.setFailureReason(failureReason);
        result.setDuration(duration);
        result.setAuthenticatedAt(LocalDateTime.now());
        result.setScore(0);
        return result;
    }
}
