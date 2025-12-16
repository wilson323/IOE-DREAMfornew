package net.lab1024.sa.consume.domain.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订餐订单详情VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MealOrderDetailVO extends MealOrderVO {

    /**
     * 账户ID
     */
    private Long accountId;

    /**
     * 取餐设备ID
     */
    private Long pickupDeviceId;

    /**
     * 取餐设备名称
     */
    private String pickupDeviceName;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 取消时间
     */
    private LocalDateTime cancelTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 订单明细列表
     */
    private List<MealOrderItemVO> items;

    /**
     * 订单明细VO
     */
    @Data
    public static class MealOrderItemVO {
        /**
         * 明细ID
         */
        private Long id;

        /**
         * 菜品ID
         */
        private Long dishId;

        /**
         * 菜品名称
         */
        private String dishName;

        /**
         * 菜品单价
         */
        private BigDecimal price;

        /**
         * 数量
         */
        private Integer quantity;

        /**
         * 小计金额
         */
        private BigDecimal subtotal;
    }
}
