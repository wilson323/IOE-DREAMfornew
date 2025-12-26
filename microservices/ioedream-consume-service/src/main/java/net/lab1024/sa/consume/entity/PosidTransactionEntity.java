package net.lab1024.sa.consume.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * POSID交易流水表实体
 *
 * 对应表: POSID_TRANSACTION
 * 表说明: 交易流水表，按月分区，支持离线消费同步
 *
 * 核心字段:
 * - transaction_id: 交易ID（主键的一部分）
 * - account_id: 账户ID
 * - user_id: 用户ID
 * - amount: 交易金额
 * - consume_mode: 消费模式（FIXED_AMOUNT/FREE_AMOUNT/METERED/PRODUCT/ORDER/INTELLIGENCE）
 * - offline_flag: 离线标记（0-在线，1-离线）
 * - sync_status: 同步状态（0-未同步，1-已同步）
 *
 * 分区策略: 按create_time按月分区
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-23
 */
@Data
@TableName("POSID_TRANSACTION")
public class PosidTransactionEntity {

    /**
     * 交易ID
     */
    @TableId(type = IdType.AUTO)
    private Long transactionId;

    /**
     * 账户ID
     */
    @TableField("account_id")
    private Long accountId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户姓名
     */
    @TableField("user_name")
    private String userName;

    /**
     * 设备ID
     */
    @TableField("device_id")
    private String deviceId;

    /**
     * 设备名称
     */
    @TableField("device_name")
    private String deviceName;

    /**
     * 商户ID
     */
    @TableField("merchant_id")
    private Long merchantId;

    /**
     * 商户名称
     */
    @TableField("merchant_name")
    private String merchantName;

    /**
     * 交易金额
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 消费模式（FIXED_AMOUNT/FREE_AMOUNT/METERED/PRODUCT/ORDER/INTELLIGENCE）
     */
    @TableField("consume_mode")
    private String consumeMode;

    /**
     * 消费类型（MEAL-餐饮，SNACK-零食，DRINK-饮品）
     */
    @TableField("consume_type")
    private String consumeType;

    /**
     * 消费类型名称
     */
    @TableField("consume_type_name")
    private String consumeTypeName;

    /**
     * 商品明细（JSON格式）
     */
    @TableField("product_detail")
    private String productDetail;

    /**
     * 支付方式（BALANCE-余额，SUBSIDY-补贴，CARD-卡）
     */
    @TableField("payment_method")
    private String paymentMethod;

    /**
     * 订单号
     */
    @TableField("order_no")
    private String orderNo;

    /**
     * 交易流水号
     */
    @TableField("transaction_no")
    private String transactionNo;

    /**
     * 交易状态（1-成功，2-处理中，3-失败）
     */
    @TableField("transaction_status")
    private Integer transactionStatus;

    /**
     * 消费状态（1-正常，2-已退款，3-已撤销）
     */
    @TableField("consume_status")
    private Integer consumeStatus;

    /**
     * 消费时间
     */
    @TableField("consume_time")
    private LocalDateTime consumeTime;

    /**
     * 消费地点
     */
    @TableField("consume_location")
    private String consumeLocation;

    /**
     * 退款状态（0-未退款，1-部分退款，2-全额退款）
     */
    @TableField("refund_status")
    private Integer refundStatus;

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
     * 退款原因
     */
    @TableField("refund_reason")
    private String refundReason;

    /**
     * 离线标记（0-在线，1-离线）
     */
    @TableField("offline_flag")
    private Integer offlineFlag;

    /**
     * 同步状态（0-未同步，1-已同步）
     */
    @TableField("sync_status")
    private Integer syncStatus;

    /**
     * 同步时间
     */
    @TableField("sync_time")
    private LocalDateTime syncTime;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建时间（分区键）
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 删除标记
     */
    @TableField("deleted_flag")
    private Integer deletedFlag;
}
