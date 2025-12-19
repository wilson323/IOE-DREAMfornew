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
 * <strong>核心职责是验证认证方式是否允许</strong>：
 * - 验证用户是否允许使用人脸认证方式
 * - 验证区域配置中是否允许人脸认证
 * - 验证设备配置中是否支持人脸认证
 * - 记录人脸认证方式（用于统计和审计）
 * </p>
 * <p>
 * 两种验证模式的区别：
 * </p>
 * <ul>
 * <li><strong>边缘验证模式（Edge）</strong>：设备端已完成验证，软件端只记录认证方式</li>
 * <li><strong>后台验证模式（Backend）</strong>：设备端已识别人员，软件端验证认证方式是否允许</li>
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
     * 验证用户是否允许使用人脸认证方式
     * <p>
     * ⚠️ 注意：不是进行人员识别，设备端已完成人员识别并发送了人员编号（pin）
     * </p>
     * <p>
     * 验证内容：
     * - 用户权限配置中是否允许人脸认证
     * - 区域配置中是否允许人脸认证
     * - 设备配置中是否支持人脸认证
     * </p>
     * <p>
     * 默认实现：允许使用人脸认证方式
     * 后续可以扩展：检查用户权限、区域配置、设备配置
     * </p>
     *
     * @param request 验证请求（包含userId、verifyType等，设备端已识别出人员编号）
     * @return 认证方式验证结果
     */
    @Override
    protected VerificationResult doAuthenticate(AccessVerificationRequest request) {
        log.debug("[人脸认证] 验证认证方式是否允许: userId={}, deviceId={}, areaId={}",
                request.getUserId(), request.getDeviceId(), request.getAreaId());

        // TODO: 后续扩展：检查用户权限配置中是否允许人脸认证
        // TODO: 后续扩展：检查区域配置中是否允许人脸认证
        // TODO: 后续扩展：检查设备配置中是否支持人脸认证

        // 当前实现：默认允许使用人脸认证方式
        // 实际的权限验证在AccessVerificationManager中完成（反潜、互锁、时间段等）

        return VerificationResult.success("人脸认证方式验证通过", null, "face");
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
