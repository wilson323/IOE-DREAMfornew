package net.lab1024.sa.consume.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 用餐订单项实体
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_meal_order_item")
@Schema(description = "用餐订单项实体")
public class MealOrderItemEntity extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;
    /**
     * 项目ID
     */
    @TableField("item_id")
    @Schema(description = "项目ID")
    private Long itemId;
    /**
     * 订单ID
     */
    @TableField("order_id")
    @Schema(description = "订单ID")
    private Long orderId;
    /**
     * 产品名称
     */
    @TableField("product_name")
    @Schema(description = "产品名称")
    private String productName;
    /**
     * 数量
     */
    @TableField("quantity")
    @Schema(description = "数量")
    private Integer quantity;
    /**
     * 单价
     */
    @TableField("unit_price")
    @Schema(description = "单价")
    private java.math.BigDecimal unitPrice;
    /**
     * 小计
     */
    @TableField("total_price")
    @Schema(description = "小计")
    private java.math.BigDecimal totalPrice;
    // 注意：createTime, updateTime, createUserId, updateUserId, deletedFlag, version
    // 已由BaseEntity提供，无需重复定义
}
