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
     * 账户ID（订餐扣款账户）
     */
    @TableField("account_id")
    @Schema(description = "账户ID")
    private Long accountId;
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

    /**
     * 区域ID
     */
    @TableField("area_id")
    @Schema(description = "区域ID")
    private Long areaId;

    /**
     * 餐次类型ID
     */
    @TableField("meal_type_id")
    @Schema(description = "餐次类型ID")
    private Long mealTypeId;

    /**
     * 金额
     */
    @TableField("amount")
    @Schema(description = "金额")
    private java.math.BigDecimal amount;

    /**
     * 实际金额
     */
    @TableField("actual_amount")
    @Schema(description = "实际金额")
    private java.math.BigDecimal actualAmount;

    /**
     * 取餐开始时间
     */
    @TableField("pickup_start_time")
    @Schema(description = "取餐开始时间")
    private java.time.LocalDateTime pickupStartTime;

    /**
     * 取餐结束时间
     */
    @TableField("pickup_end_time")
    @Schema(description = "取餐结束时间")
    private java.time.LocalDateTime pickupEndTime;

    // 注意：createTime, updateTime, createUserId, updateUserId, deletedFlag, version
    // 已由BaseEntity提供，无需重复定义

    /**
     * 设置账户ID（兼容：long）
     *
     * @param accountId 账户ID
     */
    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    /**
     * 设置账户ID
     *
     * @param accountId 账户ID
     */
    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    /**
     * 获取账户ID（兼容）
     *
     * @return 账户ID
     */
    public Long getAccountId() {
        return this.accountId;
    }

    // 兼容性方法
    public void setDeleted(boolean deleted) {
        // 与BaseEntity的deletedFlag字段兼容
        this.setDeletedFlag(deleted ? 1 : 0);
    }

    /**
     * 设置订单状态（String兼容方法）
     * <p>
     * 兼容测试/旧代码：以字符串描述设置订单状态。
     * 映射规则（与MealOrderManager一致）：PENDING->1，PAID->2，COMPLETED->3，CANCELLED->4
     * </p>
     *
     * @param statusText 状态文本
     */
    public void setStatusText(String statusText) {
        if (statusText == null || statusText.trim().isEmpty()) {
            this.status = null;
            return;
        }
        String normalized = statusText.trim().toUpperCase();
        switch (normalized) {
            case "PENDING":
                this.status = 1;
                break;
            case "PAID":
                this.status = 2;
                break;
            case "COMPLETED":
                this.status = 3;
                break;
            case "CANCELLED":
                this.status = 4;
                break;
            default:
                // 无法识别的状态，置空避免误写
                this.status = null;
                break;
        }
    }

    /**
     * 获取取餐开始时间
     * <p>
     * Lombok @Data 应该自动生成，但为了确保编译通过，手动添加
     * </p>
     *
     * @return 取餐开始时间
     */
    public LocalDateTime getPickupStartTime() {
        return this.pickupStartTime;
    }

    /**
     * 设置取餐开始时间
     *
     * @param pickupStartTime 取餐开始时间
     */
    public void setPickupStartTime(LocalDateTime pickupStartTime) {
        this.pickupStartTime = pickupStartTime;
    }

    /**
     * 获取取餐结束时间
     * <p>
     * Lombok @Data 应该自动生成，但为了确保编译通过，手动添加
     * </p>
     *
     * @return 取餐结束时间
     */
    public LocalDateTime getPickupEndTime() {
        return this.pickupEndTime;
    }

    /**
     * 设置取餐结束时间
     *
     * @param pickupEndTime 取餐结束时间
     */
    public void setPickupEndTime(LocalDateTime pickupEndTime) {
        this.pickupEndTime = pickupEndTime;
    }

    // ==================== Lombok @Data 未生效时的手动 getter/setter ====================

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return this.orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getOrderNo() {
        return this.orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMealType() {
        return this.mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public BigDecimal getTotalAmount() {
        return this.totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Integer getStatus() {
        return this.status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getAreaId() {
        return this.areaId;
    }

    public void setAreaId(Long areaId) {
        this.areaId = areaId;
    }

    public Long getMealTypeId() {
        return this.mealTypeId;
    }

    public void setMealTypeId(Long mealTypeId) {
        this.mealTypeId = mealTypeId;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getActualAmount() {
        return this.actualAmount;
    }

    public void setActualAmount(BigDecimal actualAmount) {
        this.actualAmount = actualAmount;
    }
}
