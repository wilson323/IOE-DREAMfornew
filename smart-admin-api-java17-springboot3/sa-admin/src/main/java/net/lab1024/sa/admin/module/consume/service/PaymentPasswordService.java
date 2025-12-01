/*
 * 支付密码验证服务接口
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-17
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.consume.service;

import jakarta.validation.constraints.NotNull;
import net.lab1024.sa.admin.module.consume.domain.result.PaymentPasswordResult;

import java.util.List;

/**
 * 支付密码验证服务接口
 * 负责消费支付密码的验证和管理
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
public interface PaymentPasswordService {

    /**
     * 验证支付密码
     *
     * @param personId 人员ID
     * @param password 支付密码
     * @param deviceId 设备ID（用于记录验证行为）
     * @return 验证结果
     */
    PaymentPasswordResult verifyPassword(@NotNull Long personId, @NotNull String password, Long deviceId);

    /**
     * 设置支付密码
     *
     * @param personId 人员ID
     * @param oldPassword 旧密码（首次设置时可为null）
     * @param newPassword 新密码
     * @param confirmPassword 确认密码
     * @param deviceId 设备ID
     * @return 设置结果
     */
    PaymentPasswordResult setPassword(@NotNull Long personId, String oldPassword, @NotNull String newPassword, @NotNull String confirmPassword, Long deviceId);

    /**
     * 修改支付密码
     *
     * @param personId 人员ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @param confirmPassword 确认密码
     * @param deviceId 设备ID
     * @return 修改结果
     */
    PaymentPasswordResult changePassword(@NotNull Long personId, @NotNull String oldPassword, @NotNull String newPassword, @NotNull String confirmPassword, Long deviceId);

    /**
     * 重置支付密码
     *
     * @param personId 人员ID
     * @param resetType 重置类型（FORGOT/MANUAL）
     * @param verifyCode 验证码
     * @param newPassword 新密码
     * @param confirmPassword 确认密码
     * @param deviceId 设备ID
     * @return 重置结果
     */
    PaymentPasswordResult resetPassword(@NotNull Long personId, @NotNull String resetType, String verifyCode, @NotNull String newPassword, @NotNull String confirmPassword, Long deviceId);

    /**
     * 检查是否已设置支付密码
     *
     * @param personId 人员ID
     * @return 是否已设置
     */
    boolean hasPaymentPassword(@NotNull Long personId);

    /**
     * 检查支付密码是否过期
     *
     * @param personId 人员ID
     * @return 是否过期
     */
    boolean isPasswordExpired(@NotNull Long personId);

    /**
     * 更新密码过期时间
     *
     * @param personId 人员ID
     * @param expiredDays 过期天数
     * @return 更新结果
     */
    boolean updatePasswordExpiry(@NotNull Long personId, Integer expiredDays);

    /**
     * 获取密码安全策略
     *
     * @return 密码安全策略
     */
    PasswordSecurityPolicy getPasswordSecurityPolicy();

    /**
     * 验证密码强度
     *
     * @param password 密码
     * @return 密码强度结果
     */
    PasswordStrengthResult checkPasswordStrength(@NotNull String password);

    /**
     * 记录密码验证行为
     *
     * @param personId 人员ID
     * @param deviceId 设备ID
     * @param verifyResult 验证结果
     * @param clientIp 客户端IP
     */
    void recordPasswordVerification(@NotNull Long personId, Long deviceId, PaymentPasswordResult verifyResult, String clientIp);

    /**
     * 检测异常密码尝试行为
     *
     * @param personId 人员ID
     * @param deviceId 设备ID
     * @param timeWindowMinutes 时间窗口（分钟）
     * @return 是否存在异常行为
     */
    boolean detectAbnormalAttempts(@NotNull Long personId, Long deviceId, Integer timeWindowMinutes);

    /**
     * 获取密码验证历史
     *
     * @param personId 人员ID
     * @param limit 记录数量限制
     * @return 验证历史列表
     */
    List<PasswordVerificationHistory> getVerificationHistory(@NotNull Long personId, Integer limit);

    /**
     * 锁定账户密码功能
     *
     * @param personId 人员ID
     * @param lockReason 锁定原因
     * @param lockMinutes 锁定时间（分钟）
     * @return 锁定结果
     */
    PaymentPasswordResult lockPassword(@NotNull Long personId, @NotNull String lockReason, Integer lockMinutes);

    /**
     * 解锁账户密码功能
     *
     * @param personId 人员ID
     * @param unlockReason 解锁原因
     * @return 解锁结果
     */
    PaymentPasswordResult unlockPassword(@NotNull Long personId, String unlockReason);

    /**
     * 检查密码是否被锁定
     *
     * @param personId 人员ID
     * @return 是否被锁定
     */
    boolean isPasswordLocked(@NotNull Long personId);

    /**
     * 启用/禁用支付密码功能
     *
     * @param personId 人员ID
     * @param enabled 是否启用
     * @param reason 原因
     * @return 操作结果
     */
    boolean setPaymentPasswordEnabled(@NotNull Long personId, boolean enabled, String reason);

    /**
     * 验证生物特征作为支付验证
     *
     * @param personId 人员ID
     * @param biometricData 生物特征数据
     * @param biometricType 生物特征类型
     * @param deviceId 设备ID
     * @return 验证结果
     */
    PaymentPasswordResult verifyBiometric(@NotNull Long personId, @NotNull String biometricData, @NotNull String biometricType, Long deviceId);
}