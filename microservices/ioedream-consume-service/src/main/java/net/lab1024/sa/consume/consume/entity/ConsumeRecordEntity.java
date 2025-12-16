package net.lab1024.sa.consume.consume.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
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
 * 消费记录实体类
 * <p>
 * 借鉴Smart-Admin优秀设计，完善字段定义，确保100%业务功能覆盖
 * 严格遵循CLAUDE.md规范：
 * - 继承BaseEntity获取审计字段
 * - 使用@TableName指定数据库表名
 * - 字段设计全面，支持完整业务流程
 * - 行数控制在合理范围内
 * </p>
 * <p>
 * 业务场景：
 * - 消费流水记录（完整的余额变化记录）
 * - 消费统计分析（多维度数据支持）
 * - 对账和审计（完整的审计信息）
 * - 退款处理（完整的退款流程）
 * - 多维度数据查询（灵活的数据查询）
 * </p>
 * <p>
 * 设计优势：
 * - 1. 完整的余额跟踪（消费前后余额）
 * - 2. 完整的支付信息（支付方式、时间、交易号）
 * - 3. 完整的退款支持（退款金额、时间、原因）
 * - 4. 完整的扩展性（JSON扩展字段）
 * - 5. 完整的审计跟踪（用户信息、设备信息）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.1.0-ENHANCED
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_consume_record")
@Schema(description = "消费记录实体")
public class ConsumeRecordEntity extends BaseEntity {

    /**
     * 消费记录ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "消费记录ID")
    private Long id;

    /**
     * 交易流水号（唯一）
     */
    @TableField("transaction_no")
    @Schema(description = "交易流水号")
    private String transactionNo;

    /**
     * 订单号（业务订单号）
     */
    @TableField("order_no")
    @Schema(description = "订单号")
    private String orderNo;

    /**
     * 用户ID
     */
    @TableField("user_id")
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 用户姓名
     */
    @TableField("user_name")
    @Schema(description = "用户姓名")
    private String userName;

    /**
     * 用户手机号
     */
    @TableField("user_phone")
    @Schema(description = "用户手机号")
    private String userPhone;

    /**
     * 用户类型（与AccountEntity.account_type对应）
     */
    @TableField("user_type")
    @Schema(description = "用户类型")
    private Integer userType;

    /**
     * 账户ID
     */
    @TableField("account_id")
    @Schema(description = "账户ID")
    private Long accountId;

    /**
     * 账户编号
     */
    @TableField("account_no")
    @Schema(description = "账户编号")
    private String accountNo;

    /**
     * 账户名称
     */
    @TableField("account_name")
    @Schema(description = "账户名称")
    private String accountName;

    /**
     * 区域ID
     */
    @TableField("area_id")
    @Schema(description = "区域ID")
    private Long areaId;

    /**
     * 区域名称
     */
    @TableField("area_name")
    @Schema(description = "区域名称")
    private String areaName;

    /**
     * 设备ID
     */
    @TableField("device_id")
    @Schema(description = "设备ID")
    private Long deviceId;

    /**
     * 设备名称
     */
    @TableField("device_name")
    @Schema(description = "设备名称")
    private String deviceName;

    /**
     * 设备编号
     */
    @TableField("device_no")
    @Schema(description = "设备编号")
    private String deviceNo;

    /**
     * 消费日期
     */
    @TableField("consume_date")
    @Schema(description = "消费日期")
    private LocalDate consumeDate;

    /**
     * 消费金额
     */
    @TableField("amount")
    @Schema(description = "消费金额")
    private BigDecimal amount;

    /**
     * 消费金额别名（兼容字段）
     */
    @TableField("consume_amount")
    @Schema(description = "消费金额别名")
    private BigDecimal consumeAmount;

    /**
     * 消费前余额
     */
    @TableField("balance_before")
    @Schema(description = "消费前余额")
    private BigDecimal balanceBefore;

    /**
     * 消费后余额
     */
    @TableField("balance_after")
    @Schema(description = "消费后余额")
    private BigDecimal balanceAfter;

    /**
     * 币种
     */
    @TableField("currency")
    @Schema(description = "币种")
    private String currency;

    /**
     * 汇率
     */
    @TableField("exchange_rate")
    @Schema(description = "汇率")
    private BigDecimal exchangeRate;

    /**
     * 折扣金额
     */
    @TableField("discount_amount")
    @Schema(description = "折扣金额")
    private BigDecimal discountAmount;

    /**
     * 补贴金额
     */
    @TableField("subsidy_amount")
    @Schema(description = "补贴金额")
    private BigDecimal subsidyAmount;

    /**
     * 实际支付金额
     */
    @TableField("actual_amount")
    @Schema(description = "实际支付金额")
    private BigDecimal actualAmount;

    /**
     * 支付方式
     */
    @TableField("pay_method")
    @Schema(description = "支付方式")
    private String payMethod;

    /**
     * 支付时间
     */
    @TableField("pay_time")
    @Schema(description = "支付时间")
    private LocalDateTime payTime;

    /**
     * 消费时间
     */
    @TableField("consume_time")
    @Schema(description = "消费时间")
    private LocalDateTime consumeTime;

    /**
     * 消费类型
     */
    @TableField("consume_type")
    @Schema(description = "消费类型")
    private String consumeType;

    /**
     * 消费模式
     */
    @TableField("consume_mode")
    @Schema(description = "消费模式")
    private String consumeMode;

    /**
     * 消费模式配置JSON
     */
    @TableField("mode_config")
    @Schema(description = "消费模式配置JSON")
    private String modeConfig;

    /**
     * 商户名称
     */
    @TableField("merchant_name")
    @Schema(description = "商户名称")
    private String merchantName;

    /**
     * 商品信息JSON
     */
    @TableField("goods_info")
    @Schema(description = "商品信息JSON")
    private String goodsInfo;

    /**
     * 状态
     */
    @TableField("status")
    @Schema(description = "状态")
    private String status;

    /**
     * 退款状态
     */
    @TableField("refund_status")
    @Schema(description = "退款状态")
    private Integer refundStatus;

    /**
     * 退款金额
     */
    @TableField("refund_amount")
    @Schema(description = "退款金额")
    private BigDecimal refundAmount;

    /**
     * 退款时间
     */
    @TableField("refund_time")
    @Schema(description = "退款时间")
    private LocalDateTime refundTime;

    /**
     * 退款原因
     */
    @TableField("refund_reason")
    @Schema(description = "退款原因")
    private String refundReason;

    /**
     * 原始消费记录ID
     */
    @TableField("original_record_id")
    @Schema(description = "原始消费记录ID")
    private Long originalRecordId;

    /**
     * 第三方支付订单号
     */
    @TableField("third_party_order_no")
    @Schema(description = "第三方支付订单号")
    private String thirdPartyOrderNo;

    /**
     * 第三方交易号
     */
    @TableField("third_party_transaction_id")
    @Schema(description = "第三方交易号")
    private String thirdPartyTransactionId;

    /**
     * 手续费
     */
    @TableField("fee_amount")
    @Schema(description = "手续费")
    private BigDecimal feeAmount;

    /**
     * 扩展数据JSON
     */
    @TableField("extend_data")
    @Schema(description = "扩展数据JSON")
    private String extendData;

    /**
     * 客户端IP地址
     */
    @TableField("client_ip")
    @Schema(description = "客户端IP地址")
    private String clientIp;

    /**
     * 用户代理
     */
    @TableField("user_agent")
    @Schema(description = "用户代理")
    private String userAgent;

    /**
     * 备注
     */
    @TableField("remark")
    @Schema(description = "备注")
    private String remark;

    // 注意：createTime, updateTime, createUserId, updateUserId, deletedFlag, version
    // 已由BaseEntity提供，无需重复定义
}




