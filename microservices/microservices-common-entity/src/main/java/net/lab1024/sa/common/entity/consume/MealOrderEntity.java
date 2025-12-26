package net.lab1024.sa.common.entity.consume;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import net.lab1024.sa.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 订餐订单实体类
 *
 * @author IOE-DREAM
 * @since 2025-12-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_meal_order")
@Schema(description = "订餐订单实体")
public class MealOrderEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "订单号", required = true)
    private String orderNo;

    @Schema(description = "用户ID", required = true)
    private Long userId;

    @Schema(description = "用户姓名")
    private String userName;

    @Schema(description = "用户手机号")
    private String userPhone;

    @Schema(description = "订餐日期", required = true)
    private java.time.LocalDate orderDate;

    @Schema(description = "餐别（1-早餐 2-午餐 3-晚餐）", required = true)
    private Integer mealType;

    @Schema(description = "订单总额（元）", required = true)
    private java.math.BigDecimal totalAmount;

    @Schema(description = "优惠金额（元）")
    private java.math.BigDecimal discountAmount;

    @Schema(description = "实付金额（元）", required = true)
    private java.math.BigDecimal actualAmount;

    @Schema(description = "补贴金额（元）")
    private java.math.BigDecimal subsidyAmount;

    @Schema(description = "订单状态（1-待支付 2-已支付 3-已完成 4-已取消 5-已退款）")
    private Integer orderStatus;

    @Schema(description = "支付状态（0-未支付 1-已支付 2-支付失败）")
    private Integer paymentStatus;

    @Schema(description = "支付时间")
    private java.time.LocalDateTime paymentTime;

    @Schema(description = "支付方式（balance-余额 wechat-微信 alipay-支付宝）")
    private String paymentMethod;

    @Schema(description = "取餐时间")
    private java.time.LocalTime pickupTime;

    @Schema(description = "取餐地点")
    private String pickupLocation;

    @Schema(description = "特殊要求")
    private String specialRequirements;

    @Schema(description = "取消原因")
    private String cancelReason;

    @Schema(description = "取消时间")
    private java.time.LocalDateTime cancelTime;

    @Schema(description = "退款金额")
    private java.math.BigDecimal refundAmount;

    @Schema(description = "退款时间")
    private java.time.LocalDateTime refundTime;

    @Schema(description = "备注")
    private String remark;
}
