package net.lab1024.sa.admin.module.consume.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 消费记录VO
 * <p>
 * 严格遵循repowiki规范：
 * - 使用@Data注解自动生成getter/setter
 * - 使用@Accessors启用链式调用
 * - 提供完整的展示字段
 * - 包含格式化后的显示信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-27
 */
@Data
@Accessors(chain = true)
public class ConsumeRecordVO {

    /**
     * 消费记录ID
     */
    private Long recordId;

    /**
     * 消费编号
     */
    private String consumeNo;

    /**
     * 账户ID
     */
    private Long accountId;

    /**
     * 账户编号
     */
    private String accountNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户姓名
     */
    private String userName;

    /**
     * 用户卡号
     */
    private String userCardNo;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备编号
     */
    private String deviceCode;

    /**
     * 消费模式代码
     */
    private String consumeModeCode;

    /**
     * 消费模式名称
     */
    private String consumeModeName;

    /**
     * 交易类型代码
     */
    private String transactionTypeCode;

    /**
     * 交易类型名称
     */
    private String transactionTypeName;

    /**
     * 支付方式代码
     */
    private String paymentMethodCode;

    /**
     * 支付方式名称
     */
    private String paymentMethodName;

    /**
     * 消费金额
     */
    private BigDecimal consumeAmount;

    /**
     * 折扣金额
     */
    private BigDecimal discountAmount;

    /**
     * 实付金额
     */
    private BigDecimal actualAmount;

    /**
     * 原余额
     */
    private BigDecimal originalBalance;

    /**
     * 消费后余额
     */
    private BigDecimal currentBalance;

    /**
     * 积分使用数量
     */
    private Integer pointsUsed;

    /**
     * 积分获得数量
     */
    private Integer pointsEarned;

    /**
     * 优惠券ID
     */
    private Long couponId;

    /**
     * 优惠券编号
     */
    private String couponNo;

    /**
     * 优惠券名称
     */
    private String couponName;

    /**
     * 优惠金额
     */
    private BigDecimal couponAmount;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品编号
     */
    private String productCode;

    /**
     * 商品数量
     */
    private Integer productQuantity;

    /**
     * 商品单价
     */
    private BigDecimal unitPrice;

    /**
     * 商品小计
     */
    private BigDecimal subtotalAmount;

    /**
     * 手续费
     */
    private BigDecimal serviceFee;

    /**
     * 消费地点
     */
    private String consumeLocation;

    /**
     * 消费地址
     */
    private String consumeAddress;

    /**
     * 消费备注
     */
    private String consumeRemarks;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 外部交易号
     */
    private String externalTransactionNo;

    /**
     * 消费时间
     */
    private LocalDateTime consumeTime;

    /**
     * 结算时间
     */
    private LocalDateTime settleTime;

    /**
     * 退款状态代码
     */
    private String refundStatusCode;

    /**
     * 退款状态名称
     */
    private String refundStatusName;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 退款时间
     */
    private LocalDateTime refundTime;

    /**
     * 退款原因
     */
    private String refundReason;

    /**
     * 交易状态代码
     */
    private String statusCode;

    /**
     * 交易状态名称
     */
    private String statusName;

    /**
     * 异常标志
     */
    private Boolean isAbnormal;

    /**
     * 异常描述
     */
    private String abnormalDescription;

    /**
     * 处理状态
     */
    private Integer processStatus;

    /**
     * 处理备注
     */
    private String processRemarks;

    /**
     * 操作员ID
     */
    private Long operatorId;

    /**
     * 操作员姓名
     */
    private String operatorName;

    /**
     * 审核状态
     */
    private Integer auditStatus;

    /**
     * 审核人ID
     */
    private Long auditorId;

    /**
     * 审核人姓名
     */
    private String auditorName;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 审核备注
     */
    private String auditRemarks;

    /**
     * 数据来源
     */
    private String dataSource;

    /**
     * 同步状态
     */
    private String syncStatus;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 删除标识（0-未删除，1-已删除）
     */
    private Integer deletedFlag;
}