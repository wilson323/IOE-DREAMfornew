package net.lab1024.sa.common.ai.ethics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

/**
 * 智能体伦理防火墙。
 *
 * <p>
 * 设计目标：
 * <ul>
 *     <li>默认安全：不确定即拒绝/复核（Fail Closed）</li>
 *     <li>可组合：按策略链（Policy Chain）串联规则</li>
 *     <li>可审计：输出policyId/reasonCode/traceId</li>
 * </ul>
 * </p>
 *
 * <p>
 * 注意：该类为纯Java类，不使用Spring注解；可在微服务中通过配置类注册为Bean。
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-14
 */
@Slf4j
public class EthicsFirewall {

    private final List<EthicsPolicy> policies;

    /**
     * 构造函数。
     *
     * @param policies 策略链（按顺序执行）
     */
    public EthicsFirewall(List<EthicsPolicy> policies) {
        if (policies == null || policies.isEmpty()) {
            this.policies = Collections.emptyList();
        } else {
            this.policies = Collections.unmodifiableList(new ArrayList<>(policies));
        }
    }

    /**
     * 评估一次请求是否允许执行。
     *
     * <p>
     * 判定规则：
     * <ul>
     *     <li>任一策略DENY → 立即拒绝（最高优先级）</li>
     *     <li>若无DENY但存在REQUIRE_REVIEW → 返回复核</li>
     *     <li>全部不命中 → ALLOW（policyId为空）</li>
     * </ul>
     * </p>
     *
     * @param request 伦理请求
     * @return 伦理判定结果
     */
    public EthicsDecision evaluate(EthicsRequest request) {
        if (request == null) {
            return EthicsDecision.deny("core.null_request", "NULL_REQUEST", "请求为空，拒绝执行", null);
        }

        EthicsDecision firstReview = null;
        for (EthicsPolicy policy : policies) {
            if (policy == null) {
                continue;
            }

            Optional<EthicsDecision> decisionOpt;
            try {
                decisionOpt = policy.evaluate(request);
            } catch (Exception e) {
                log.error("[伦理防火墙] 策略执行异常，policyId={}, action={}, actorId={}, error={}",
                        safePolicyId(policy), request.getAction(), request.getActorId(), e.getMessage(), e);
                return EthicsDecision.requireReview(
                        safePolicyId(policy),
                        "POLICY_ERROR",
                        "策略执行异常，需人工复核: " + e.getMessage(),
                        request.getTraceId()
                );
            }

            if (decisionOpt.isEmpty()) {
                continue;
            }

            EthicsDecision decision = decisionOpt.get();
            if (decision.getDecisionType() == EthicsDecisionType.DENY) {
                audit(decision, request);
                return decision;
            }
            if (decision.getDecisionType() == EthicsDecisionType.REQUIRE_REVIEW && firstReview == null) {
                firstReview = decision;
            }
        }

        // TRACE-ethics-20251214-默认安全策略：先拒绝再复核再允许（Fail Closed可扩展）

        if (firstReview != null) {
            audit(firstReview, request);
            return firstReview;
        }

        EthicsDecision allow = EthicsDecision.allow(null, request.getTraceId());
        audit(allow, request);
        return allow;
    }

    /**
     * 获取策略链（只读）。
     *
     * @return 策略列表
     */
    public List<EthicsPolicy> getPolicies() {
        return policies;
    }

    private void audit(EthicsDecision decision, EthicsRequest request) {
        if (decision == null || request == null) {
            return;
        }
        log.info("[伦理防火墙] decision={}, policyId={}, reasonCode={}, action={}, actorId={}, traceId={}",
                decision.getDecisionType(),
                decision.getPolicyId(),
                decision.getReasonCode(),
                request.getAction(),
                request.getActorId(),
                request.getTraceId());
    }

    private static String safePolicyId(EthicsPolicy policy) {
        try {
            String id = policy.getPolicyId();
            if (id == null || id.trim().isEmpty()) {
                return "unknown";
            }
            return id.trim();
        } catch (Exception e) {
            return "unknown";
        }
    }

    // TRACE-ethics-20251214-审计输出确保可观测性与可追溯性
}
