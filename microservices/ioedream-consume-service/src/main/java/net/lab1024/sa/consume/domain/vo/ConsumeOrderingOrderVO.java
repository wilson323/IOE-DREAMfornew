package net.lab1024.sa.consume.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.Builder;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 在线订餐订单视图对象
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-24
 */
@Data
@Builder
@Schema(description = "在线订餐订单视图对象")
public class ConsumeOrderingOrderVO {

    @Schema(description = "订单ID", example = "1001")
    private Long orderId;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "用户名", example = "张三")
    private String username;

    @Schema(description = "订餐日期", example = "2025-12-25")
    private LocalDate orderDate;

    @Schema(description = "餐别类型", example = "LUNCH")
    private String mealType;

    @Schema(description = "餐别类型名称", example = "午餐")
    private String mealTypeName;

    @Schema(description = "订单总金额", example = "25.50")
    private BigDecimal totalAmount;

    @Schema(description = "优惠金额", example = "5.00")
    private BigDecimal discountAmount;

    @Schema(description = "实付金额", example = "20.50")
    private BigDecimal actualAmount;

    @Schema(description = "订单状态", example = "PENDING")
    private String orderStatus;

    @Schema(description = "订单状态名称", example = "待核销")
    private String orderStatusName;

    @Schema(description = "备注", example = "不要辣")
    private String remark;

    @Schema(description = "下单时间", example = "2025-12-24T10:30:00")
    private LocalDateTime orderTime;

    @Schema(description = "支付时间", example = "2025-12-24T10:31:00")
    private LocalDateTime payTime;

    @Schema(description = "核销时间", example = "2025-12-25T12:30:00")
    private LocalDateTime verifyTime;

    @Schema(description = "取消时间", example = "2025-12-24T11:00:00")
    private LocalDateTime cancelTime;

    @Schema(description = "创建时间", example = "2025-12-24T10:30:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-12-24T10:31:00")
    private LocalDateTime updateTime;

    @Schema(description = "版本号", example = "1")
    private Integer version;
}
