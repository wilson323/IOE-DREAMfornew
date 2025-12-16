package net.lab1024.sa.consume.domain.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订餐创建表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@Data
public class MealOrderCreateForm {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 账户ID
     */
    @NotNull(message = "账户ID不能为空")
    private Long accountId;

    /**
     * 区域ID（食堂）
     */
    @NotNull(message = "区域ID不能为空")
    private Long areaId;

    /**
     * 餐别ID
     */
    @NotNull(message = "餐别ID不能为空")
    private Long mealTypeId;

    /**
     * 餐别名称
     */
    private String mealTypeName;

    /**
     * 预约日期
     */
    @NotNull(message = "预约日期不能为空")
    private LocalDateTime orderDate;

    /**
     * 取餐开始时间
     */
    private LocalDateTime pickupStartTime;

    /**
     * 取餐结束时间
     */
    private LocalDateTime pickupEndTime;

    /**
     * 订单明细
     */
    private List<MealOrderItemForm> items;

    /**
     * 备注
     */
    private String remark;

    /**
     * 订单明细表单
     */
    @Data
    public static class MealOrderItemForm {
        /**
         * 菜品ID
         */
        @NotNull(message = "菜品ID不能为空")
        private Long dishId;

        /**
         * 菜品名称
         */
        private String dishName;

        /**
         * 菜品单价
         */
        @NotNull(message = "菜品单价不能为空")
        private BigDecimal price;

        /**
         * 数量
         */
        @NotNull(message = "数量不能为空")
        private Integer quantity;
    }
}
