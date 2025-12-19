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
 * ⚠️ 重要说明：不是进行人员识别
 * </p>
 * <p>
 * <strong>设备端已完成人员识别</strong>：
 * - 设备端通过人脸识别（1:N比对）识别出人员编号（pin）
 * - 设备端发送请求到软件端：pin=1001, verifytype=11
 * - 软件端接收的是人员编号（pin），不是人脸图像数据
 * </p>
 * <p>
 * <strong>核心职责是记录认证方式用于统计和审计</strong>：
 * - ✅ 记录认证方式（verifytype）用于统计和审计
 * - ✅ 提供认证方式枚举（VerifyTypeEnum）统一管理
 * - ✅ 转换认证方式（verifytype ↔ verifyMethod）用于数据存储和展示
 * - ❌ 不进行人员识别（设备端已完成）
 * - ❌ 不验证认证方式是否允许（设备端已完成，如果设备不支持该认证方式，设备端不会识别成功）
 * </p>
 * <p>
 * 两种验证模式的区别：
 * </p>
 * <ul>
 * <li><strong>边缘验证模式（Edge）</strong>：设备端已完成验证，软件端只记录认证方式</li>
 * <li><strong>后台验证模式（Backend）</strong>：设备端已识别人员和验证认证方式，软件端只记录认证方式</li>
 * </ul>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Component
public class FaceAuthenticationStrategy extends AbstractAuthenticationStrategy {

    /**
     * 记录认证方式（用于统计和审计）
     * <p>
     * ⚠️ 注意：不是进行人员识别，也不是验证认证方式是否允许
     * </p>
     * <p>
     * 设备端已完成：
     * - 人员识别（1:N比对，识别出pin）
     * - 认证方式验证（如果设备不支持，不会识别成功）
     * </p>
     * <p>
     * 软件端只记录认证方式（verifytype）用于统计和审计
     * 后续可以扩展：统计各认证方式的使用次数、提供认证方式使用报表
     * </p>
     *
     * @param request 验证请求（包含userId、verifyType等，设备端已识别出人员编号）
     * @return 记录结果（始终成功，因为只是记录）
     */
    @Override
    protected VerificationResult doAuthenticate(AccessVerificationRequest request) {
        log.debug("[人脸认证] 记录认证方式: userId={}, deviceId={}, areaId={}, verifyType={}",
                request.getUserId(), request.getDeviceId(), request.getAreaId(), request.getVerifyType());

        // ⚠️ 注意：设备端已完成人员识别和认证方式验证
        // - 设备端通过1:N比对识别出人员编号（pin）
        // - 设备端已验证认证方式是否支持（如果设备不支持，不会识别成功）
        // - 软件端只记录认证方式（verifytype）用于统计和审计

        // 记录认证方式（用于统计和审计）
        // TODO: 后续扩展：统计各认证方式的使用次数
        // TODO: 后续扩展：提供认证方式使用报表

        return VerificationResult.success("人脸认证方式记录成功", null, "face");
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
