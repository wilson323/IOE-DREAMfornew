package net.lab1024.sa.admin.module.consume.domain.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 消费账户扩展实体类
 * <p>
 * 基于扩展表架构设计，存储账户扩展信息
 * 与AccountBaseEntity配合使用，提供完整的账户功能
 *
 * 扩展表架构：t_account_base (基础表) + t_account_extension (扩展表)
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_account_extension")
public class AccountExtensionEntity extends BaseEntity {

    /**
     * 账户ID（关联AccountBaseEntity.accountId）
     */
    @NotNull(message = "账户ID不能为空")
    @TableField("account_id")
    private Long accountId;

    // ==================== 消费限额配置 ====================

    /**
     * 月度消费限额
     */
    @TableField("monthly_limit")
    private BigDecimal monthlyLimit;

    /**
     * 日度消费限额
     */
    @TableField("daily_limit")
    private BigDecimal dailyLimit;

    /**
     * 单次消费限额
     */
    @TableField("single_limit")
    private BigDecimal singleLimit;

    /**
     * 当前月度已消费金额
     */
    @TableField("current_monthly_amount")
    private BigDecimal currentMonthlyAmount;

    /**
     * 当前日度已消费金额
     */
    @TableField("current_daily_amount")
    private BigDecimal currentDailyAmount;

    // ==================== 安全认证配置 ====================

    /**
     * 密码（用于消费验证）
     */
    @TableField("password")
    private String password;

    /**
     * 支付密码
     */
    @TableField("pay_password")
    private String payPassword;

    /**
     * 卡片ID（物理卡）
     */
    @TableField("card_id")
    private String cardId;

    /**
     * 卡片状态
     * NORMAL-正常
     * LOST-丢失
     * DAMAGE-损坏
     * EXPIRED-过期
     */
    @TableField("card_status")
    private String cardStatus;

    /**
     * 人脸特征标识
     */
    @TableField("face_feature_id")
    private String faceFeatureId;

    /**
     * 指纹特征标识
     */
    @TableField("fingerprint_id")
    private String fingerprintId;

    // ==================== 通知配置 ====================

    /**
     * 手机号码（用于通知和验证）
     */
    @TableField("phone_number")
    private String phoneNumber;

    /**
     * 邮箱地址（用于通知）
     */
    @TableField("email")
    private String email;

    /**
     * 是否开启短信通知
     */
    @TableField("sms_notification")
    private Boolean smsNotification;

    /**
     * 是否开启邮件通知
     */
    @TableField("email_notification")
    private Boolean emailNotification;

    /**
     * 是否开启消费提醒
     */
    @TableField("consume_reminder")
    private Boolean consumeReminder;

    /**
     * 消费提醒阈值（低于此金额时提醒）
     */
    @TableField("reminder_threshold")
    private BigDecimal reminderThreshold;

    // ==================== 自动充值配置 ====================

    /**
     * 自动充值开关
     */
    @TableField("auto_recharge")
    private Boolean autoRecharge;

    /**
     * 自动充值阈值
     */
    @TableField("auto_recharge_threshold")
    private BigDecimal autoRechargeThreshold;

    /**
     * 自动充值金额
     */
    @TableField("auto_recharge_amount")
    private BigDecimal autoRechargeAmount;

    /**
     * 绑定支付方式（JSON格式：微信、支付宝、银行卡等）
     */
    @TableField("payment_methods")
    private String paymentMethods;

    // ==================== 扩展配置 ====================

    /**
     * 账户配置JSON（存储个性化配置）
     */
    @TableField("account_config")
    private String accountConfig;

    /**
     * 扩展数据JSON（存储扩展字段）
     */
    @TableField("extend_data")
    private String extendData;

    /**
     * 密码重置时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("password_reset_time")
    private LocalDateTime passwordResetTime;

    /**
     * 支付密码重置时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("pay_password_reset_time")
    private LocalDateTime payPasswordResetTime;

    /**
     * 最后登录IP
     */
    @TableField("last_login_ip")
    private String lastLoginIp;

    /**
     * 最后登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 登录失败次数
     */
    @TableField("login_fail_count")
    private Integer loginFailCount;

    /**
     * 账户锁定时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("lock_time")
    private LocalDateTime lockTime;

    /**
     * 备注信息
     */
    @TableField("remark")
    private String remark;

    // ==================== 业务方法 ====================

    /**
     * 检查卡片是否正常
     *
     * @return 卡片是否正常
     */
    public boolean isCardNormal() {
        return "NORMAL".equals(cardStatus);
    }

    /**
     * 检查是否设置了支付密码
     *
     * @return 是否设置了支付密码
     */
    public boolean hasPayPassword() {
        return payPassword != null && !payPassword.trim().isEmpty();
    }

    /**
     * 检查是否开启了自动充值
     *
     * @return 是否开启自动充值
     */
    public boolean isAutoRechargeEnabled() {
        return Boolean.TRUE.equals(autoRecharge);
    }

    /**
     * 检查是否需要自动充值
     *
     * @param currentBalance 当前余额
     * @return 是否需要自动充值
     */
    public boolean needAutoRecharge(BigDecimal currentBalance) {
        if (!isAutoRechargeEnabled()) {
            return false;
        }
        if (autoRechargeThreshold == null || currentBalance == null) {
            return false;
        }
        return currentBalance.compareTo(autoRechargeThreshold) <= 0;
    }

    /**
     * 检查单次消费是否超限
     *
     * @param amount 消费金额
     * @return 是否超限
     */
    public boolean isSingleLimitExceeded(BigDecimal amount) {
        if (singleLimit == null || amount == null) {
            return false;
        }
        return amount.compareTo(singleLimit) > 0;
    }

    /**
     * 检查日度消费是否超限
     *
     * @param amount 新增消费金额
     * @return 是否超限
     */
    public boolean isDailyLimitExceeded(BigDecimal amount) {
        if (dailyLimit == null) {
            return false;
        }
        BigDecimal current = currentDailyAmount != null ? currentDailyAmount : BigDecimal.ZERO;
        BigDecimal newTotal = current.add(amount != null ? amount : BigDecimal.ZERO);
        return newTotal.compareTo(dailyLimit) > 0;
    }

    /**
     * 检查月度消费是否超限
     *
     * @param amount 新增消费金额
     * @return 是否超限
     */
    public boolean isMonthlyLimitExceeded(BigDecimal amount) {
        if (monthlyLimit == null) {
            return false;
        }
        BigDecimal current = currentMonthlyAmount != null ? currentMonthlyAmount : BigDecimal.ZERO;
        BigDecimal newTotal = current.add(amount != null ? amount : BigDecimal.ZERO);
        return newTotal.compareTo(monthlyLimit) > 0;
    }

    /**
     * 检查是否开启了消费提醒
     *
     * @return 是否开启消费提醒
     */
    public boolean isConsumeReminderEnabled() {
        return Boolean.TRUE.equals(consumeReminder);
    }

    /**
     * 检查是否需要发送余额提醒
     *
     * @param currentBalance 当前余额
     * @return 是否需要发送提醒
     */
    public boolean needBalanceReminder(BigDecimal currentBalance) {
        if (!isConsumeReminderEnabled()) {
            return false;
        }
        if (reminderThreshold == null || currentBalance == null) {
            return false;
        }
        return currentBalance.compareTo(reminderThreshold) <= 0;
    }

    /**
     * 更新日度消费金额
     *
     * @param consumeAmount 消费金额
     */
    public void updateDailyConsumedAmount(BigDecimal consumeAmount) {
        if (consumeAmount != null) {
            if (currentDailyAmount == null) {
                currentDailyAmount = BigDecimal.ZERO;
            }
            this.currentDailyAmount = currentDailyAmount.add(consumeAmount);
        }
    }

    /**
     * 更新月度消费金额
     *
     * @param consumeAmount 消费金额
     */
    public void updateMonthlyConsumedAmount(BigDecimal consumeAmount) {
        if (consumeAmount != null) {
            if (currentMonthlyAmount == null) {
                currentMonthlyAmount = BigDecimal.ZERO;
            }
            this.currentMonthlyAmount = currentMonthlyAmount.add(consumeAmount);
        }
    }

    /**
     * 重置日度消费金额（新的一天开始时调用）
     */
    public void resetDailyConsumedAmount() {
        this.currentDailyAmount = BigDecimal.ZERO;
    }

    /**
     * 重置月度消费金额（新的一月开始时调用）
     */
    public void resetMonthlyConsumedAmount() {
        this.currentMonthlyAmount = BigDecimal.ZERO;
    }

    /**
     * 增加登录失败次数
     */
    public void increaseLoginFailCount() {
        if (loginFailCount == null) {
            loginFailCount = 0;
        }
        this.loginFailCount = loginFailCount + 1;
    }

    /**
     * 重置登录失败次数
     */
    public void resetLoginFailCount() {
        this.loginFailCount = 0;
        this.lockTime = null;
    }

    /**
     * 检查账户是否被锁定
     *
     * @return 是否被锁定
     */
    public boolean isLocked() {
        return lockTime != null && lockTime.isAfter(LocalDateTime.now());
    }

    /**
     * 获取卡片状态描述
     *
     * @return 状态描述
     */
    public String getCardStatusDescription() {
        if (cardStatus == null) {
            return "未知";
        }
        switch (cardStatus) {
            case "NORMAL":
                return "正常";
            case "LOST":
                return "丢失";
            case "DAMAGE":
                return "损坏";
            case "EXPIRED":
                return "过期";
            default:
                return cardStatus;
        }
    }
}