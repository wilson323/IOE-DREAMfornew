package net.lab1024.sa.admin.module.consume.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import net.lab1024.sa.base.common.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订餐实体
 * <p>
 * 严格遵循repowiki规范:
 * - 继承BaseEntity，包含审计字段
 * - 使用@TableName指定表名
 * - 使用@TableId标记主键
 * - 使用@Data注解自动生成getter/setter
 * - 使用@Accessors启用链式调用
 * - 包含完整的订餐业务字段
 * </p>
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("t_ordering")
public class OrderingEntity extends BaseEntity {

    /**
     * 订餐ID
     */
    @TableId
    private Long orderingId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 账户ID
     */
    private Long accountId;

    /**
     * 产品ID
     */
    private Long productId;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 产品价格
     */
    private BigDecimal productPrice;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 总金额
     */
    private BigDecimal totalAmount;

    /**
     * 订餐日期
     */
    private LocalDateTime orderingDate;

    /**
     * 预计取餐时间
     */
    private LocalDateTime pickupTime;

    /**
     * 实际取餐时间
     */
    private LocalDateTime actualPickupTime;

    /**
     * 订单状态：1-待支付 2-已支付 3-制作中 4-已完成 5-已取消 6-已退款
     */
    private Integer status;

    /**
     * 支付状态：1-未支付 2-已支付 3-已退款
     */
    private Integer paymentStatus;

    /**
     * 取餐地点
     */
    private String pickupLocation;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 备注
     */
    private String remark;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 取消时间
     */
    private LocalDateTime cancelTime;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 退款时间
     */
    private LocalDateTime refundTime;

    /**
     * 桌号
     */
    private String tableNo;

    /**
     * 折扣金额
     */
    private BigDecimal discountAmount;

    /**
     * 最终金额
     */
    private BigDecimal finalAmount;

    /**
     * 订单类型：DINE_IN-堂食，TAKEAWAY-外带，DELIVERY-外送
     */
    private String orderType;

    /**
     * 订单状态（字符串）：PENDING-待处理，PROCESSING-制作中，COMPLETED-已完成，CANCELLED-已取消
     */
    private String statusText;
}