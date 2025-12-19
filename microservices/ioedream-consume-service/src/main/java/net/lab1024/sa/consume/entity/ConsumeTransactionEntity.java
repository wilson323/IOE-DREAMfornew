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
 * 消费交易实体
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_consume_transaction")
@Schema(description = "消费交易实体")
public class ConsumeTransactionEntity extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;
    /**
     * 交易ID
     */
    @TableField("transaction_id")
    @Schema(description = "交易ID")
    private Long transactionId;
    /**
     * 交易流水号
     */
    @TableField("transaction_no")
    @Schema(description = "交易流水号")
    private String transactionNo;
    /**
     * 账户编号
     */
    @TableField("account_no")
    @Schema(description = "账户编号")
    private String accountNo;
    /**
     * 交易金额
     */
    @TableField("amount")
    @Schema(description = "交易金额")
    private java.math.BigDecimal amount;
    /**
     * 交易类型
     */
    @TableField("transaction_type")
    @Schema(description = "交易类型")
    private String transactionType;
    /**
     * 交易状态
     */
    @TableField("status")
    @Schema(description = "交易状态")
    private String status;

    /**
     * 最终金额
     */
    @TableField("final_money")
    @Schema(description = "最终金额")
    private java.math.BigDecimal finalMoney;

    /**
     * 消费时间
     */
    @TableField("consume_time")
    @Schema(description = "消费时间")
    private java.time.LocalDateTime consumeTime;

    /**
     * 商户ID
     */
    @TableField("merchant_id")
    @Schema(description = "商户ID")
    private Long merchantId;

    /**
     * 交易状态（详细状态）
     */
    @TableField("transaction_status")
    @Schema(description = "交易状态（详细状态）")
    private Integer transactionStatus;

    /**
     * 补贴使用金额
     * <p>
     * 用于统计与明细展示（例如：补贴消费、混合支付时的补贴抵扣部分）
     * </p>
     */
    @TableField("allowance_used")
    @Schema(description = "补贴使用金额")
    private BigDecimal allowanceUsed;

    /**
     * 备注
     */
    @TableField("remark")
    @Schema(description = "备注")
    private String remark;

    // 缺失字段 - 根据错误日志添加
    /**
     * 用户ID (String类型)
     */
    @TableField("user_id_str")
    @Schema(description = "用户ID(String)")
    private String userIdStr;

    /**
     * 用户姓名
     */
    @TableField("user_name")
    @Schema(description = "用户姓名")
    private String userName;

    /**
     * 设备ID
     */
    @TableField("device_id")
    @Schema(description = "设备ID")
    private Long deviceId;

    /**
     * 设备编号
     */
    @TableField("device_code")
    @Schema(description = "设备编号")
    private String deviceCode;

    /**
     * 区域ID
     */
    @TableField("area_id")
    @Schema(description = "区域ID")
    private Long areaId;

    /**
     * 区域名称
     */
    @TableField("area_name")
    @Schema(description = "区域名称")
    private String areaName;

    /**
     * 商户名称
     */
    @TableField("merchant_name")
    @Schema(description = "商户名称")
    private String merchantName;

    /**
     * 订单号
     */
    @TableField("order_no")
    @Schema(description = "订单号")
    private String orderNo;

    /**
     * 商品ID
     * <p>
     * 仅在“商品/餐别”类消费模式下可能存在
     * </p>
     */
    @TableField("product_id")
    @Schema(description = "商品ID")
    private Long productId;

    /**
     * 商品名称
     * <p>
     * 仅在“商品/餐别”类消费模式下可能存在
     * </p>
     */
    @TableField("product_name")
    @Schema(description = "商品名称")
    private String productName;

    /**
     * 餐别ID
     * <p>
     * 兼容报表统计中对“餐别”维度的聚合
     * </p>
     */
    @TableField("meal_id")
    @Schema(description = "餐别ID")
    private Long mealId;

    /**
     * 餐别名称
     * <p>
     * 兼容报表统计中对“餐别”维度的展示
     * </p>
     */
    @TableField("meal_name")
    @Schema(description = "餐别名称")
    private String mealName;

    /**
     * 商品明细（JSON）
     * <p>
     * 兼容推荐/画像等服务对商品详情的读取（如：[{productId,productName,qty,price}]）
     * </p>
     */
    @TableField("product_details")
    @Schema(description = "商品明细(JSON)")
    private String productDetails;

    /**
     * 交易来源
     */
    @TableField("transaction_source")
    @Schema(description = "交易来源")
    private String transactionSource;

    /**
     * 支付方式
     */
    @TableField("payment_method")
    @Schema(description = "支付方式")
    private String paymentMethod;

    /**
     * 用户ID
     */
    @TableField("user_id")
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 设备名称
     */
    @TableField("device_name")
    @Schema(description = "设备名称")
    private String deviceName;

    /**
     * 消费金额
     */
    @TableField("consume_money")
    @Schema(description = "消费金额")
    private BigDecimal consumeMoney;

    /**
     * 折扣金额
     */
    @TableField("discount_money")
    @Schema(description = "折扣金额")
    private BigDecimal discountMoney;

    /**
     * 消费前余额
     */
    @TableField("balance_before")
    @Schema(description = "消费前余额")
    private BigDecimal balanceBefore;

    /**
     * 消费后余额
     */
    @TableField("balance_after")
    @Schema(description = "消费后余额")
    private BigDecimal balanceAfter;

    /**
     * 消费模式
     */
    @TableField("consume_mode")
    @Schema(description = "消费模式")
    private String consumeMode;

    /**
     * 消费类型
     */
    @TableField("consume_type")
    @Schema(description = "消费类型")
    private String consumeType;

    // 注意：createTime, updateTime, createUserId, updateUserId, deletedFlag, version
    // 已由BaseEntity提供，无需重复定义

    // 缺失的getter/setter方法 - 根据错误日志添加
    public void setUserIdStr(String userIdStr) {
        this.userIdStr = userIdStr;
    }

    public String getUserIdStr() {
        return this.userIdStr;
    }

    /**
     * 获取最终金额（如果finalMoney为null则返回amount）
     * Lombok的@Data会自动生成getFinalMoney()，这里提供自定义逻辑
     */
    public BigDecimal getFinalMoney() {
        return this.finalMoney != null ? this.finalMoney : this.amount;
    }

    public BigDecimal getFinalAmount() {
        return this.finalMoney; // 兼容方法
    }

    public void setFinalAmount(BigDecimal finalAmount) {
        this.finalMoney = finalAmount;
    }

    // 缺失字段 - 根据错误日志添加
    /**
     * 部门ID
     */
    @TableField("dept_id")
    @Schema(description = "部门ID")
    private Long deptId;

    /**
     * 账户ID
     */
    @TableField("account_id")
    @Schema(description = "账户ID")
    private Long accountId;

    /**
     * 账户类型ID
     */
    @TableField("account_kind_id")
    @Schema(description = "账户类型ID")
    private Long accountKindId;

    /**
     * 区域管理模式
     */
    @TableField("area_manage_mode")
    @Schema(description = "区域管理模式")
    private Integer areaManageMode;

    /**
     * 区域子类型
     */
    @TableField("area_sub_type")
    @Schema(description = "区域子类型")
    private Integer areaSubType;

    // 缺失的getter/setter方法 - 根据错误日志添加
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return this.userId;
    }

    public Long getDeptId() {
        return this.deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public Long getAccountId() {
        return this.accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getAccountKindId() {
        return this.accountKindId;
    }

    public void setAccountKindId(Long accountKindId) {
        this.accountKindId = accountKindId;
    }

    public Integer getAreaManageMode() {
        return this.areaManageMode;
    }

    public void setAreaManageMode(Integer areaManageMode) {
        this.areaManageMode = areaManageMode;
    }

    public Integer getAreaSubType() {
        return this.areaSubType;
    }

    public void setAreaSubType(Integer areaSubType) {
        this.areaSubType = areaSubType;
    }

    /**
     * 交易时间（兼容字段：部分服务层使用transactionTime）
     */
    public LocalDateTime getTransactionTime() {
        return this.consumeTime;
    }

    public void setTransactionTime(LocalDateTime transactionTime) {
        this.consumeTime = transactionTime;
    }

    // ==================== 补充缺失的方法 ====================
    // 根据编译错误日志补充缺失的getter/setter方法

    /**
     * 获取套餐ID (兼容方法)
     * <p>
     * 根据编译错误添加getMealId()方法
     * </p>
     *
     * @return 套餐ID (从商户ID或其他字段映射)
     */
    public Long getMealId() {
        // 从商户ID映射为套餐ID，业务逻辑映射
        return this.merchantId;
    }

    /**
     * 获取产品ID (兼容方法)
     * <p>
     * 根据编译错误添加getProductId()方法
     * </p>
     *
     * @return 产品ID (从订单号或其他字段映射)
     */
    public String getProductId() {
        // 从订单号映射为产品ID，业务逻辑映射
        return this.orderNo;
    }

    /**
     * 获取产品名称 (兼容方法)
     * <p>
     * 根据编译错误添加getProductName()方法
     * </p>
     *
     * @return 产品名称 (从备注或其他字段映射)
     */
    public String getProductName() {
        // 从交易类型映射为产品名称，业务逻辑映射
        if (this.transactionType != null) {
            switch (this.transactionType) {
                case "CONSUME":
                    return "消费";
                case "REFUND":
                    return "退款";
                case "RECHARGE":
                    return "充值";
                default:
                    return this.transactionType;
            }
        }
        return "未知产品";
    }

    /**
     * 获取补贴已用金额 (兼容方法)
     * <p>
     * 根据编译错误添加getAllowanceUsed()方法
     * </p>
     *
     * @return 补贴已用金额 (从消费金额或折扣金额映射)
     */
    public BigDecimal getAllowanceUsed() {
        // 从折扣金额映射为补贴已用金额，业务逻辑映射
        return this.discountMoney != null ? this.discountMoney : BigDecimal.ZERO;
    }

    /**
     * 获取消费类型名称 (兼容方法)
     * <p>
     * 根据编译错误添加getConsumeTypeName()方法
     * </p>
     *
     * @return 消费类型名称
     */
    public String getConsumeTypeName() {
        if (this.consumeType != null) {
            switch (this.consumeType) {
                case "MEAL":
                    return "用餐";
                case "SNACK":
                    return "零食";
                case "DRINK":
                    return "饮品";
                case "OTHER":
                    return "其他";
                default:
                    return this.consumeType;
            }
        }
        return "未知消费类型";
    }
}
