package net.lab1024.sa.consume.entity;

import java.math.BigDecimal;

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

    /**
     * 价格（兼容性方法）
     */
    @TableField("price")
    @Schema(description = "价格")
    private java.math.BigDecimal price;

    /**
     * 菜品ID（测试/旧代码兼容字段，不落库）
     */
    @TableField(exist = false)
    @Schema(description = "菜品ID(兼容)")
    private Long dishId;

    /**
     * 菜品名称（测试/旧代码兼容字段，不落库）
     */
    @TableField(exist = false)
    @Schema(description = "菜品名称(兼容)")
    private String dishName;

    // 注意：createTime, updateTime, createUserId, updateUserId, deletedFlag, version
    // 已由BaseEntity提供，无需重复定义

    // 兼容性方法
    public java.math.BigDecimal getPrice() {
        return this.price != null ? this.price : this.unitPrice;
    }

    public void setPrice(java.math.BigDecimal price) {
        this.price = price;
        // 同时更新单价以保持一致性
        this.unitPrice = price;
    }

    /**
     * 获取小计（兼容性方法）
     * <p>
     * 根据编译错误修复需求添加
     * </p>
     *
     * @return 小计金额
     */
    public java.math.BigDecimal getSubtotal() {
        return this.totalPrice;
    }

    /**
     * 设置小计（兼容性方法）
     * <p>
     * 根据编译错误修复需求添加
     * </p>
     *
     * @param subtotal 小计金额
     */
    public void setSubtotal(java.math.BigDecimal subtotal) {
        this.totalPrice = subtotal;
    }

    /**
     * 设置菜品ID（兼容：映射到 itemId）
     *
     * @param dishId 菜品ID
     */
    public void setDishId(long dishId) {
        this.dishId = dishId;
        this.itemId = dishId;
    }

    // ==================== Lombok @Data 未生效时的手动 getter/setter ====================

    /**
     * 设置菜品ID（兼容性方法）
     * <p>
     * 兼容测试/旧代码：dishId 映射到 itemId。
     * </p>
     *
     * @param dishId 菜品ID
     */
    public void setDishId(Long dishId) {
        this.itemId = dishId;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getItemId() {
        return this.itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getOrderId() {
        return this.orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getProductName() {
        return this.productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantity() {
        return this.quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return this.unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotalPrice() {
        return this.totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Long getDishId() {
        return this.dishId;
    }

    public String getDishName() {
        return this.dishName;
    }
}
