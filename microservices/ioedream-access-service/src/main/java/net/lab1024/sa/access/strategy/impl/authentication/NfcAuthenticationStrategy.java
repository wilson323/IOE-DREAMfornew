package net.lab1024.sa.access.strategy.impl.authentication;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.domain.dto.AccessVerificationRequest;
import net.lab1024.sa.access.domain.dto.VerificationResult;
import net.lab1024.sa.access.domain.enumeration.VerifyTypeEnum;
import org.springframework.stereotype.Component;

/**
 * NFC认证策略实现
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
 * - 设备端通过NFC识别出人员编号（pin）
 * - 设备端发送请求到软件端：pin=1001, verifytype=21
 * - 软件端接收的是人员编号（pin），不是NFC数据
 * </p>
 * <p>
 * <strong>核心职责是验证认证方式是否允许</strong>：
 * - 验证用户是否允许使用NFC认证方式
 * - 验证区域配置中是否允许NFC认证
 * - 验证设备配置中是否支持NFC认证
 * - 记录NFC认证方式（用于统计和审计）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Component
public class NfcAuthenticationStrategy extends AbstractAuthenticationStrategy {

    @Override
    protected VerificationResult doAuthenticate(AccessVerificationRequest request) {
        log.debug("[NFC认证] 验证认证方式是否允许: userId={}, deviceId={}, areaId={}",
                request.getUserId(), request.getDeviceId(), request.getAreaId());

        // TODO: 后续扩展：检查用户权限配置中是否允许NFC认证
        // TODO: 后续扩展：检查区域配置中是否允许NFC认证
        // TODO: 后续扩展：检查设备配置中是否支持NFC认证

        // 当前实现：默认允许使用NFC认证方式
        // 实际的权限验证在AccessVerificationManager中完成（反潜、互锁、时间段等）

        return VerificationResult.success("NFC认证方式验证通过", null, "nfc");
    }

    @Override
    public String getStrategyName() {
        return "NfcAuthenticationStrategy";
    }

    @Override
    public VerifyTypeEnum getVerifyType() {
        return VerifyTypeEnum.NFC;
    }
}
