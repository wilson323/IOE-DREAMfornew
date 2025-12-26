package net.lab1024.sa.consume.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.Builder;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 菜品视图对象
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-24
 */
@Data
@Builder
@Schema(description = "菜品视图对象")
public class ConsumeDishVO {

    @Schema(description = "菜品ID", example = "1001")
    private Long dishId;

    @Schema(description = "菜品名称", example = "宫保鸡丁")
    private String dishName;

    @Schema(description = "菜品分类", example = "热菜")
    private String category;

    @Schema(description = "菜品分类ID", example = "1")
    private Long categoryId;

    @Schema(description = "价格", example = "25.00")
    private BigDecimal price;

    @Schema(description = "原价", example = "30.00")
    private BigDecimal originalPrice;

    @Schema(description = "菜品图片URL", example = "https://example.com/dish.jpg")
    private String imageUrl;

    @Schema(description = "菜品描述", example = "经典川菜，麻辣鲜香")
    private String description;

    @Schema(description = "辣度等级", example = "2", allowableValues = {"0", "1", "2", "3"})
    private Integer spicyLevel;

    @Schema(description = "辣度名称", example = "中辣")
    private String spicyLevelName;

    @Schema(description = "可用库存", example = "50")
    private Integer availableQuantity;

    @Schema(description = "已售数量", example = "30")
    private Integer soldQuantity;

    @Schema(description = "菜品标签", example = "[\"热门\", \"推荐\", \"辣\"]")
    private String tags;

    @Schema(description = "营养信息", example = "{\"calories\": 350, \"protein\": 25}")
    private String nutritionInfo;

    @Schema(description = "适用餐别", example = "[\"BREAKFAST\", \"LUNCH\", \"DINNER\"]")
    private String applicableMeals;

    @Schema(description = "上架状态", example = "1", allowableValues = {"0", "1"})
    private Integer onShelfStatus;

    @Schema(description = "上架状态名称", example = "已上架")
    private String onShelfStatusName;

    @Schema(description = "商家ID", example = "101")
    private Long merchantId;

    @Schema(description = "商家名称", example = "中区餐厅")
    private String merchantName;

    @Schema(description = "创建时间", example = "2025-01-01T09:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-12-24T10:30:00")
    private LocalDateTime updateTime;

    @Schema(description = "版本号", example = "1")
    private Integer version;
}
