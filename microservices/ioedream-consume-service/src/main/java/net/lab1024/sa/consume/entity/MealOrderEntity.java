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
 * 用餐订单实体
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_meal_order")
@Schema(description = "用餐订单实体")
public class MealOrderEntity extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;
    /**
     * 订单ID
     */
    @TableField("order_id")
    @Schema(description = "订单ID")
    private Long orderId;
    /**
     * 订单编号
     */
    @TableField("order_no")
    @Schema(description = "订单编号")
    private String orderNo;
    /**
     * 用户ID
     */
    @TableField("user_id")
    @Schema(description = "用户ID")
    private Long userId;
    /**
     * 用户名
     */
    @TableField("user_name")
    @Schema(description = "用户名")
    private String userName;
    /**
     * 餐次类型
     */
    @TableField("meal_type")
    @Schema(description = "餐次类型")
    private String mealType;
    /**
     * 总金额
     */
    @TableField("total_amount")
    @Schema(description = "总金额")
    private java.math.BigDecimal totalAmount;
    /**
     * 订单状态
     */
    @TableField("status")
    @Schema(description = "订单状态")
    private Integer status;
    // 注意：createTime, updateTime, createUserId, updateUserId, deletedFlag, version
    // 已由BaseEntity提供，无需重复定义
}
