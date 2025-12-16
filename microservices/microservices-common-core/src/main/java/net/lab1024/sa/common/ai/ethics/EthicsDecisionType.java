package net.lab1024.sa.common.ai.ethics;

/**
 * 伦理判定结果类型。
 *
 * <p>
 * 用于表达一次“智能体/AI能力/自动化动作”请求的处置结论。
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-14
 */
public enum EthicsDecisionType {

    /**
     * 允许执行。
     */
    ALLOW,

    /**
     * 直接拒绝执行。
     */
    DENY,

    /**
     * 需要人工复核/二次确认后方可执行。
     */
    REQUIRE_REVIEW;

    // TRACE-ethics-20251214-统一决策枚举以便审计
}
