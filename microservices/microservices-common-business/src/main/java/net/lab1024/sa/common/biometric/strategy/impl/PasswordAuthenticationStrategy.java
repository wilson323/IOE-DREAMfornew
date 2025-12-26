package net.lab1024.sa.common.biometric.strategy.impl;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import net.lab1024.sa.common.biometric.domain.dto.VerificationRequest;
import net.lab1024.sa.common.biometric.domain.dto.VerificationResult;
import net.lab1024.sa.common.biometric.domain.enumeration.VerifyTypeEnum;

/**
 * 密码认证策略实现
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@Component注解注册为Spring Bean
 * - 继承AbstractAuthenticationStrategy抽象类
 * </p>
 * <p>
 * ⚠️ 重要说明：不是进行人员识别
 * </p>
 * <p>
 * <strong>设备端已完成人员识别</strong>：
 * - 设备端通过密码识别出人员编号（pin）
 * - 设备端发送请求到软件端：pin=1001, verifytype=0
 * - 软件端接收的是人员编号（pin），不是密码数据
 * </p>
 * <p>
 * <strong>核心职责是记录认证方式用于统计和审计</strong>：
 * - ✅ 记录认证方式（verifytype）用于统计和审计
 * - ✅ 提供认证方式枚举（VerifyTypeEnum）统一管理
 * - ✅ 转换认证方式（verifytype ↔ verifyMethod）用于数据存储和展示
 * - ❌ 不进行人员识别（设备端已完成）
 * - ❌ 不验证认证方式是否允许（设备端已完成，如果设备不支持该认证方式，设备端不会识别成功）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Component
@Slf4j
public class PasswordAuthenticationStrategy extends AbstractAuthenticationStrategy {

    @Override
    protected VerificationResult doAuthenticate(VerificationRequest request) {
        log.debug("[密码认证] 记录认证方式: userId={}, deviceId={}, areaId={}, verifyType={}",
                request.getUserId(), request.getDeviceId(), request.getAreaId(), request.getVerifyType());

        // ⚠️ 注意：设备端已完成人员识别和认证方式验证
        // - 设备端通过1:N比对识别出人员编号（pin）
        // - 设备端已验证认证方式是否支持（如果设备不支持，不会识别成功）
        // - 软件端只记录认证方式（verifytype）用于统计和审计

        return VerificationResult.success("密码认证方式记录成功", null, "password");
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
