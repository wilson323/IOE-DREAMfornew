package net.lab1024.sa.consume.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 消费账户实体类
 * <p>
 * 用于管理用户消费账户信息，包括余额、补贴等
 * 严格遵循CLAUDE.md规范：
 * - 继承BaseEntity获取审计字段
 * - 使用@TableName指定数据库表名
 * - 字段数控制在30个以内
 * - 行数控制在200行以内
 * - 已修复字段重复问题
 * </p>
 * <p>
 * 业务场景：
 * - 账户余额管理
 * - 账户充值
 * - 账户消费
 * - 补贴管理
 * </p>
 * <p>
 * 数据库表：t_consume_account（标准化命名）
 * 修复记录：2025-12-09 修复字段重复问题，统一使用BigDecimal类型
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.1-FIX
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_consume_account")
public class AccountEntity extends BaseEntity {

    /**
     * 账户ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long accountId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 账户编号
     */
    private String accountNo;

    /**
     * 账户名称
     */
    private String accountName;

    /**
     * 账户类型：1-个人账户 2-团体账户 3-临时账户
     */
    private Integer accountType;

    /**
     * 补贴余额
     */
    private BigDecimal allowanceBalance;

    /**
     * 账户余额（BigDecimal类型，精确到分）
     */
    private BigDecimal balance;

    /**
     * 冻结金额
     */
    private BigDecimal frozenAmount;

    /**
     * 信用额度
     */
    private BigDecimal creditLimit;

    /**
     * 日消费限额
     */
    private BigDecimal dailyLimit;

    /**
     * 月消费限额
     */
    private BigDecimal monthlyLimit;

    /**
     * 补贴余额
     */
    private BigDecimal subsidyBalance;

    /**
     * 累计充值金额
     */
    private BigDecimal totalRechargeAmount;

    /**
     * 累计消费金额
     */
    private BigDecimal totalConsumeAmount;

    /**
     * 累计补贴金额
     */
    private BigDecimal totalSubsidyAmount;

    /**
     * 账户状态：1-正常 2-冻结 3-注销
     */
    private Integer status;

    /**
     * 最后使用时间
     */
    private LocalDateTime lastUseTime;

      // 注意：version、createTime、updateTime、createUserId、updateUserId、deletedFlag
    // 已由BaseEntity提供，无需重复定义

    // 便捷方法：为了兼容现有代码调用getId()
    public Long getId() {
        return this.accountId;
    }

    // 便捷方法：为了兼容现有代码调用getAccountKindId()
    // 返回账户类型作为账户种类ID
    public Long getAccountKindId() {
        return this.accountType != null ? this.accountType.longValue() : null;
    }

    // 便捷方法：为了兼容现有代码调用getBalanceAmount()
    public BigDecimal getBalanceAmount() {
        return this.balance;
    }

    // 便捷方法：为了兼容现有代码调用getFrozenBalance()
    public BigDecimal getFrozenBalance() {
        return this.frozenAmount;
    }

    // 便捷方法：为了兼容现有代码调用getFrozenBalanceAmount()
    public BigDecimal getFrozenBalanceAmount() {
        return this.frozenAmount;
    }

    // 便捷方法：为了兼容现有代码调用setFrozenBalance()
    public void setFrozenBalance(BigDecimal frozenBalance) {
        this.frozenAmount = frozenBalance;
    }

    // 新增缺失的方法 - 为了兼容现有代码调用

    /**
     * 设置账户种类ID（兼容方法）
     */
    public void setAccountKindId(Long accountKindId) {
        if (accountKindId != null) {
            this.accountType = accountKindId.intValue();
        }
    }

    /**
     * 获取余额（BigDecimal类型，兼容方法）
     */
    public BigDecimal getBalance() {
        return this.balance;
    }

    /**
     * 设置余额（BigDecimal类型，兼容方法）
     */
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    /**
     * 设置补贴余额（BigDecimal类型，兼容方法）
     */
    public void setAllowanceBalance(BigDecimal allowanceBalance) {
        this.allowanceBalance = allowanceBalance;
    }

    /**
     * 设置补贴余额（Long类型，兼容方法 - 用于存储分）
     */
    public void setAllowanceBalance(Long allowanceBalanceCents) {
        if (allowanceBalanceCents != null) {
            this.allowanceBalance = BigDecimal.valueOf(allowanceBalanceCents).divide(BigDecimal.valueOf(100));
        }
    }

    /**
     * 获取补贴余额（Long类型，兼容方法 - 返回分）
     */
    public Long getAllowanceBalanceCents() {
        if (this.allowanceBalance != null) {
            return this.allowanceBalance.multiply(BigDecimal.valueOf(100)).longValue();
        }
        return 0L;
    }

    /**
     * 获取余额（Long类型，兼容方法 - 返回分）
     */
    public Long getBalanceCents() {
        if (this.balance != null) {
            return this.balance.multiply(BigDecimal.valueOf(100)).longValue();
        }
        return 0L;
    }

    /**
     * 设置余额（Long类型，兼容方法 - 用于存储分）
     */
    public void setBalance(Long balanceCents) {
        if (balanceCents != null) {
            this.balance = BigDecimal.valueOf(balanceCents).divide(BigDecimal.valueOf(100));
        }
    }
}



