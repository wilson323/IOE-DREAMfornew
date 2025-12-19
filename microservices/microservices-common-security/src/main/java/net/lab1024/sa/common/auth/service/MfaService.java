package net.lab1024.sa.common.auth.service;

import net.lab1024.sa.common.auth.domain.dto.MfaSetupDTO;
import net.lab1024.sa.common.auth.domain.dto.MfaVerifyDTO;
import net.lab1024.sa.common.auth.domain.dto.SmsCodeRequestDTO;
import net.lab1024.sa.common.auth.domain.dto.EmailCodeRequestDTO;

/**
 * 多因子认证服务接口
 *
 * 功能职责：
 * - 生成和验证TOTP验证码
 * - 发送短信验证码
 * - 发送邮箱验证码
 * - 管理MFA设置和状态
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
public interface MfaService {

    /**
     * 生成MFA设置信息
     *
     * @param userId 用户ID
     * @return MFA设置信息（包含密钥、二维码等）
     */
    MfaSetupDTO generateMfaSetup(Long userId);

    /**
     * 启用MFA验证
     *
     * @param userId 用户ID
     * @param verifyCode 验证码
     * @return 是否启用成功
     */
    boolean enableMfa(Long userId, String verifyCode);

    /**
     * 禁用MFA验证
     *
     * @param userId 用户ID
     * @param password 用户密码（用于验证身份）
     * @return 是否禁用成功
     */
    boolean disableMfa(Long userId, String password);

    /**
     * 验证MFA代码
     *
     * @param userId 用户ID
     * @param mfaVerify MFA验证请求
     * @return 验证结果
     */
    boolean verifyMfaCode(Long userId, MfaVerifyDTO mfaVerify);

    /**
     * 发送短信验证码
     *
     * @param request 短信验证码请求
     */
    void sendSmsCode(SmsCodeRequestDTO request);

    /**
     * 发送邮箱验证码
     *
     * @param request 邮箱验证码请求
     */
    void sendEmailCode(EmailCodeRequestDTO request);

    /**
     * 验证短信验证码
     *
     * @param phone 手机号
     * @param code 验证码
     * @return 验证结果
     */
    boolean verifySmsCode(String phone, String code);

    /**
     * 验证邮箱验证码
     *
     * @param email 邮箱地址
     * @param code 验证码
     * @return 验证结果
     */
    boolean verifyEmailCode(String email, String code);

    /**
     * 检查用户是否启用了MFA
     *
     * @param userId 用户ID
     * @return 是否启用MFA
     */
    boolean isMfaEnabled(Long userId);

    /**
     * 获取用户的MFA设置状态
     *
     * @param userId 用户ID
     * @return MFA设置状态信息
     */
    MfaSetupDTO getMfaStatus(Long userId);
}