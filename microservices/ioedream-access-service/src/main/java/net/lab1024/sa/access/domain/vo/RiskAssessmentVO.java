package net.lab1024.sa.access.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 风险评估视图对象
 * <p>
 * 访问风险评估结果的数据传输对象
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
@Schema(description = "风险评估信息")
public class RiskAssessmentVO {

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    /**
     * 区域ID
     */
    @Schema(description = "区域ID", example = "2001")
    private Long areaId;

    /**
     * 访问类型
     */
    @Schema(description = "访问类型", example = "CARD_ACCESS")
    private String accessType;

    /**
     * 风险评分（0-100）
     */
    @Schema(description = "风险评分（0-100）", example = "35.5")
    private BigDecimal riskScore;

    /**
     * 风险等级
     * LOW - 低风险
     * MEDIUM - 中等风险
     * HIGH - 高风险
     * CRITICAL - 严重风险
     */
    @Schema(description = "风险等级", example = "MEDIUM")
    private String riskLevel;

    /**
     * 风险因子
     */
    @Schema(description = "风险因子")
    private Map<String, Integer> riskFactors;

    /**
     * 评估时间
     */
    @Schema(description = "评估时间", example = "2025-01-30T15:45:00")
    private LocalDateTime assessmentTime;

    /**
     * 添加风险因子
     */
    public void addRiskFactor(String factorType, Integer score) {
        if (riskFactors == null) {
            riskFactors = new HashMap<>();
        }
        riskFactors.put(factorType, score);
    }
}