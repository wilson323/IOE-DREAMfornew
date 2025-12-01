package net.lab1024.sa.admin.module.consume.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单VO
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
public class OrderVO {

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 外部订单号
     */
    private String externalOrderNo;

    /**
     * 订单类型代码
     */
    private String orderTypeCode;

    /**
     * 订单类型名称
     */
    private String orderTypeName;

    /**
     * 订单优先级代码
     */
    private String priorityCode;

    /**
     * 订单优先级名称
     */
    private String priorityName;

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
     * 用户手机号
     */
    private String userPhone;

    /**
     * 用户邮箱
     */
    private String userEmail;

    /**
     * 下单人ID
     */
    private Long ordererId;

    /**
     * 下单人姓名
     */
    private String ordererName;

    /**
     * 下单人手机号
     */
    private String ordererPhone;

    /**
     * 收货人姓名
     */
    private String consigneeName;

    /**
     * 收货人手机号
     */
    private String consigneePhone;

    /**
     * 收货地址
     */
    private String deliveryAddress;

    /**
     * 收货地址详情
     */
    private String deliveryAddressDetail;

    /**
     * 收货省份
     */
    private String province;

    /**
     * 收货城市
     */
    private String city;

    /**
     * 收货区县
     */
    private String district;

    /**
     * 邮政编码
     */
    private String postalCode;

    /**
     * 配送方式代码
     */
    private String deliveryMethodCode;

    /**
     * 配送方式名称
     */
    private String deliveryMethodName;

    /**
     * 支付方式代码
     */
    private String paymentMethodCode;

    /**
     * 支付方式名称
     */
    private String paymentMethodName;

    /**
     * 发票类型代码
     */
    private String invoiceTypeCode;

    /**
     * 发票类型名称
     */
    private String invoiceTypeName;

    /**
     * 发票抬头
     */
    private String invoiceTitle;

    /**
     * 纳税人识别号
     */
    private String taxNumber;

    /**
     * 订单商品金额
     */
    private BigDecimal productAmount;

    /**
     * 运费金额
     */
    private BigDecimal freightAmount;

    /**
     * 优惠金额
     */
    private BigDecimal discountAmount;

    /**
     * 积分抵扣金额
     */
    private BigDecimal pointsDeductionAmount;

    /**
     * 优惠券抵扣金额
     */
    private BigDecimal couponDeductionAmount;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 实付金额
     */
    private BigDecimal paidAmount;

    /**
     * 未付金额
     */
    private BigDecimal unpaidAmount;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 订单状态代码
     */
    private String statusCode;

    /**
     * 订单状态名称
     */
    private String statusName;

    /**
     * 物流状态代码
     */
    private String logisticsStatusCode;

    /**
     * 物流状态名称
     */
    private String logisticsStatusName;

    /**
     * 下单时间
     */
    private LocalDateTime orderTime;

    /**
     * 支付时间
     */
    private LocalDateTime paymentTime;

    /**
     * 确认时间
     */
    private LocalDateTime confirmTime;

    /**
     * 发货时间
     */
    private LocalDateTime shipTime;

    /**
     * 送达时间
     */
    private LocalDateTime deliveryTime;

    /**
     * 完成时间
     */
    private LocalDateTime completeTime;

    /**
     * 取消时间
     */
    private LocalDateTime cancelTime;

    /**
     * 预计送达时间
     */
    private LocalDateTime estimatedDeliveryTime;

    /**
     * 订单商品列表
     */
    private List<OrderItemVO> orderItems;

    /**
     * 订单备注
     */
    private String remarks;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 退款原因
     */
    private String refundReason;

    /**
     * 物流单号
     */
    private String trackingNumber;

    /**
     * 物流公司
     */
    private String logisticsCompany;

    /**
     * 配送员
     */
    private String deliveryMan;

    /**
     * 配送员电话
     */
    private String deliveryManPhone;

    /**
     * 评价状态
     */
    private Integer evaluationStatus;

    /**
     * 评价时间
     */
    private LocalDateTime evaluationTime;

    /**
     * 评分
     */
    private Integer rating;

    /**
     * 评价内容
     */
    private String evaluationContent;

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
     * 创建人ID
     */
    private Long createUserId;

    /**
     * 创建人姓名
     */
    private String createUserName;

    /**
     * 更新人ID
     */
    private Long updateUserId;

    /**
     * 更新人姓名
     */
    private String updateUserName;

    /**
     * 删除标识（0-未删除，1-已删除）
     */
    private Integer deletedFlag;

    /**
     * 订单商品项VO
     */
    @Data
    @Accessors(chain = true)
    public static class OrderItemVO {

        /**
         * 订单项ID
         */
        private Long orderItemId;

        /**
         * 商品ID
         */
        private Long productId;

        /**
         * 商品编号
         */
        private String productCode;

        /**
         * 商品名称
         */
        private String productName;

        /**
         * 商品图片
         */
        private String productImage;

        /**
         * 商品规格
         */
        private String specification;

        /**
         * 商品单位
         */
        private String unit;

        /**
         * 商品数量
         */
        private Integer quantity;

        /**
         * 商品单价
         */
        private BigDecimal unitPrice;

        /**
         * 商品小计
         */
        private BigDecimal subtotal;

        /**
         * 折扣金额
         */
        private BigDecimal discountAmount;

        /**
         * 实付金额
         */
        private BigDecimal actualAmount;
    }
}