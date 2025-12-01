package net.lab1024.sa.admin.module.consume.domain.result;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 用户风险评分
 *
 * @author SmartAdmin Team
 * @since 2025-11-22
 */




@Schema(description = "用户风险评分")
public class UserRiskScore {

    @Schema(description = "评分ID")
    private Long scoreId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "综合风险评分")
    private Integer overallRiskScore;

    @Schema(description = "风险等级")
    private String riskLevel;

    @Schema(description = "评分时间")
    private LocalDateTime scoreTime;

    @Schema(description = "评分有效期")
    private LocalDateTime validUntil;

    @Schema(description = "风险因素分析")
    private List<RiskFactor> riskFactors;

    @Schema(description = "风险指标")
    private RiskMetrics riskMetrics;

    @Schema(description = "建议措施")
    private List<String> recommendedActions;

    @Schema(description = "历史趋势")
    private List<ScoreHistory> scoreHistory;

    /**
     * 风险因素内部类
     */
    
    
    
    
    @Schema(description = "风险因素")
    public static class RiskFactor {
        @Schema(description = "因素ID")
        private Long factorId;

        @Schema(description = "因素类型")
        private String factorType;

        @Schema(description = "因素描述")
        private String description;

        @Schema(description = "权重")
        private Double weight;

        @Schema(description = "评分")
        private Integer score;

        @Schema(description = "风险等级")
        private String riskLevel;
    }

    /**
     * 风险指标内部类
     */
    
    
    
    
    @Schema(description = "风险指标")
    public static class RiskMetrics {
        @Schema(description = "交易频率评分")
        private Integer frequencyScore;

        @Schema(description = "交易金额评分")
        private Integer amountScore;

        @Schema(description = "交易地点评分")
        private Integer locationScore;

        @Schema(description = "交易时间评分")
        private Integer timeScore;

        @Schema(description = "设备评分")
        private Integer deviceScore;

        @Schema(description = "行为模式评分")
        private Integer behaviorScore;
    }

    /**
     * 评分历史内部类
     */
    
    
    
    
    @Schema(description = "评分历史")
    public static class ScoreHistory {
        @Schema(description = "历史ID")
        private Long historyId;

        @Schema(description = "评分时间")
        private LocalDateTime scoreTime;

        @Schema(description = "评分")
        private Integer score;

        @Schema(description = "风险等级")
        private String riskLevel;

        @Schema(description = "主要变化因素")
        private String mainChangeFactor;
    }

    /**
     * 创建用户风险评分实例
     */
    public static UserRiskScore create(Long userId, int overallScore, List<RiskFactor> factors) {
        return UserRiskScore.builder()
                .scoreId(System.currentTimeMillis())
                .userId(userId)
                .overallRiskScore(overallScore)
                .riskLevel(determineRiskLevel(overallScore))
                .scoreTime(LocalDateTime.now())
                .validUntil(LocalDateTime.now().plusDays(7))
                .riskFactors(factors)
                .build();
    }

    /**
     * 创建高风险评分实例
     */
    public static UserRiskScore highRisk(Long userId, String reason, List<String> actions) {
        return UserRiskScore.builder()
                .scoreId(System.currentTimeMillis())
                .userId(userId)
                .overallRiskScore(85)
                .riskLevel("HIGH")
                .scoreTime(LocalDateTime.now())
                .validUntil(LocalDateTime.now().plusDays(3))
                .recommendedActions(actions)
                .build();
    }

    /**
     * 创建低风险评分实例
     */
    public static UserRiskScore lowRisk(Long userId) {
        return UserRiskScore.builder()
                .scoreId(System.currentTimeMillis())
                .userId(userId)
                .overallRiskScore(15)
                .riskLevel("LOW")
                .scoreTime(LocalDateTime.now())
                .validUntil(LocalDateTime.now().plusDays(14))
                .build();
    }

    /**
     * 更新风险评分
     */
    public UserRiskScore updateScore(int newScore, List<RiskFactor> newFactors) {
        return UserRiskScore.builder()
                .scoreId(this.scoreId)
                .userId(this.userId)
                .overallRiskScore(newScore)
                .riskLevel(determineRiskLevel(newScore))
                .scoreTime(LocalDateTime.now())
                .validUntil(this.validUntil)
                .riskFactors(newFactors)
                .riskMetrics(this.riskMetrics)
                .recommendedActions(this.recommendedActions)
                .scoreHistory(this.scoreHistory)
                .build();
    }

    /**
     * 确定风险等级
     */
    private static String determineRiskLevel(int score) {
        if (score >= 80)
            return "HIGH";
        if (score >= 60)
            return "MEDIUM";
        if (score >= 40)
            return "LOW";
        return "MINIMAL";
    }

    /**
     * 检查评分是否有效
     */
    public boolean isValid() {
        return LocalDateTime.now().isBefore(validUntil);
    }

    /**
     * 检查是否需要重新评分
     */
    public boolean needsRescore() {
        return !isValid() || hasSignificantChanges();
    }

    /**
     * 检查是否有显著变化
     */
    private boolean hasSignificantChanges() {
        if (scoreHistory == null || scoreHistory.size() < 2) {
            return false;
        }

        ScoreHistory latest = scoreHistory.get(0);
        ScoreHistory previous = scoreHistory.get(1);

        return Math.abs(latest.getScore() - previous.getScore()) > 20;
    }
}
