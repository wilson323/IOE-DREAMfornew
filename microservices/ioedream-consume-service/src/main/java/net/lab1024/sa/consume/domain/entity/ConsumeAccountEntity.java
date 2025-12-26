package net.lab1024.sa.consume.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 消费账户实体
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_consume_account")
@Schema(description = "消费账户实体")
public class ConsumeAccountEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "账户ID", example = "1")
    private Long accountId;

    @TableField("user_id")
    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @TableField("account_code")
    @NotBlank(message = "账户编码不能为空")
    @Size(max = 50, message = "账户编码长度不能超过50个字符")
    @Schema(description = "账户编码", example = "ACC_001")
    private String accountCode;

    @TableField("account_name")
    @NotBlank(message = "账户名称不能为空")
    @Size(max = 100, message = "账户名称长度不能超过100个字符")
    @Schema(description = "账户名称", example = "员工食堂账户")
    private String accountName;

    @TableField("account_type")
    @NotNull(message = "账户类型不能为空")
    @Min(value = 1, message = "账户类型值无效")
    @Max(value = 3, message = "账户类型值无效")
    @Schema(description = "账户类型", example = "1")
    private Integer accountType;

    @TableField(exist = false)
    @Schema(description = "账户类型名称", example = "员工账户")
    private String accountTypeName;

    @TableField("balance")
    @NotNull(message = "账户余额不能为空")
    @DecimalMin(value = "0", message = "账户余额不能为负数")
    @Digits(integer = 12, fraction = 2, message = "账户余额格式不正确")
    @Schema(description = "账户余额", example = "100.50")
    private BigDecimal balance;

    @TableField("frozen_amount")
    @DecimalMin(value = "0", message = "冻结金额不能为负数")
    @Digits(integer = 12, fraction = 2, message = "冻结金额格式不正确")
    @Schema(description = "冻结金额", example = "0.00")
    private BigDecimal frozenAmount;

    @TableField("credit_limit")
    @DecimalMin(value = "0", message = "信用额度不能为负数")
    @Digits(integer = 12, fraction = 2, message = "信用额度格式不正确")
    @Schema(description = "信用额度", example = "500.00")
    private BigDecimal creditLimit;

    @TableField("total_recharge")
    @DecimalMin(value = "0", message = "累计充值不能为负数")
    @Digits(integer = 12, fraction = 2, message = "累计充值格式不正确")
    @Schema(description = "累计充值", example = "1000.00")
    private BigDecimal totalRecharge;

    @TableField("total_consume")
    @DecimalMin(value = "0", message = "累计消费不能为负数")
    @Digits(integer = 12, fraction = 2, message = "累计消费格式不正确")
    @Schema(description = "累计消费", example = "899.50")
    private BigDecimal totalConsume;

    @TableField("account_status")
    @NotNull(message = "账户状态不能为空")
    @Min(value = 0, message = "账户状态值无效")
    @Max(value = 1, message = "账户状态值无效")
    @Schema(description = "账户状态", example = "1")
    private Integer accountStatus;

    @TableField(exist = false)
    @Schema(description = "账户状态名称", example = "正常")
    private String accountStatusName;

    @TableField("password")
    @Size(max = 255, message = "支付密码长度不能超过255个字符")
    @Schema(description = "支付密码", example = "")
    private String password;

    @TableField("enable_auto_recharge")
    @NotNull(message = "是否启用自动充值不能为空")
    @Schema(description = "是否启用自动充值", example = "0")
    private Integer enableAutoRecharge;

    @TableField("auto_recharge_amount")
    @DecimalMin(value = "0", message = "自动充值金额不能为负数")
    @Digits(integer = 12, fraction = 2, message = "自动充值金额格式不正确")
    @Schema(description = "自动充值金额", example = "100.00")
    private BigDecimal autoRechargeAmount;

    @TableField("auto_recharge_threshold")
    @DecimalMin(value = "0", message = "自动充值阈值不能为负数")
    @Digits(integer = 12, fraction = 2, message = "自动充值阈值格式不正确")
    @Schema(description = "自动充值阈值", example = "20.00")
    private BigDecimal autoRechargeThreshold;

    @TableField("last_consume_time")
    @Schema(description = "最后消费时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastConsumeTime;

    @TableField("last_recharge_time")
    @Schema(description = "最后充值时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastRechargeTime;

    @TableField("version")
    @Version
    @Schema(description = "版本号", example = "1")
    private Integer version;

    // 审计字段继承自BaseEntity，避免重复定义

    // ==================== 业务状态方法 ====================

    /**
     * 检查账户是否正常
     */
    public boolean isActive() {
        return Integer.valueOf(1).equals(accountStatus);
    }

    /**
     * 检查账户是否冻结
     */
    public boolean isFrozen() {
        return Integer.valueOf(0).equals(accountStatus);
    }

    /**
     * 获取可用余额
     */
    public BigDecimal getAvailableBalance() {
        if (balance == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal frozen = frozenAmount != null ? frozenAmount : BigDecimal.ZERO;
        return balance.subtract(frozen);
    }

    /**
     * 获取总可用额度（余额+信用额度）
     */
    public BigDecimal getTotalAvailableCredit() {
        BigDecimal availableBalance = getAvailableBalance();
        BigDecimal credit = creditLimit != null ? creditLimit : BigDecimal.ZERO;
        return availableBalance.add(credit);
    }

    /**
     * 检查是否可以消费指定金额
     */
    public boolean canConsume(BigDecimal amount) {
        if (!isActive() || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        return getTotalAvailableCredit().compareTo(amount) >= 0;
    }

    /**
     * 检查是否需要自动充值
     */
    public boolean needAutoRecharge() {
        return Integer.valueOf(1).equals(enableAutoRecharge) &&
               autoRechargeAmount != null && autoRechargeAmount.compareTo(BigDecimal.ZERO) > 0 &&
               autoRechargeThreshold != null && getAvailableBalance().compareTo(autoRechargeThreshold) <= 0;
    }

    /**
     * 检查余额是否充足
     */
    public boolean hasSufficientBalance(BigDecimal amount) {
        return getAvailableBalance().compareTo(amount) >= 0;
    }

    /**
     * 获取账户状态描述
     */
    public String getStatusDescription() {
        if (isActive()) {
            BigDecimal frozenAmt = frozenAmount != null ? frozenAmount : BigDecimal.ZERO;
            if (frozenAmt.compareTo(BigDecimal.ZERO) > 0) {
                return "部分冻结";
            }
            return "正常";
        } else {
            return "冻结";
        }
    }

    /**
     * 获取余额状态描述
     */
    public String getBalanceStatusDescription() {
        BigDecimal available = getAvailableBalance();
        if (available.compareTo(BigDecimal.ZERO) <= 0) {
            return "余额不足";
        } else if (available.compareTo(new BigDecimal("20")) <= 0) {
            return "余额较少";
        } else if (available.compareTo(new BigDecimal("100")) <= 0) {
            return "余额正常";
        } else {
            return "余额充足";
        }
    }

    /**
     * 检查是否为高消费用户
     */
    public boolean isHighConsumer() {
        return totalConsume != null && totalConsume.compareTo(new BigDecimal("1000")) >= 0;
    }

    /**
     * 计算消费率
     */
    public BigDecimal getConsumptionRate() {
        if (totalRecharge == null || totalRecharge.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = totalConsume != null ? totalConsume : BigDecimal.ZERO;
        return total.divide(totalRecharge, 4, BigDecimal.ROUND_HALF_UP);
    }

    // ==================== 兼容性方法（为Manager层提供支持）====================

    /**
     * 获取用户名（兼容性别名方法）
     */
    public String getUserName() {
        return accountName;
    }

    /**
     * 获取部门ID（兼容性别名方法）
     */
    public Long getDepartmentId() {
        return getCreateUserId(); // 使用创建人ID作为部门ID的兼容
    }

    /**
     * 获取状态（兼容性方法，返回Integer类型）
     */
    public Integer getStatus() {
        return accountStatus;
    }

    /**
     * 获取版本号（兼容性方法）
     */
    public Integer getVersion() {
        return this.version;
    }

    /**
     * 扣减余额（用于离线消费等场景）
     *
     * @param amount 要扣减的金额
     * @return 是否扣减成功
     */
    public boolean decreaseBalance(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        BigDecimal available = getAvailableBalance();
        if (available.compareTo(amount) < 0) {
            return false;
        }

        this.balance = this.balance.subtract(amount);
        return true;
    }

    /**
     * 增加余额（用于充值、退款等场景）
     *
     * @param amount 要增加的金额
     * @return 是否增加成功
     */
    public boolean increaseBalance(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        this.balance = this.balance.add(amount);
        return true;
    }
}