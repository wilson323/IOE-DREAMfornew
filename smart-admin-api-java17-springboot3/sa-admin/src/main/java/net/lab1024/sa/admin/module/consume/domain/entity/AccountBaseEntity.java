package net.lab1024.sa.admin.module.consume.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.entity.BaseEntity;

/**
 * 消费账户基础实体类
 * <p>
 * 基于扩展表架构设计，存储账户核心信息
 * 与AccountExtensionEntity配合使用，提供完整的账户功能
 *
 * 扩展表架构：t_account_base (基础表) + t_account_extension (扩展表)
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_account_base")
public class AccountBaseEntity extends BaseEntity {

    /**
     * 账户ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long accountId;

    /**
     * 人员ID（主键关联）
     */
    @NotNull(message = "人员ID不能为空")
    @TableField("person_id")
    private Long personId;

    /**
     * 人员姓名
     */
    @TableField("person_name")
    private String personName;

    /**
     * 员工编号
     */
    @TableField("employee_no")
    private String employeeNo;

    /**
     * 账户编号（系统生成的唯一标识）
     */
    @TableField("account_no")
    private String accountNo;

    /**
     * 可用余额
     */
    @TableField("balance")
    private BigDecimal balance;

    /**
     * 冻结金额
     */
    @TableField("frozen_amount")
    private BigDecimal frozenAmount;

    /**
     * 信用额度
     */
    @TableField("credit_limit")
    private BigDecimal creditLimit;

    /**
     * 可用额度（余额 + 信用额度 - 冻结金额）
     */
    @TableField("available_limit")
    private BigDecimal availableLimit;

    /**
     * 账户类型
     * STAFF-员工账户
     * STUDENT-学生账户
     * VISITOR-访客账户
     * TEMP-临时账户
     */
    @TableField("account_type")
    private String accountType;

    /**
     * 账户状态
     * ACTIVE-正常
     * FROZEN-冻结
     * CLOSED-关闭
     * SUSPENDED-暂停
     */
    @TableField("status")
    private String status;

    /**
     * 所属区域ID
     */
    @TableField("region_id")
    private String regionId;

    /**
     * 所属区域名称
     */
    @TableField("region_name")
    private String regionName;

    /**
     * 部门ID
     */
    @TableField("department_id")
    private Long departmentId;

    /**
     * 部门名称
     */
    @TableField("department_name")
    private String departmentName;

    /**
     * 账户等级
     * NORMAL-普通
     * VIP-VIP
     * DIAMOND-钻石
     */
    @TableField("account_level")
    private String accountLevel;

    /**
     * 积分余额
     */
    @TableField("points")
    private Integer points;

    /**
     * 累计消费金额
     */
    @TableField("total_consume_amount")
    private BigDecimal totalConsumeAmount;

    /**
     * 累计充值金额
     */
    @TableField("total_recharge_amount")
    private BigDecimal totalRechargeAmount;

    /**
     * 最后消费时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("last_consume_time")
    private LocalDateTime lastConsumeTime;

    /**
     * 最后充值时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("last_recharge_time")
    private LocalDateTime lastRechargeTime;

    /**
     * 账户创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("account_create_time")
    private LocalDateTime accountCreateTime;

    /**
     * 账户激活时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("active_time")
    private LocalDateTime activeTime;

    /**
     * 账户冻结时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("freeze_time")
    private LocalDateTime freezeTime;

    /**
     * 账户关闭时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("close_time")
    private LocalDateTime closeTime;

    /**
     * 冻结原因
     */
    @TableField("freeze_reason")
    private String freezeReason;

    /**
     * 关闭原因
     */
    @TableField("close_reason")
    private String closeReason;

    // ==================== 业务方法 ====================

    /**
     * 计算可用额度
     * 可用额度 = 余额 + 信用额度 - 冻结金额
     *
     * @return 可用额度
     */
    public BigDecimal calculateAvailableLimit() {
        BigDecimal limit = BigDecimal.ZERO;
        if (balance != null) {
            limit = limit.add(balance);
        }
        if (creditLimit != null) {
            limit = limit.add(creditLimit);
        }
        if (frozenAmount != null) {
            limit = limit.subtract(frozenAmount);
        }
        return limit;
    }

    /**
     * 检查账户是否正常
     *
     * @return 是否正常
     */
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    /**
     * 检查账户是否冻结
     *
     * @return 是否冻结
     */
    public boolean isFrozen() {
        return "FROZEN".equals(status);
    }

    /**
     * 检查账户是否关闭
     *
     * @return 是否关闭
     */
    public boolean isClosed() {
        return "CLOSED".equals(status);
    }

    /**
     * 检查余额是否充足
     *
     * @param amount 需要检查的金额
     * @return 余额是否充足
     */
    public boolean hasEnoughBalance(BigDecimal amount) {
        if (amount == null || balance == null) {
            return false;
        }
        return balance.compareTo(amount) >= 0;
    }

    /**
     * 检查可用额度是否充足
     *
     * @param amount 需要检查的金额
     * @return 可用额度是否充足
     */
    public boolean hasEnoughAvailableLimit(BigDecimal amount) {
        if (amount == null) {
            return false;
        }
        BigDecimal available = calculateAvailableLimit();
        return available.compareTo(amount) >= 0;
    }

    /**
     * 获取账户状态描述
     *
     * @return 状态描述
     */
    public String getStatusDescription() {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case "ACTIVE":
                return "正常";
            case "FROZEN":
                return "冻结";
            case "CLOSED":
                return "关闭";
            case "SUSPENDED":
                return "暂停";
            default:
                return status;
        }
    }

    /**
     * 获取账户类型描述
     *
     * @return 类型描述
     */
    public String getAccountTypeDescription() {
        if (accountType == null) {
            return "未知";
        }
        switch (accountType) {
            case "STAFF":
                return "员工账户";
            case "STUDENT":
                return "学生账户";
            case "VISITOR":
                return "访客账户";
            case "TEMP":
                return "临时账户";
            default:
                return accountType;
        }
    }

    /**
     * 更新余额
     *
     * @param changeAmount 变化金额（正数为增加，负数为减少）
     */
    public void updateBalance(BigDecimal changeAmount) {
        if (changeAmount != null && balance != null) {
            this.balance = balance.add(changeAmount);
            // 更新可用额度
            this.availableLimit = calculateAvailableLimit();
        }
    }

    /**
     * 更新累计消费金额
     *
     * @param consumeAmount 消费金额
     */
    public void updateTotalConsumeAmount(BigDecimal consumeAmount) {
        if (consumeAmount != null) {
            if (totalConsumeAmount == null) {
                totalConsumeAmount = BigDecimal.ZERO;
            }
            this.totalConsumeAmount = totalConsumeAmount.add(consumeAmount);
        }
    }

    /**
     * 更新累计充值金额
     *
     * @param rechargeAmount 充值金额
     */
    public void updateTotalRechargeAmount(BigDecimal rechargeAmount) {
        if (rechargeAmount != null) {
            if (totalRechargeAmount == null) {
                totalRechargeAmount = BigDecimal.ZERO;
            }
            this.totalRechargeAmount = totalRechargeAmount.add(rechargeAmount);
        }
    }
}