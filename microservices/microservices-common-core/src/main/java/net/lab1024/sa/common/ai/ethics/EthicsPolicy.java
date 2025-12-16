package net.lab1024.sa.common.ai.ethics;

import java.util.Optional;

/**
 * 伦理策略接口。
 *
 * <p>
 * 策略只负责“判断是否命中 + 给出决策”，不负责执行真实动作。
 * 返回 Optional.empty() 表示本策略不命中，交由后续策略继续判断。
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-14
 */
public interface EthicsPolicy {

    /**
     * 获取策略ID（用于审计与灰度）。
     *
     * @return 策略ID
     */
    String getPolicyId();

    /**
     * 执行策略判定。
     *
     * @param request 伦理请求
     * @return Optional决策：
     * - empty：不命中，继续
     * - present：命中并给出决策
     */
    Optional<EthicsDecision> evaluate(EthicsRequest request);

    // TRACE-ethics-20251214-策略接口允许按域拆分与组合
}
