package net.lab1024.sa.common.ai.ethics;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * 默认伦理策略集合（最小可用）。
 *
 * <p>
 * 说明：该集合仅提供“企业级默认安全底线”。
 * 业务方应基于自身场景新增更严格的策略（例如：数据脱敏、权限校验、操作审计等）。
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-14
 */
public final class DefaultEthicsPolicies {

    private DefaultEthicsPolicies() {
    }

    /**
     * 创建一组默认策略（按顺序执行）。
     *
     * @return 策略列表
     */
    public static List<EthicsPolicy> createDefaultPolicies() {
        return List.of(
                new DenyUnknownActionPolicy(),
                new DenySensitiveKeywordLeakPolicy(),
                new ReviewHighRiskActionPolicy()
        );
    }

    // TRACE-ethics-20251214-默认规则集可快速落地并支持后续扩展

    /**
     * 对未知动作默认拒绝（Fail Closed）。
     */
    static final class DenyUnknownActionPolicy implements EthicsPolicy {

        private static final Set<String> KNOWN_ACTIONS = Set.of(
                "openai.chat.completions",
                "openai.embeddings",
                "agent.tool.invoke",
                "agent.self_modify",
                "data.export",
                "system.shutdown"
        );

        @Override
        public String getPolicyId() {
            return "default.deny_unknown_action";
        }

        @Override
        public Optional<EthicsDecision> evaluate(EthicsRequest request) {
            if (request == null) {
                return Optional.of(EthicsDecision.deny(getPolicyId(), "NULL_REQUEST", "请求为空", null));
            }
            String action = request.getAction();
            if (action == null || action.trim().isEmpty()) {
                return Optional.of(EthicsDecision.deny(getPolicyId(), "EMPTY_ACTION", "动作为空", request.getTraceId()));
            }
            if (!KNOWN_ACTIONS.contains(action.trim())) {
                return Optional.of(EthicsDecision.deny(
                        getPolicyId(),
                        "UNKNOWN_ACTION",
                        "未知动作，默认拒绝: " + action,
                        request.getTraceId()
                ));
            }
            return Optional.empty();
        }
    }

    /**
     * 检测疑似敏感信息外泄风险。
     *
     * <p>
     * 仅做底线拦截：若输入摘要（attributes.input）包含高风险关键词，则拒绝。
     * </p>
     */
    static final class DenySensitiveKeywordLeakPolicy implements EthicsPolicy {

        private static final String ATTR_INPUT = "input";

        private static final Set<String> BLOCK_KEYWORDS;

        static {
            Set<String> set = new HashSet<>();
            set.add("password");
            set.add("secret");
            set.add("api_key");
            set.add("access_key");
            set.add("private_key");
            set.add("身份证");
            set.add("银行卡");
            set.add("密码");
            BLOCK_KEYWORDS = Set.copyOf(set);
        }

        @Override
        public String getPolicyId() {
            return "default.deny_sensitive_keyword";
        }

        @Override
        public Optional<EthicsDecision> evaluate(EthicsRequest request) {
            if (request == null) {
                return Optional.empty();
            }
            String input = request.getAttributeAsString(ATTR_INPUT);
            if (input == null || input.trim().isEmpty()) {
                return Optional.empty();
            }

            String lower = input.toLowerCase(Locale.ROOT);
            for (String keyword : BLOCK_KEYWORDS) {
                if (keyword == null || keyword.isEmpty()) {
                    continue;
                }
                if (lower.contains(keyword.toLowerCase(Locale.ROOT))) {
                    return Optional.of(EthicsDecision.deny(
                            getPolicyId(),
                            "SENSITIVE_LEAK_RISK",
                            "检测到疑似敏感信息泄露风险关键词: " + keyword,
                            request.getTraceId()
                    ));
                }
            }
            return Optional.empty();
        }

        // TRACE-ethics-20251214-敏感信息底线拦截防止高风险外泄
    }

    /**
     * 高风险动作要求复核。
     */
    static final class ReviewHighRiskActionPolicy implements EthicsPolicy {

        @Override
        public String getPolicyId() {
            return "default.review_high_risk_action";
        }

        @Override
        public Optional<EthicsDecision> evaluate(EthicsRequest request) {
            if (request == null) {
                return Optional.empty();
            }
            String action = request.getAction();
            if (action == null) {
                return Optional.empty();
            }
            if ("agent.self_modify".equals(action) || "system.shutdown".equals(action) || "data.export".equals(action)) {
                return Optional.of(EthicsDecision.requireReview(
                        getPolicyId(),
                        "HIGH_RISK_ACTION",
                        "高风险动作需要人工复核: " + action,
                        request.getTraceId()
                ));
            }
            return Optional.empty();
        }
    }
}
