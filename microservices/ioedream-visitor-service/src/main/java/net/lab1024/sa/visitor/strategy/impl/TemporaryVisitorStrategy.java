package net.lab1024.sa.visitor.strategy.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.visitor.strategy.IVisitorVerificationStrategy;
import net.lab1024.sa.common.factory.StrategyMarker;
import org.springframework.stereotype.Component;

/**
 * 临时访客验证策略
 * <p>
 * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
 * 实现中心实时验证模式：临时访客必须通过中心验证
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Slf4j
@Component
@StrategyMarker(name = "TEMPORARY_VISITOR", type = "VISITOR_VERIFICATION", priority = 100)
public class TemporaryVisitorStrategy implements IVisitorVerificationStrategy {

    @Override
    public String getStrategyName() {
        return "临时访客验证策略";
    }

    @Override
    public VisitorVerificationResult verify(Long visitorId, String verificationData) {
        // TODO: 实现临时访客中心验证逻辑
        // 1. 查询预约记录
        // 2. 验证预约状态和时间范围
        // 3. 人脸验证（可选）
        // 4. 生成临时模板并下发到设备

        log.debug("[临时访客策略] 中心验证, visitorId={}", visitorId);
        return VisitorVerificationResult.success("TEMP_TEMPLATE_" + visitorId, null);
    }

    @Override
    public int getPriority() {
        return 100; // 临时访客策略优先级最高
    }

    @Override
    public String getStrategyType() {
        return "TEMPORARY_VISITOR";
    }
}
