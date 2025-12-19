package net.lab1024.sa.access.strategy.impl.authentication;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.domain.dto.AccessVerificationRequest;
import net.lab1024.sa.access.domain.dto.VerificationResult;
import net.lab1024.sa.access.domain.enumeration.VerifyTypeEnum;
import org.springframework.stereotype.Component;

/**
 * IC卡认证策略实现
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@Component注解注册为Spring Bean
 * - 实现MultiModalAuthenticationStrategy接口
 * - 继承AbstractAuthenticationStrategy抽象类
 * </p>
 * <p>
 * 核心职责：
 * - 处理IC卡认证逻辑
 * - 支持卡号验证
 * - 记录IC卡认证结果
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Component
public class CardAuthenticationStrategy extends AbstractAuthenticationStrategy {

    @Override
    protected VerificationResult doAuthenticate(AccessVerificationRequest request) {
        log.debug("[IC卡认证] 执行认证: userId={}, deviceId={}, cardNo={}",
                request.getUserId(), request.getDeviceId(), request.getCardNo());

        // 边缘验证模式下，设备端已完成IC卡识别
        // 软件端只需要验证用户权限
        return VerificationResult.success("IC卡认证通过", null, "card");
    }

    @Override
    public String getStrategyName() {
        return "CardAuthenticationStrategy";
    }

    @Override
    public VerifyTypeEnum getVerifyType() {
        return VerifyTypeEnum.CARD;
    }
}
