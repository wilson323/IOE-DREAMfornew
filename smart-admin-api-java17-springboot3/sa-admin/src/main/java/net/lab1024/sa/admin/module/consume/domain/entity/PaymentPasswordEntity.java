/*
 * 支付密码实体
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-17
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.consume.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 支付密码实体
 * 存储用户的支付密码信息和安全配置
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_payment_password")
public class PaymentPasswordEntity extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long passwordId;

    /**
     * 人员ID
     */
    private Long personId;

    /**
     * 密码哈希值
     */
    private String passwordHash;

    /**
     * 密码盐值
     */
    private String salt;

    /**
     * 密码状态（ACTIVE-正常，LOCKED-锁定，DISABLED-禁用）
     */
    private String status;

    /**
     * 密码过期时间
     */
    private LocalDateTime expiredTime;

    /**
     * 锁定时间
     */
    private LocalDateTime lockTime;

    /**
     * 锁定截止时间
     */
    private LocalDateTime lockUntil;

    /**
     * 锁定原因
     */
    private String lockReason;

    /**
     * 最后修改时间
     */
    private LocalDateTime lastModifyTime;

    /**
     * 最后验证时间
     */
    private LocalDateTime lastVerifyTime;

    /**
     * 验证失败次数
     */
    private Integer failureCount;

    /**
     * 累计验证次数
     */
    private Integer totalVerifyCount;

    /**
     * 最后验证IP
     */
    private String lastVerifyIp;

    /**
     * 最后验证设备
     */
    private String lastVerifyDevice;

    /**
     * 是否启用生物特征验证
     */
    private Boolean enableBiometric;

    /**
     * 生物特征类型（FINGERPRINT-指纹，FACE-人脸，IRIS-虹膜）
     */
    private String biometricTypes;

    /**
     * 生物特征数据模板
     */
    private String biometricTemplate;

    /**
     * 密码强度等级（WEAK/MEDIUM/STRONG/VERY_STRONG）
     */
    private String passwordStrength;

    /**
     * 密码分数（0-100）
     */
    private Integer passwordScore;

    /**
     * 是否需要强制更新密码
     */
    private Boolean forcePasswordChange;

    /**
     * 安全问题
     */
    private String securityQuestion;

    /**
     * 安全问题答案（哈希存储）
     */
    private String securityAnswerHash;

    /**
     * 安全问题盐值
     */
    private String securityAnswerSalt;

    /**
     * 扩展数据（JSON格式）
     */
    private String extendData;

    /**
     * 备注
     */
    private String remark;

    /**
     * 检查密码是否已过期
     */
    public boolean isExpired() {
        return expiredTime != null && expiredTime.isBefore(LocalDateTime.now());
    }

    /**
     * 检查密码是否被锁定
     */
    public boolean isLocked() {
        return "LOCKED".equals(status) || (lockUntil != null && lockUntil.isAfter(LocalDateTime.now()));
    }

    /**
     * 检查密码是否处于活动状态
     */
    public boolean isActive() {
        return "ACTIVE".equals(status) && !isExpired() && !isLocked();
    }

    /**
     * 检查密码是否被禁用
     */
    public boolean isDisabled() {
        return "DISABLED".equals(status);
    }

    /**
     * 检查是否启用了生物特征验证
     */
    public boolean isBiometricEnabled() {
        return Boolean.TRUE.equals(enableBiometric) && biometricTypes != null && !biometricTypes.isEmpty();
    }

    /**
     * 获取密码强度描述
     */
    public String getStrengthDescription() {
        if (passwordStrength == null) {
            return "未知";
        }
        switch (passwordStrength) {
            case "VERY_STRONG":
                return "非常强";
            case "STRONG":
                return "强";
            case "MEDIUM":
                return "中等";
            case "WEAK":
                return "弱";
            default:
                return passwordStrength;
        }
    }

    /**
     * 增加验证失败次数
     */
    public void incrementFailureCount() {
        this.failureCount = (this.failureCount != null ? this.failureCount : 0) + 1;
        this.totalVerifyCount = (this.totalVerifyCount != null ? this.totalVerifyCount : 0) + 1;
    }

    /**
     * 增加验证成功次数
     */
    public void incrementSuccessCount() {
        this.totalVerifyCount = (this.totalVerifyCount != null ? this.totalVerifyCount : 0) + 1;
        this.failureCount = 0; // 重置失败次数
    }

    /**
     * 更新最后验证信息
     */
    public void updateLastVerifyInfo(String clientIp, String deviceInfo) {
        this.lastVerifyTime = LocalDateTime.now();
        this.lastVerifyIp = clientIp;
        this.lastVerifyDevice = deviceInfo;
    }

    /**
     * 锁定密码
     */
    public void lock(String reason, Integer lockMinutes) {
        this.status = "LOCKED";
        this.lockReason = reason;
        this.lockTime = LocalDateTime.now();
        this.lockUntil = lockMinutes != null ? LocalDateTime.now().plusMinutes(lockMinutes) : null;
    }

    /**
     * 解锁密码
     */
    public void unlock() {
        this.status = "ACTIVE";
        this.lockReason = null;
        this.lockTime = null;
        this.lockUntil = null;
    }

    /**
     * 禁用密码
     */
    public void disable() {
        this.status = "DISABLED";
    }

    /**
     * 启用密码
     */
    public void enable() {
        this.status = "ACTIVE";
    }
}