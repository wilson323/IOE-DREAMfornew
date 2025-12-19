package net.lab1024.sa.visitor.strategy.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.visitor.strategy.IVisitorVerificationStrategy;
import net.lab1024.sa.common.factory.StrategyMarker;
import org.springframework.stereotype.Component;

/**
 * 常客验证策略
 * <p>
 * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
 * 实现边缘验证模式：常客支持边缘验证，设备端完成验证
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Slf4j
@Component
@StrategyMarker(name = "REGULAR_VISITOR", type = "VISITOR_VERIFICATION", priority = 90)
public class RegularVisitorStrategy implements IVisitorVerificationStrategy {

    @Override
    public String getStrategyName() {
        return "常客验证策略";
    }

    @Override
    public VisitorVerificationResult verify(Long visitorId, String verificationData) {
        // TODO: 实现常客边缘验证逻辑
        // 1. 查询电子通行证
        // 2. 验证有效期
        // 3. 设备端已完成验证，软件端只记录结果

        log.debug("[常客策略] 边缘验证（设备端已完成）, visitorId={}", visitorId);
        return VisitorVerificationResult.success(null, "PASS_" + visitorId);
    }

    @Override
    public int getPriority() {
        return 90; // 常客策略优先级较低
    }

    @Override
    public String getStrategyType() {
        return "REGULAR_VISITOR";
    }
}
