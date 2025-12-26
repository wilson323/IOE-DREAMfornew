package net.lab1024.sa.common.entity.consume;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import net.lab1024.sa.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 菜品库存实体类
 *
 * @author IOE-DREAM
 * @since 2025-12-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_meal_inventory")
@Schema(description = "菜品库存实体")
public class MealInventoryEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "库存ID")
    private Long inventoryId;

    @Schema(description = "菜品ID", required = true)
    private Long menuId;

    @Schema(description = "库存日期", required = true)
    private LocalDate inventoryDate;

    @Schema(description = "餐别（1-早餐 2-午餐 3-晚餐）", required = true)
    private Integer mealType;

    @Schema(description = "初始数量")
    private Integer initialQuantity;

    @Schema(description = "已售数量")
    private Integer soldQuantity;

    @Schema(description = "剩余数量")
    private Integer remainingQuantity;

    @Schema(description = "状态（1-有效 0-无效）")
    private Integer status;
}
