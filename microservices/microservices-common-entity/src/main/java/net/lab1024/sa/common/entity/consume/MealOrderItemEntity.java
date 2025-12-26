package net.lab1024.sa.common.entity.consume;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import net.lab1024.sa.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 订单明细实体类
 *
 * @author IOE-DREAM
 * @since 2025-12-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_meal_order_item")
@Schema(description = "订单明细实体")
public class MealOrderItemEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "明细ID")
    private Long itemId;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "菜品ID")
    private Long menuId;

    @Schema(description = "菜品名称")
    private String menuName;

    @Schema(description = "菜品编码")
    private String menuCode;

    @Schema(description = "菜品图片URL")
    private String menuImage;

    @Schema(description = "单价（元）")
    private BigDecimal unitPrice;

    @Schema(description = "数量")
    private Integer quantity;

    @Schema(description = "小计（元）")
    private BigDecimal subtotal;

    @Schema(description = "特殊要求")
    private String specialRequirements;
}
