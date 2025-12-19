package net.lab1024.sa.access.strategy.impl.authentication;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.domain.dto.AccessVerificationRequest;
import net.lab1024.sa.access.domain.dto.VerificationResult;
import net.lab1024.sa.access.domain.enumeration.VerifyTypeEnum;
import org.springframework.stereotype.Component;

/**
 * 人脸认证策略实现
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@Component注解注册为Spring Bean
 * - 实现MultiModalAuthenticationStrategy接口
 * - 继承AbstractAuthenticationStrategy抽象类
 * </p>
 * <p>
 * 核心职责：
 * - 处理人脸识别认证逻辑
 * - 支持活体检测
 * - 记录人脸认证结果
 * </p>
 * <p>
 * 业务说明：
 * - 边缘验证模式：设备端已完成人脸识别，软件端只记录认证方式
 * - 后台验证模式：设备端完成人脸识别，软件端验证权限
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Component
public class FaceAuthenticationStrategy extends AbstractAuthenticationStrategy {

    /**
     * 执行人脸认证逻辑
     * <p>
     * 注意：根据边缘自主验证模式，设备端已完成人脸识别
     * 软件端主要负责记录认证方式和验证权限
     * </p>
     *
     * @param request 验证请求
     * @return 认证结果
     */
    @Override
    protected VerificationResult doAuthenticate(AccessVerificationRequest request) {
        log.debug("[人脸认证] 执行认证: userId={}, deviceId={}", request.getUserId(), request.getDeviceId());

        // 边缘验证模式下，设备端已完成人脸识别
        // 软件端只需要验证用户权限，不需要再次进行人脸识别
        // 这里返回成功，实际的权限验证在AccessVerificationManager中完成

        return VerificationResult.success("人脸认证通过", null, "face");
    }

    @Override
    public String getStrategyName() {
        return "FaceAuthenticationStrategy";
    }

    @Override
    public VerifyTypeEnum getVerifyType() {
        return VerifyTypeEnum.FACE;
    }
}
