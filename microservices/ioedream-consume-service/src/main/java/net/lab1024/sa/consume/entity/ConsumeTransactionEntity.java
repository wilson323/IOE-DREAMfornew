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
 * 消费交易实体
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_consume_transaction")
@Schema(description = "消费交易实体")
public class ConsumeTransactionEntity extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;
    /**
     * 交易ID
     */
    @TableField("transaction_id")
    @Schema(description = "交易ID")
    private Long transactionId;
    /**
     * 交易流水号
     */
    @TableField("transaction_no")
    @Schema(description = "交易流水号")
    private String transactionNo;
    /**
     * 账户编号
     */
    @TableField("account_no")
    @Schema(description = "账户编号")
    private String accountNo;
    /**
     * 交易金额
     */
    @TableField("amount")
    @Schema(description = "交易金额")
    private java.math.BigDecimal amount;
    /**
     * 交易类型
     */
    @TableField("transaction_type")
    @Schema(description = "交易类型")
    private String transactionType;
    /**
     * 交易状态
     */
    @TableField("status")
    @Schema(description = "交易状态")
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
