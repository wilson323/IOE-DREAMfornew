package net.lab1024.sa.consume.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 消费交易视图对象
 * <p>
 * 用于返回给前端显示的交易信息
 * 不包含敏感的内部字段
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@Accessors(chain = true)
@Schema(description = "消费交易信息")
public class ConsumeTransactionVO {

    /**
     * 交易ID
     */
    @Schema(description = "交易ID", example = "20251221001")
    private String transactionId;

    /**
     * 人员ID
     */
    @Schema(description = "人员ID", example = "1001")
    private Long userId;

    /**
     * 人员姓名
     */
    @Schema(description = "人员姓名", example = "张三")
    private String userName;

    /**
     * 交易金额
     */
    @Schema(description = "交易金额", example = "25.50")
    private BigDecimal amount;

    /**
     * 设备ID
     */
    @Schema(description = "设备ID", example = "DEV001")
    private String deviceId;

    /**
     * 设备名称
     */
    @Schema(description = "设备名称", example = "食堂POS机01")
    private String deviceName;

    /**
     * 餐次ID
     */
    @Schema(description = "餐次ID", example = "MEAL_001")
    private String mealId;

    /**
     * 餐次名称
     */
    @Schema(description = "餐次名称", example = "午餐")
    private String mealName;

    /**
     * 交易前余额
     */
    @Schema(description = "交易前余额", example = "100.00")
    private BigDecimal balanceBefore;

    /**
     * 交易后余额
     */
    @Schema(description = "交易后余额", example = "74.50")
    private BigDecimal balanceAfter;

    /**
     * 消费模式
     */
    @Schema(description = "消费模式", example = "AMOUNT")
    private String consumeMode;

    /**
     * 消费类型
     */
    @Schema(description = "消费类型", example = "CONSUME")
    private String consumeType;

    /**
     * 交易时间
     */
    @Schema(description = "交易时间", example = "2025-12-21T12:30:00")
    private LocalDateTime transactionTime;

    /**
     * 交易状态
     */
    @Schema(description = "交易状态", example = "SUCCESS")
    private String status;

    /**
     * 错误信息
     */
    @Schema(description = "错误信息", example = "")
    private String errorMsg;

    /**
     * 订单号
     */
    @Schema(description = "订单号", example = "ORDER20251221001")
    private String orderNo;
}