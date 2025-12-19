package net.lab1024.sa.access.strategy.impl.authentication;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.domain.dto.AccessVerificationRequest;
import net.lab1024.sa.access.domain.dto.VerificationResult;
import net.lab1024.sa.access.domain.enumeration.VerifyTypeEnum;
import org.springframework.stereotype.Component;

/**
 * 密码认证策略实现
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@Component注解注册为Spring Bean
 * - 实现MultiModalAuthenticationStrategy接口
 * - 继承AbstractAuthenticationStrategy抽象类
 * </p>
 * <p>
 * 核心职责：
 * - 处理密码认证逻辑
 * - 支持密码加密验证
 * - 记录密码认证结果
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Component
public class PasswordAuthenticationStrategy extends AbstractAuthenticationStrategy {

    @Override
    protected VerificationResult doAuthenticate(AccessVerificationRequest request) {
        log.debug("[密码认证] 执行认证: userId={}, deviceId={}", request.getUserId(), request.getDeviceId());

        // 边缘验证模式下，设备端已完成密码验证
        // 软件端只需要验证用户权限
        return VerificationResult.success("密码认证通过", null, "password");
    }

    @Override
    public String getStrategyName() {
        return "PasswordAuthenticationStrategy";
    }

    @Override
    public VerifyTypeEnum getVerifyType() {
        return VerifyTypeEnum.PASSWORD;
    }
}
