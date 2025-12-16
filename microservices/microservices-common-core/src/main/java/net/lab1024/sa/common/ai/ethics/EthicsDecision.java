package net.lab1024.sa.common.ai.ethics;

import java.time.Instant;
import java.util.Objects;

/**
 * 伦理防火墙判定结果。
 *
 * <p>
 * 该对象用于：
 * <ul>
 *     <li>对外提供一致的ALLOW/DENY/REQUIRE_REVIEW语义</li>
 *     <li>记录命中策略（policyId）与可审计原因（reasonCode/reasonMessage）</li>
 *     <li>提供生成时间与追踪ID，便于跨服务日志串联</li>
 * </ul>
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-14
 */
public final class EthicsDecision {

    private final EthicsDecisionType decisionType;
    private final String policyId;
    private final String reasonCode;
    private final String reasonMessage;
    private final String traceId;
    private final Instant decidedAt;

    private EthicsDecision(
            EthicsDecisionType decisionType,
            String policyId,
            String reasonCode,
            String reasonMessage,
            String traceId,
            Instant decidedAt) {
        this.decisionType = Objects.requireNonNull(decisionType, "decisionType不能为空");
        this.policyId = policyId;
        this.reasonCode = reasonCode;
        this.reasonMessage = reasonMessage;
        this.traceId = traceId;
        this.decidedAt = decidedAt != null ? decidedAt : Instant.now();
    }

    /**
     * 创建“允许”结果。
     *
     * @param policyId 策略ID（可为空，表示未命中任何策略）
     * @param traceId  追踪ID（建议从调用链上下文传入）
     * @return EthicsDecision
     */
    public static EthicsDecision allow(String policyId, String traceId) {
        return new EthicsDecision(EthicsDecisionType.ALLOW, policyId, "ALLOW", "允许执行", traceId, Instant.now());
    }

    // TRACE-ethics-20251214-拒绝与复核结果统一化便于审计

    /**
     * 创建“拒绝”结果。
     *
     * @param policyId       策略ID
     * @param reasonCode     原因码（用于统计与告警聚合）
     * @param reasonMessage  原因描述（面向人阅读）
     * @param traceId        追踪ID
     * @return EthicsDecision
     */
    public static EthicsDecision deny(String policyId, String reasonCode, String reasonMessage, String traceId) {
        return new EthicsDecision(
                EthicsDecisionType.DENY,
                policyId,
                safe(reasonCode, "DENY"),
                safe(reasonMessage, "拒绝执行"),
                traceId,
                Instant.now()
        );
    }

    /**
     * 创建“需要复核”结果。
     *
     * @param policyId       策略ID
     * @param reasonCode     原因码
     * @param reasonMessage  原因描述
     * @param traceId        追踪ID
     * @return EthicsDecision
     */
    public static EthicsDecision requireReview(String policyId, String reasonCode, String reasonMessage, String traceId) {
        return new EthicsDecision(
                EthicsDecisionType.REQUIRE_REVIEW,
                policyId,
                safe(reasonCode, "REQUIRE_REVIEW"),
                safe(reasonMessage, "需要人工复核"),
                traceId,
                Instant.now()
        );
    }

    private static String safe(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }

    public EthicsDecisionType getDecisionType() {
        return decisionType;
    }

    public String getPolicyId() {
        return policyId;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public String getReasonMessage() {
        return reasonMessage;
    }

    // TRACE-ethics-20251214-追踪字段用于跨服务审计串联

    public String getTraceId() {
        return traceId;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }
}
