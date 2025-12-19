package net.lab1024.sa.consume.entity;

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
 * 支付退款记录实体
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_payment_refund_record")
@Schema(description = "支付退款记录实体")
public class PaymentRefundRecordEntity extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;
    /**
     * 退款ID
     */
    @TableField("refund_id")
    @Schema(description = "退款ID")
    private Long refundId;
    /**
     * 退款编号
     */
    @TableField("refund_no")
    @Schema(description = "退款编号")
    private String refundNo;
    /**
     * 原支付ID
     */
    @TableField("payment_id")
    @Schema(description = "原支付ID")
    private Long paymentId;
    /**
     * 退款金额
     */
    @TableField("refund_amount")
    @Schema(description = "退款金额")
    private java.math.BigDecimal refundAmount;
    /**
     * 退款时间
     */
    @TableField("refund_time")
    @Schema(description = "退款时间")
    private java.time.LocalDateTime refundTime;
    /**
     * 退款状态
     */
    @TableField("status")
    @Schema(description = "退款状态")
    private String status;
    /**
     * 备注
     */
    @TableField("remark")
    @Schema(description = "备注")
    private String remark;
    // 注意：createTime, updateTime, createUserId, updateUserId, deletedFlag, version
    // 已由BaseEntity提供，无需重复定义
}
