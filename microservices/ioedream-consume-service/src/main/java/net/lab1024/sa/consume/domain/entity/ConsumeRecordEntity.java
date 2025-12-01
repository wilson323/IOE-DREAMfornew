package net.lab1024.sa.consume.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 消费记录实体类
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Data
@TableName("consume_record")
public class ConsumeRecordEntity {

    /**
     * 消费记录ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 消费单号
     */
    @TableField("consume_no")
    private String consumeNo;

    /**
     * 用户ID（人员ID）
     */
    @TableField("person_id")
    private Long personId;

    /**
     * 设备ID
     */
    @TableField("device_id")
    private Long deviceId;

    /**
     * 消费金额
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 消费类型
     */
    @TableField("consume_type")
    private String consumeType;

    /**
     * 消费时间
     */
    @TableField("consume_time")
    private LocalDateTime consumeTime;

    /**
     * 消费状态
     */
    @TableField("status")
    private String status;

    /**
     * 退款金额
     */
    @TableField("refund_amount")
    private BigDecimal refundAmount;

    /**
     * 退款时间
     */
    @TableField("refund_time")
    private LocalDateTime refundTime;

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