package net.lab1024.sa.base.module.support.rbac;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 策略评估结果
 *
 * @author SmartAdmin Team
 * @since 2025-11-23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyEvaluationResult {

    /**
     * 是否允许访问
     */
    private Boolean allowed;

    /**
     * 决策原因
     */
    private String reason;

    /**
     * 是否匹配条件策略
     */
    private Boolean conditionMatched;

    /**
     * 决策类型
     */
    private String decisionType;

    /**
     * 评估时间戳
     */
    private Long evaluationTime;

    /**
     * 扩展信息
     */
    private String extendInfo;

    /**
     * 创建允许结果
     */
    public static PolicyEvaluationResult allowed(String reason) {
        return PolicyEvaluationResult.builder()
                .allowed(true)
                .reason(reason)
                .conditionMatched(false)
                .decisionType("ALLOW")
                .evaluationTime(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建拒绝结果
     */
    public static PolicyEvaluationResult denied(String reason) {
        return PolicyEvaluationResult.builder()
                .allowed(false)
                .reason(reason)
                .conditionMatched(false)
                .decisionType("DENY")
                .evaluationTime(System.currentTimeMillis())
                .build();
    }

    /**
     * 从PolicyDecision转换
     */
    public static PolicyEvaluationResult from(PolicyEvaluator.PolicyDecision decision) {
        return PolicyEvaluationResult.builder()
                .allowed(decision.isAllowed())
                .reason(decision.getReason())
                .conditionMatched(decision.isConditionMatched())
                .decisionType(decision.isAllowed() ? "ALLOW" : "DENY")
                .evaluationTime(System.currentTimeMillis())
                .build();
    }
}
