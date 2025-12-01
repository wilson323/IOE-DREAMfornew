package net.lab1024.sa.admin.module.consume.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 限额配置实体类
 * 严格遵循repowiki规范：管理消费限额配置，支持多级限额和动态配置
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_consume_limit_config")
public class LimitConfigEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long configId;

    /**
     * 配置名称
     */
    private String configName;

    /**
     * 配置描述
     */
    private String configDesc;

    /**
     * 适用账户类型（STAFF/STUDENT/VISITOR/TEMP，ALL表示全部）
     */
    private String accountType;

    /**
     * 适用账户等级（普通/VIP/钻石，ALL表示全部）
     */
    private String accountLevel;

    /**
     * 单次消费限额
     */
    private BigDecimal singleLimit;

    /**
     * 日累计消费限额
     */
    private BigDecimal dailyLimit;

    /**
     * 周累计消费限额
     */
    private BigDecimal weeklyLimit;

    /**
     * 月累计消费限额
     */
    private BigDecimal monthlyLimit;

    /**
     * 季累计消费限额
     */
    private BigDecimal quarterlyLimit;

    /**
     * 年累计消费限额
     */
    private BigDecimal yearlyLimit;

    /**
     * 每日消费次数限制
     */
    private Integer dailyCountLimit;

    /**
     * 每月消费次数限制
     */
    private Integer monthlyCountLimit;

    /**
     * 是否启用（0：禁用，1：启用）
     */
    private Boolean enabled;

    /**
     * 优先级（数字越大优先级越高）
     */
    private Integer priority;

    /**
     * 生效时间
     */
    private LocalDateTime effectiveTime;

    /**
     * 失效时间
     */
    private LocalDateTime expireTime;

    /**
     * 特殊标识（用于特殊配置，如VIP配置等）
     */
    private String specialFlag;

    /**
     * 配置值（JSON格式，用于扩展配置）
     */
    private String configValue;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 备注
     */
    private String remark;

    /**
     * 检查配置是否在有效期内
     */
    public boolean isEffective() {
        LocalDateTime now = LocalDateTime.now();

        boolean afterEffectiveTime = effectiveTime == null || now.isAfter(effectiveTime);
        boolean beforeExpireTime = expireTime == null || now.isBefore(expireTime);

        return afterEffectiveTime && beforeExpireTime;
    }

    /**
     * 检查是否适用于指定账户类型
     */
    public boolean isApplicableToAccountType(String accountType) {
        return this.accountType == null || "ALL".equals(this.accountType) || this.accountType.equals(accountType);
    }

    /**
     * 检查是否适用于指定账户等级
     */
    public boolean isApplicableToAccountLevel(String accountLevel) {
        return this.accountLevel == null || "ALL".equals(this.accountLevel) || this.accountLevel.equals(accountLevel);
    }

    /**
     * 检查配置是否完全适用
     */
    public boolean isApplicable(String accountType, String accountLevel) {
        return enabled != null && enabled
                && isEffective()
                && isApplicableToAccountType(accountType)
                && isApplicableToAccountLevel(accountLevel);
    }

    /**
     * 获取指定类型的限额
     */
    public BigDecimal getLimitByType(String limitType) {
        switch (limitType.toUpperCase()) {
            case "SINGLE":
                return singleLimit;
            case "DAILY":
                return dailyLimit;
            case "WEEKLY":
                return weeklyLimit;
            case "MONTHLY":
                return monthlyLimit;
            case "QUARTERLY":
                return quarterlyLimit;
            case "YEARLY":
                return yearlyLimit;
            default:
                return null;
        }
    }
}