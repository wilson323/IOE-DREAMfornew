package net.lab1024.sa.consume.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 账户变动记录实体
 * <p>
 * 用于记录消费账户的所有余额变动
 * 包含充值、消费、退款、扣减、调整等所有操作
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_consume_account_transaction")
@Schema(description = "账户变动记录实体")
public class ConsumeAccountTransactionEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "交易ID", example = "20001")
    private Long transactionId;

    @TableField("account_id")
    @NotNull(message = "账户ID不能为空")
    @Schema(description = "账户ID", example = "1001")
    private Long accountId;

    @TableField("user_id")
    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "10001")
    private Long userId;

    @TableField("transaction_type")
    @NotBlank(message = "交易类型不能为空")
    @Size(max = 20, message = "交易类型长度不能超过20个字符")
    @Schema(description = "交易类型", example = "CONSUME",
            allowableValues = {"CONSUME", "RECHARGE", "REFUND", "DEDUCT", "ADJUST"})
    private String transactionType;

    @TableField("transaction_no")
    @NotBlank(message = "交易流水号不能为空")
    @Size(max = 64, message = "交易流水号长度不能超过64个字符")
    @Schema(description = "交易流水号", example = "TXN20251223001")
    private String transactionNo;

    @TableField("business_no")
    @NotBlank(message = "业务编号不能为空")
    @Size(max = 64, message = "业务编号长度不能超过64个字符")
    @Schema(description = "业务编号", example = "ORDER20251223001")
    private String businessNo;

    @TableField("amount")
    @NotNull(message = "变动金额不能为空")
    @Digits(integer = 10, fraction = 2, message = "变动金额格式不正确")
    @Schema(description = "变动金额（正-增加，负-减少）", example = "-25.50")
    private BigDecimal amount;

    @TableField("balance_before")
    @NotNull(message = "变动前余额不能为空")
    @Digits(integer = 10, fraction = 2, message = "变动前余额格式不正确")
    @Schema(description = "变动前余额", example = "100.00")
    private BigDecimal balanceBefore;

    @TableField("balance_after")
    @NotNull(message = "变动后余额不能为空")
    @Digits(integer = 10, fraction = 2, message = "变动后余额格式不正确")
    @Schema(description = "变动后余额", example = "74.50")
    private BigDecimal balanceAfter;

    @TableField("frozen_amount_before")
    @NotNull(message = "变动前冻结金额不能为空")
    @Digits(integer = 10, fraction = 2, message = "变动前冻结金额格式不正确")
    @Schema(description = "变动前冻结金额", example = "0.00")
    private BigDecimal frozenAmountBefore;

    @TableField("frozen_amount_after")
    @NotNull(message = "变动后冻结金额不能为空")
    @Digits(integer = 10, fraction = 2, message = "变动后冻结金额格式不正确")
    @Schema(description = "变动后冻结金额", example = "0.00")
    private BigDecimal frozenAmountAfter;

    @TableField("related_record_id")
    @Schema(description = "关联记录ID", example = "10001")
    private Long relatedRecordId;

    @TableField("related_order_no")
    @Size(max = 64, message = "关联订单号长度不能超过64个字符")
    @Schema(description = "关联订单号", example = "ORDER20251223001")
    private String relatedOrderNo;

    @TableField("transaction_status")
    @NotNull(message = "交易状态不能为空")
    @Min(value = 1, message = "交易状态值无效")
    @Max(value = 3, message = "交易状态值无效")
    @Schema(description = "交易状态（1-成功，2-处理中，3-失败）", example = "1")
    private Integer transactionStatus;

    @TableField("fail_reason")
    @Size(max = 200, message = "失败原因长度不能超过200个字符")
    @Schema(description = "失败原因", example = "余额不足")
    private String failReason;

    @TableField("transaction_time")
    @NotNull(message = "交易时间不能为空")
    @Schema(description = "交易时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime transactionTime;

    @TableField("operator_id")
    @Schema(description = "操作员ID", example = "100")
    private Long operatorId;

    @TableField("operator_name")
    @Size(max = 50, message = "操作员姓名长度不能超过50个字符")
    @Schema(description = "操作员姓名", example = "管理员")
    private String operatorName;

    // ==================== 业务方法 ====================

    /**
     * 判断是否为增加余额
     */
    public boolean isIncrease() {
        return this.amount != null && this.amount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 判断是否为减少余额
     */
    public boolean isDecrease() {
        return this.amount != null && this.amount.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * 判断交易是否成功
     */
    public boolean isSuccess() {
        return Integer.valueOf(1).equals(this.transactionStatus);
    }

    /**
     * 判断是否为充值类型
     */
    public boolean isRecharge() {
        return "RECHARGE".equals(this.transactionType);
    }

    /**
     * 判断是否为消费类型
     */
    public boolean isConsume() {
        return "CONSUME".equals(this.transactionType) || "DEDUCT".equals(this.transactionType);
    }

    /**
     * 判断是否为退款类型
     */
    public boolean isRefund() {
        return "REFUND".equals(this.transactionType);
    }

    /**
     * 判断是否为调整类型
     */
    public boolean isAdjust() {
        return "ADJUST".equals(this.transactionType);
    }
}
