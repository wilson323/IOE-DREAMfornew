package net.lab1024.sa.common.entity.consume;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Builder;

import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 消费交易实体类
 * <p>
 * 记录消费交易详情，支持在线/离线消费和退款管理
 * 提供交易流水、支付状态跟踪和同步处理等功能
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 * <p>
 * 业务场景：
 * - 在线消费交易
 * - 离线消费交易
 * - 退款交易处理
 * - 交易统计分析
 * - 对账记录生成
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@TableName("consume_transaction")
@Schema(description = "消费交易实体")
public class ConsumeTransactionEntity extends BaseEntity {

    /**
     * 交易ID（主键）
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "交易ID")
    private Long transactionId;

    /**
     * 交易流水号（业务唯一）
     */
    @Schema(description = "交易流水号")
    private String transactionNo;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 用户姓名
     */
    @Schema(description = "用户姓名")
    private String userName;

    /**
     * 账户ID
     */
    @Schema(description = "账户ID")
    private Long accountId;

    /**
     * 账户编号
     */
    @Schema(description = "账户编号")
    private String accountNo;

    /**
     * 交易类型（CONSUME-消费 REFUND-退款 RECHARGE-充值 DEDUCT-扣款）
     */
    @Schema(description = "交易类型")
    private String transactionType;

    /**
     * 交易金额
     */
    @Schema(description = "交易金额")
    private BigDecimal transactionAmount;

    /**
     * 实际支付金额（扣除折扣后）
     */
    @Schema(description = "实际支付金额")
    private BigDecimal actualAmount;

    /**
     * 折扣金额
     */
    @Schema(description = "折扣金额")
    private BigDecimal discountAmount;

    /**
     * 交易前余额
     */
    @Schema(description = "交易前余额")
    private BigDecimal balanceBefore;

    /**
     * 交易后余额
     */
    @Schema(description = "交易后余额")
    private BigDecimal balanceAfter;

    /**
     * 设备ID
     */
    @Schema(description = "设备ID")
    private String deviceId;

    /**
     * 设备名称
     */
    @Schema(description = "设备名称")
    private String deviceName;

    /**
     * 区域ID
     */
    @Schema(description = "区域ID")
    private Long areaId;

    /**
     * 区域名称
     */
    @Schema(description = "区域名称")
    private String areaName;

    /**
     * 餐次ID
     */
    @Schema(description = "餐次ID")
    private Long mealId;

    /**
     * 餐次名称
     */
    @Schema(description = "餐次名称")
    private String mealName;

    /**
     * 交易时间
     */
    @Schema(description = "交易时间")
    private LocalDateTime transactionTime;

    /**
     * 消费时间（实际消费发生时间）
     */
    @Schema(description = "消费时间")
    private LocalDateTime consumeTime;

    /**
     * 交易状态（1-成功 2-处理中 3-失败 4-已取消 5-已退款）
     */
    @Schema(description = "交易状态")
    private Integer transactionStatus;

    /**
     * 交易状态描述
     */
    @Schema(description = "交易状态描述")
    private String transactionStatusDesc;

    /**
     * 支付方式（1-现金 2-微信 3-支付宝 4-银行卡 5-账户余额）
     */
    @Schema(description = "支付方式")
    private Integer paymentMethod;

    /**
     * 支付方式名称
     */
    @Schema(description = "支付方式名称")
    private String paymentMethodName;

    /**
     * 交易备注
     */
    @Schema(description = "交易备注")
    private String remark;

    /**
     * 扩展属性（JSON格式，存储业务特定的扩展信息）
     */
    @TableField(exist = false)
    @Schema(description = "扩展属性")
    private String extendedAttributes;
}
