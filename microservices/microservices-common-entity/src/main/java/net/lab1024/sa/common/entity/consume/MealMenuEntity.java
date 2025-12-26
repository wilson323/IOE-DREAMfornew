package net.lab1024.sa.common.entity.consume;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * 菜品实体类
 *
 * @author IOE-DREAM
 * @since 2025-12-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_meal_menu")
@Schema(description = "菜品实体")
public class MealMenuEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "菜品ID")
    private Long menuId;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "菜品名称", required = true, example = "红烧肉")
    private String menuName;

    @Schema(description = "菜品编码", required = true)
    private String menuCode;

    @Schema(description = "菜品图片URL")
    private String menuImage;

    @Schema(description = "价格（元）", required = true, example = "25.00")
    private BigDecimal price;

    @Schema(description = "原价（元）")
    private BigDecimal originalPrice;

    @Schema(description = "单位（份/个/碗）")
    private String unit;

    @Schema(description = "菜品描述")
    private String description;

    @Schema(description = "食材清单（JSON格式）")
    private String ingredients;

    @Schema(description = "营养信息（JSON格式）")
    private String nutritionInfo;

    @Schema(description = "辣度（0-不辣 1-微辣 2-中辣 3-重辣）")
    private Integer spicyLevel;

    @Schema(description = "供应日期（1,2,3,4,5代表周一到周五）")
    private String availableDays;

    @Schema(description = "供应开始时间")
    private LocalTime availableStartTime;

    @Schema(description = "供应结束时间")
    private LocalTime availableEndTime;

    @Schema(description = "每日最大供应数量")
    private Integer maxDailyQuantity;

    @Schema(description = "当前剩余数量")
    private Integer currentQuantity;

    @Schema(description = "状态（1-上架 0-下架）")
    private Integer status;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "备注")
    private String remark;
}
