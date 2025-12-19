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
 * <strong>核心职责是记录认证方式用于统计和审计</strong>：
 * - ✅ 记录认证方式（verifytype）用于统计和审计
 * - ✅ 提供认证方式枚举（VerifyTypeEnum）统一管理
 * - ✅ 转换认证方式（verifytype ↔ verifyMethod）用于数据存储和展示
 * - ❌ 不进行人员识别（设备端已完成）
 * - ❌ 不验证认证方式是否允许（设备端已完成，如果设备不支持该认证方式，设备端不会识别成功）
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
     * 记录认证方式（用于统计和审计）
     * <p>
     * ⚠️ 注意：不是进行人员识别，也不是验证认证方式是否允许
     * </p>
     * <p>
     * 设备端已完成：
     * - 人员识别（1:N比对，识别出pin）
     * - 认证方式验证（如果设备不支持该认证方式，设备端不会识别成功）
     * </p>
     * <p>
     * 软件端只记录认证方式（verifytype）用于统计和审计
     * </p>
     *
     * @param request 验证请求（包含userId、verifyType等，设备端已识别出人员编号）
     * @return 记录结果（始终成功，因为只是记录）
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
