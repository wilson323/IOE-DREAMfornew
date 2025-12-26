package net.lab1024.sa.common.entity.consume;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 消费账户实体类
 * <p>
 * 管理消费账户信息，支持余额管理和信用支付
 * 提供账户充值、消费、冻结等功能
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 * <p>
 * 业务场景：
 * - 账户余额管理
 * - 充值退款处理
 * - 消费扣款
 * - 账户冻结解冻
 * - 信用额度管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("consume_account")
@Schema(description = "消费账户实体")
public class ConsumeAccountEntity extends BaseEntity {

    /**
     * 账户ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "账户ID")
    private Long accountId;

    /**
     * 账户编号（唯一）
     */
    @Schema(description = "账户编号")
    private String accountCode;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 用户姓名
     */
    @Schema(description = "用户姓名")
    private String userName;

    /**
     * 部门ID
     */
    @Schema(description = "部门ID")
    private Long departmentId;

    /**
     * 部门名称
     */
    @Schema(description = "部门名称")
    private String departmentName;

    /**
     * 账户类型（1-个人账户 2-部门账户 3-临时账户 4-访客账户）
     */
    @Schema(description = "账户类型（1-个人账户 2-部门账户 3-临时账户 4-访客账户）")
    private Integer accountType;

    /**
     * 账户类型名称
     */
    @Schema(description = "账户类型名称")
    private String accountTypeName;

    /**
     * 账户余额（元）
     */
    @Schema(description = "账户余额")
    private BigDecimal balance;

    /**
     * 冻结金额（元）
     */
    @Schema(description = "冻结金额")
    private BigDecimal frozenAmount;

    /**
     * 可用余额（元）= 余额 - 冻结金额
     */
    @Schema(description = "可用余额")
    private BigDecimal availableBalance;

    /**
     * 信用额度（元）
     */
    @Schema(description = "信用额度")
    private BigDecimal creditLimit;

    /**
     * 已用信用额度（元）
     */
    @Schema(description = "已用信用额度")
    private BigDecimal usedCredit;

    /**
     * 可用信用额度（元）= 信用额度 - 已用信用额度
     */
    @Schema(description = "可用信用额度")
    private BigDecimal availableCredit;

    /**
     * 账户状态（1-正常 2-冻结 3-注销 4-挂失）
     */
    @Schema(description = "账户状态（1-正常 2-冻结 3-注销 4-挂失）")
    private Integer status;

    /**
     * 账户状态名称
     */
    @Schema(description = "账户状态名称")
    private String statusName;

    /**
     * 支付密码（加密存储）
     */
    @Schema(description = "支付密码")
    private String password;

    /**
     * 是否设置密码（0-未设置 1-已设置）
     */
    @Schema(description = "是否设置密码")
    private Integer hasPassword;

    /**
     * 安全等级（1-低 2-中 3-高）
     */
    @Schema(description = "安全等级（1-低 2-中 3-高）")
    private Integer securityLevel;

    /**
     * 积分
     */
    @Schema(description = "积分")
    private Integer points;

    /**
     * 最后交易时间
     */
    @Schema(description = "最后交易时间")
    private LocalDateTime lastTransactionTime;

    /**
     * 最后交易地点
     */
    @Schema(description = "最后交易地点")
    private String lastTransactionLocation;

    /**
     * 最后交易金额（元）
     */
    @Schema(description = "最后交易金额")
    private BigDecimal lastTransactionAmount;

    /**
     * 总充值金额（元）
     */
    @Schema(description = "总充值金额")
    private BigDecimal totalRecharge;

    /**
     * 总消费金额（元）
     */
    @Schema(description = "总消费金额")
    private BigDecimal totalConsume;

    /**
     * 总退款金额（元）
     */
    @Schema(description = "总退款金额")
    private BigDecimal totalRefund;

    /**
     * 交易次数
     */
    @Schema(description = "交易次数")
    private Integer transactionCount;

    /**
     * 冻结原因
     */
    @Schema(description = "冻结原因")
    private String freezeReason;

    /**
     * 冻结时间
     */
    @Schema(description = "冻结时间")
    private LocalDateTime freezeTime;

    /**
     * 冻结人ID
     */
    @Schema(description = "冻结人ID")
    private Long freezerId;

    /**
     * 冻结人姓名
     */
    @Schema(description = "冻结人姓名")
    private String freezerName;

    /**
     * 开户时间
     */
    @Schema(description = "开户时间")
    private LocalDateTime openTime;

    /**
     * 账户备注
     */
    @Schema(description = "账户备注")
    private String remark;

    /**
     * 乐观锁版本号
     */
    @Version
    @Schema(description = "乐观锁版本号")
    private Integer version;

    /**
     * 扩展属性（JSON格式，存储业务特定的扩展信息）
     */
    @TableField(exist = false)
    @Schema(description = "扩展属性")
    private String extendedAttributes;

    // ==================== 业务方法 ====================

    /**
     * 检查账户是否正常
     *
     * @return true-正常，false-异常
     */
    public boolean isNormal() {
        return status != null && status == 1;
    }

    /**
     * 检查账户是否冻结
     *
     * @return true-冻结，false-未冻结
     */
    public boolean isFrozen() {
        return status != null && status == 2;
    }

    /**
     * 检查余额是否充足
     *
     * @param amount 需要的金额
     * @return true-余额充足，false-余额不足
     */
    public boolean hasEnoughBalance(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return true;
        }
        BigDecimal available = availableBalance != null ? availableBalance : BigDecimal.ZERO;
        return available.compareTo(amount) >= 0;
    }

    /**
     * 检查是否设置了密码
     *
     * @return true-已设置，false-未设置
     */
    public boolean hasPassword() {
        return hasPassword != null && hasPassword == 1;
    }

    /**
     * 计算总可用额度（可用余额 + 可用信用额度）
     *
     * @return 总可用额度
     */
    public BigDecimal getTotalAvailableCredit() {
        BigDecimal available = availableBalance != null ? availableBalance : BigDecimal.ZERO;
        BigDecimal credit = availableCredit != null ? availableCredit : BigDecimal.ZERO;
        return available.add(credit);
    }

    /**
     * 冻结账户
     *
     * @param reason 冻结原因
     * @param freezerId 冻结人ID
     * @param freezerName 冻结人姓名
     */
    public void freezeAccount(String reason, Long freezerId, String freezerName) {
        this.status = 2;
        this.freezeReason = reason;
        this.freezeTime = LocalDateTime.now();
        this.freezerId = freezerId;
        this.freezerName = freezerName;
    }

    /**
     * 解冻账户
     */
    public void unfreezeAccount() {
        this.status = 1;
        this.freezeReason = null;
        this.freezeTime = null;
        this.freezerId = null;
        this.freezerName = null;
    }

    /**
     * 扣款（需要验证余额和版本号）
     *
     * @param amount 扣款金额
     * @param currentVersion 当前版本号
     * @return true-扣款成功，false-余额不足或版本号不匹配
     */
    public boolean deductBalance(BigDecimal amount, Integer currentVersion) {
        if (!hasEnoughBalance(amount)) {
            return false;
        }
        if (!this.version.equals(currentVersion)) {
            return false;
        }

        this.balance = this.balance.subtract(amount);
        this.availableBalance = this.availableBalance.subtract(amount);
        this.totalConsume = this.totalConsume.add(amount);
        this.transactionCount = this.transactionCount + 1;
        this.lastTransactionAmount = amount;
        this.lastTransactionTime = LocalDateTime.now();
        this.version = this.version + 1;

        return true;
    }

    /**
     * 充值
     *
     * @param amount 充值金额
     */
    public void recharge(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        this.balance = this.balance.add(amount);
        this.availableBalance = this.availableBalance.add(amount);
        this.totalRecharge = this.totalRecharge.add(amount);
        this.transactionCount = this.transactionCount + 1;
        this.lastTransactionAmount = amount;
        this.lastTransactionTime = LocalDateTime.now();
    }

    /**
     * 退款
     *
     * @param amount 退款金额
     */
    public void refund(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        this.balance = this.balance.add(amount);
        this.availableBalance = this.availableBalance.add(amount);
        this.totalRefund = this.totalRefund.add(amount);
    }

    /**
     * 获取账户状态名称
     *
     * @return 状态名称
     */
    public String getStatusName() {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 1: return "正常";
            case 2: return "冻结";
            case 3: return "注销";
            case 4: return "挂失";
            default: return "未知";
        }
    }

    /**
     * 获取账户类型名称
     *
     * @return 类型名称
     */
    public String getAccountTypeName() {
        if (accountType == null) {
            return "未知";
        }
        switch (accountType) {
            case 1: return "个人账户";
            case 2: return "部门账户";
            case 3: return "临时账户";
            case 4: return "访客账户";
            default: return "未知";
        }
    }
}
