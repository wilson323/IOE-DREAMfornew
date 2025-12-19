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
 * ⚠️ 重要说明：多模态认证的作用
 * </p>
 * <p>
 * <strong>不是进行人员识别</strong>：
 * - 设备端已完成人员识别（人脸、指纹、卡片等）
 * - 设备端已识别出人员编号（pin字段）
 * - 软件端接收的是人员编号（pin），不是生物特征数据
 * </p>
 * <p>
 * <strong>核心职责是验证认证方式是否允许</strong>：
 * - 验证用户是否允许使用该认证方式（例如：某些区域只允许人脸，不允许密码）
 * - 验证区域配置中是否允许该认证方式
 * - 验证设备配置中是否支持该认证方式
 * - 记录认证方式（用于统计和审计）
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
     * 验证用户是否允许使用该认证方式
     * <p>
     * ⚠️ 注意：不是进行人员识别，而是验证用户是否允许使用该认证方式
     * </p>
     * <p>
     * 设备端已完成人员识别，并发送了人员编号（pin）
     * 软件端根据pin和verifyType验证：
     * - 用户权限配置中是否允许该认证方式
     * - 区域配置中是否允许该认证方式
     * - 设备配置中是否支持该认证方式
     * </p>
     *
     * @param request 验证请求（包含userId、verifyType等，设备端已识别出人员编号）
     * @return 认证方式验证结果
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
