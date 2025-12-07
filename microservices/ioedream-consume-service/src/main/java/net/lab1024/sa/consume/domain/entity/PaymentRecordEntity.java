package net.lab1024.sa.consume.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 支付记录实体类
 * <p>
 * 用于记录第三方支付（支付宝、微信）的支付记录
 * 严格遵循CLAUDE.md规范：
 * - 继承BaseEntity获取审计字段
 * - 使用@TableName指定数据库表名
 * - 字段数控制在30个以内
 * - 行数控制在200行以内
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("payment_record")
public class PaymentRecordEntity extends BaseEntity {

    /**
     * 支付记录ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 支付订单号（唯一）
     */
    private String paymentId;

    /**
     * 交易ID（关联消费交易）
     */
    private String transactionId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 支付方式
     * <p>
     * ALIPAY-支付宝
     * WECHAT-微信支付
     * </p>
     */
    private String paymentMethod;

    /**
     * 支付状态
     * <p>
     * PENDING-待支付
     * SUCCESS-支付成功
     * FAILED-支付失败
     * REFUNDED-已退款
     * </p>
     */
    private String status;

    /**
     * 第三方交易号
     */
    private String thirdPartyTransactionId;

    /**
     * 支付时间
     */
    private LocalDateTime paymentTime;

    /**
     * 回调时间
     */
    private LocalDateTime callbackTime;

    /**
     * 回调数据（JSON格式）
     */
    private String callbackData;

    /**
     * 备注
     */
    private String remark;

    /**
     * 工作流实例ID
     * <p>
     * 关联OA工作流模块的流程实例ID
     * 用于查询审批状态、审批历史等
     * </p>
     */
    @TableField("workflow_instance_id")
    private Long workflowInstanceId;
}
