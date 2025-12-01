/*
 * 密码安全策略实体类
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-19
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.consume.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.lab1024.sa.base.common.entity.BaseEntity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 密码安全策略实体
 * 用于管理支付密码的安全策略配置
 *
 * @author SmartAdmin Team
 * @date 2025/01/19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("t_password_security_policy")
public class PasswordSecurityPolicyEntity extends BaseEntity {

    /**
     * 策略ID
     */
    @TableId
    private Long policyId;

    /**
     * 策略名称
     */
    @NotBlank(message = "策略名称不能为空")
    @Size(max = 100, message = "策略名称长度不能超过100个字符")
    private String policyName;

    /**
     * 策略编码
     */
    @NotBlank(message = "策略编码不能为空")
    @Size(max = 50, message = "策略编码长度不能超过50个字符")
    private String policyCode;

    /**
     * 最小密码长度
     */
    @NotNull(message = "最小密码长度不能为空")
    private Integer minLength;

    /**
     * 最大密码长度
     */
    @NotNull(message = "最大密码长度不能为空")
    private Integer maxLength;

    /**
     * 必须包含数字(0-否,1-是)
     */
    private Integer requireNumbers;

    /**
     * 必须包含字母(0-否,1-是)
     */
    private Integer requireLetters;

    /**
     * 必须包含大写字母(0-否,1-是)
     */
    private Integer requireUppercase;

    /**
     * 必须包含小写字母(0-否,1-是)
     */
    private Integer requireLowercase;

    /**
     * 必须包含特殊字符(0-否,1-是)
     */
    private Integer requireSpecialChars;

    /**
     * 禁止使用常见密码(0-否,1-是)
     */
    private Integer preventCommonPasswords;

    /**
     * 密码历史检查次数(禁止使用最近N次密码)
     */
    private Integer passwordHistoryCount;

    /**
     * 密码有效期(天)
     */
    private Integer passwordValidityDays;

    /**
     * 连续错误锁定次数
     */
    private Integer maxConsecutiveErrors;

    /**
     * 锁定时间(分钟)
     */
    private Integer lockoutDurationMinutes;

    /**
     * 策略描述
     */
    @Size(max = 500, message = "策略描述长度不能超过500个字符")
    private String description;

    /**
     * 状态 (0-禁用, 1-启用)
     */
    private Integer status;

    /**
     * 是否默认策略(0-否, 1-是)
     */
    private Integer isDefault;
}
