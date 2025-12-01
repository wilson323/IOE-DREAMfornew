package net.lab1024.sa.consume.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 退款记录实体类
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Data
@TableName("refund_record")
public class RefundRecordEntity {

    /**
     * 退款记录ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 退款单号（业务唯一标识）
     */
    @TableField("refund_id")
    private String refundId;

    /**
     * 原消费记录ID
     */
    @TableField("consume_record_id")
    private Long consumeRecordId;

    /**
     * 退款金额
     */
    @TableField("refund_amount")
    private BigDecimal refundAmount;

    /**
     * 退款原因
     */
    @TableField("reason")
    private String reason;

    /**
     * 退款状态（0-待处理，1-处理中，2-成功，3-失败，4-已取消）
     */
    @TableField("status")
    private String status;

    /**
     * 申请时间
     */
    @TableField("apply_time")
    private LocalDateTime applyTime;

    /**
     * 处理时间
     */
    @TableField("process_time")
    private LocalDateTime processTime;

    /**
     * 退款完成时间
     */
    @TableField("refund_time")
    private LocalDateTime refundTime;

    /**
     * 操作人员名称
     */
    @TableField("operator_name")
    private String operatorName;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 删除标记（0-未删除，1-已删除）
     */
    @TableField("deleted")
    private Integer deleted;
}