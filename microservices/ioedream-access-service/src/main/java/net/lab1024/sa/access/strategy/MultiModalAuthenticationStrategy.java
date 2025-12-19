package net.lab1024.sa.access.strategy;

import net.lab1024.sa.access.domain.dto.AccessVerificationRequest;
import net.lab1024.sa.access.domain.dto.VerificationResult;
import net.lab1024.sa.access.domain.enumeration.VerifyTypeEnum;

/**
 * 多模态认证策略接口
 * <p>
 * 严格遵循策略模式设计：
 * - 定义统一的认证接口
 * - 支持多种认证方式实现
 * - 可扩展新的认证方式
 * </p>
 * <p>
 * 实现类：
 * - FaceAuthenticationStrategy - 人脸认证策略
 * - FingerprintAuthenticationStrategy - 指纹认证策略
 * - PalmAuthenticationStrategy - 掌纹认证策略
 * - IrisAuthenticationStrategy - 虹膜认证策略
 * - VoiceAuthenticationStrategy - 声纹认证策略
 * - CardAuthenticationStrategy - IC卡认证策略
 * - QrCodeAuthenticationStrategy - 二维码认证策略
 * - PasswordAuthenticationStrategy - 密码认证策略
 * - NfcAuthenticationStrategy - NFC认证策略
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface MultiModalAuthenticationStrategy {

    /**
     * 执行认证
     * <p>
     * 根据不同的认证方式执行相应的认证逻辑
     * </p>
     *
     * @param request 验证请求
     * @return 认证结果
     */
    VerificationResult authenticate(AccessVerificationRequest request);

    /**
     * 是否支持该认证方式
     * <p>
     * 用于策略选择，判断当前策略是否支持指定的认证方式
     * </p>
     *
     * @param verifyType 认证方式代码（VerifyTypeEnum.code）
     * @return 是否支持
     */
    boolean supports(Integer verifyType);

    /**
     * 获取策略名称
     * <p>
     * 用于日志记录和调试
     * </p>
     *
     * @return 策略名称
     */
    String getStrategyName();

    /**
     * 获取认证方式枚举
     * <p>
     * 返回当前策略对应的认证方式枚举
     * </p>
     *
     * @return 认证方式枚举
     */
    VerifyTypeEnum getVerifyType();
}
