package net.lab1024.sa.admin.module.consume.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.entity.BaseEntity;

/**
 * 账户余额实体类
 * 用于数据一致性管理和乐观锁控制
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_account_balance")
public class AccountBalanceEntity extends BaseEntity {

    /**
     * 余额记录ID
     */
    @TableId(value = "balance_id", type = IdType.AUTO)
    private Long balanceId;

    /**
     * 账户ID
     */
    @TableField("account_id")
    private Long accountId;

    /**
     * 人员ID
     */
    @TableField("person_id")
    private Long personId;

    /**
     * 人员姓名
     */
    @TableField("person_name")
    private String personName;

    /**
     * 账户编号
     */
    @TableField("account_no")
    private String accountNo;

    /**
     * 当前余额
     */
    @TableField("balance")
    private BigDecimal balance;

    /**
     * 冻结金额
     */
    @TableField("frozen_amount")
    private BigDecimal frozenAmount;

    /**
     * 可用余额
     */
    @TableField("available_balance")
    private BigDecimal availableBalance;

    /**
     * 信用额度
     */
    @TableField("credit_limit")
    private BigDecimal creditLimit;

    /**
     * 账户类型
     */
    @TableField("account_type")
    private String accountType;

    /**
     * 账户状态
     */
    @TableField("status")
    private String status;

    /**
     * 数据版本号（用于乐观锁）
     * 注意：BaseEntity中version字段为Integer类型，这里需要保持一致
     */
    // version字段继承自BaseEntity，类型为Integer，不需要重复定义

    /**
     * 上次余额
     */
    @TableField("last_balance")
    private BigDecimal lastBalance;

    /**
     * 变动金额
     */
    @TableField("change_amount")
    private BigDecimal changeAmount;

    /**
     * 变动类型（RECHARGE/CONSUME/REFUND/FREEZE/UNFREEZE）
     */
    @TableField("change_type")
    private String changeType;

    /**
     * 变动原因
     */
    @TableField("change_reason")
    private String changeReason;

    /**
     * 关联订单号
     */
    @TableField("order_no")
    private String orderNo;

    /**
     * 变动时间
     */
    @TableField("change_time")
    private LocalDateTime changeTime;

    /**
     * 最后检查时间
     */
    @TableField("last_check_time")
    private LocalDateTime lastCheckTime;

    /**
     * 数据一致性状态（CONSISTENT/INCONSISTENT/CHECKING）
     */
    @TableField("consistency_status")
    private String consistencyStatus;

    /**
     * 是否需要重新计算（0-否，1-是）
     */
    @TableField("need_recalculate")
    private Integer needRecalculate;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    // =================== 重要提示 ===================
    // ❌ 不要重复定义以下审计字段（BaseEntity已包含）:
    // - createTime (创建时间)
    // - updateTime (更新时间)
    // - createUserId (创建人ID)
    // - updateUserId (更新人ID)
    // - deletedFlag (软删除标记) - 已删除重复定义
    // - version (乐观锁版本号)
    //
    // ✅ 如需使用这些字段，直接通过getter/setter访问即可
    // ================================================
}
