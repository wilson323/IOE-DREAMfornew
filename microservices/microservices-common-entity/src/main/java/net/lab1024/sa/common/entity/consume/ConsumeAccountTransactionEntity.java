package net.lab1024.sa.common.entity.consume;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 账户交易记录实体类
 * <p>
 * 记录账户资金变动详情，包括充值、消费、退款、冻结等操作
 * 提供完整的交易流水跟踪和审计功能
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 * <p>
 * 业务场景：
 * - 账户充值记录
 * - 消费扣款记录
 * - 退款入账记录
 * - 账户冻结/解冻
 * - 交易流水查询
 * - 资金变动审计
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_consume_account_transaction")
@Schema(description = "账户交易记录实体")
public class ConsumeAccountTransactionEntity extends BaseEntity {

    /**
     * 交易ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "交易ID")
    private Long transactionId;

    /**
     * 交易流水号（业务唯一）
     */
    @Schema(description = "交易流水号")
    private String transactionNo;

    /**
     * 业务编号（关联业务订单）
     */
    @Schema(description = "业务编号")
    private String businessNo;

    /**
     * 关联订单号
     */
    @Schema(description = "关联订单号")
    private String relatedOrderNo;

    /**
     * 账户ID
     */
    @Schema(description = "账户ID")
    private Long accountId;

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
     * 交易类型（RECHARGE-充值 CONSUME-消费 REFUND-退款 DEDUCT-扣款 FREEZE-冻结 UNFREEZE-解冻）
     */
    @Schema(description = "交易类型")
    private String transactionType;

    /**
     * 交易金额（正数表示增加，负数表示减少）
     */
    @Schema(description = "交易金额")
    private BigDecimal amount;

    /**
     * 交易前余额
     */
    @Schema(description = "交易前余额")
    private BigDecimal balanceBefore;

    /**
     * 交易后余额
     */
    @Schema(description = "交易后余额")
    private BigDecimal balanceAfter;

    /**
     * 冻结金额变动（冻结为正，解冻为负）
     */
    @Schema(description = "冻结金额变动")
    private BigDecimal frozenAmountChange;

    /**
     * 冻结前余额
     */
    @Schema(description = "冻结前余额")
    private BigDecimal frozenAmountBefore;

    /**
     * 冻结后余额
     */
    @Schema(description = "冻结后余额")
    private BigDecimal frozenAmountAfter;

    /**
     * 交易时间
     */
    @Schema(description = "交易时间")
    private LocalDateTime transactionTime;

    /**
     * 交易状态（1-成功 2-处理中 3-失败 4-已取消）
     */
    @Schema(description = "交易状态")
    private Integer transactionStatus;

    /**
     * 交易状态描述
     */
    @Schema(description = "交易状态描述")
    private String transactionStatusDesc;

    /**
     * 设备ID
     */
    @Schema(description = "设备ID")
    private String deviceId;

    /**
     * 设备名称
     */
    @Schema(description = "设备名称")
    private String deviceName;

    /**
     * 操作员ID
     */
    @Schema(description = "操作员ID")
    private Long operatorId;

    /**
     * 操作员姓名
     */
    @Schema(description = "操作员姓名")
    private String operatorName;

    /**
     * 交易备注
     */
    @Schema(description = "交易备注")
    private String remark;

    /**
     * 扩展属性（JSON格式，存储业务特定的扩展信息）
     */
    @TableField(exist = false)
    @Schema(description = "扩展属性")
    private String extendedAttributes;
}
