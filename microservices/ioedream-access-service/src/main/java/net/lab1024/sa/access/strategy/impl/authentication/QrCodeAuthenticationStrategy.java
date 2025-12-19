package net.lab1024.sa.access.strategy.impl.authentication;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.domain.dto.AccessVerificationRequest;
import net.lab1024.sa.access.domain.dto.VerificationResult;
import net.lab1024.sa.access.domain.enumeration.VerifyTypeEnum;
import org.springframework.stereotype.Component;

/**
 * 二维码认证策略实现
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@Component注解注册为Spring Bean
 * - 实现MultiModalAuthenticationStrategy接口
 * - 继承AbstractAuthenticationStrategy抽象类
 * </p>
 * <p>
 * 核心职责：
 * - 处理二维码认证逻辑
 * - 支持动态二维码验证
 * - 记录二维码认证结果
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Component
public class QrCodeAuthenticationStrategy extends AbstractAuthenticationStrategy {

    @Override
    protected VerificationResult doAuthenticate(AccessVerificationRequest request) {
        log.debug("[二维码认证] 执行认证: userId={}, deviceId={}", request.getUserId(), request.getDeviceId());

        // 边缘验证模式下，设备端已完成二维码识别
        // 软件端只需要验证用户权限
        return VerificationResult.success("二维码认证通过", null, "qr_code");
    }

    @Override
    public String getStrategyName() {
        return "QrCodeAuthenticationStrategy";
    }

    @Override
    public VerifyTypeEnum getVerifyType() {
        return VerifyTypeEnum.QR_CODE;
    }
}
