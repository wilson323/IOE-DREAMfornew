package net.lab1024.sa.consume.domain.form;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 消费交易查询表单
 * <p>
 * 用于前端查询交易记录的请求参数
 * 支持多种查询条件和分页
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@Accessors(chain = true)
@Schema(description = "消费交易查询请求")
public class ConsumeTransactionQueryForm {

    /**
     * 当前页码
     */
    @Schema(description = "当前页码", example = "1")
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    @Schema(description = "每页大小", example = "20")
    private Integer pageSize = 20;

    /**
     * 交易ID（精确查询）
     */
    @Schema(description = "交易ID", example = "20251221001")
    private String transactionId;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    /**
     * 用户姓名（模糊查询）
     */
    @Schema(description = "用户姓名", example = "张三")
    private String userName;

    /**
     * 设备ID
     */
    @Schema(description = "设备ID", example = "DEV001")
    private String deviceId;

    /**
     * 设备名称（模糊查询）
     */
    @Schema(description = "设备名称", example = "食堂POS机")
    private String deviceName;

    /**
     * 餐次ID
     */
    @Schema(description = "餐次ID", example = "MEAL_001")
    private String mealId;

    /**
     * 交易状态
     */
    @Schema(description = "交易状态", example = "SUCCESS")
    private String status;

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
     * 最小交易金额
     */
    @Schema(description = "最小交易金额", example = "0.01")
    private BigDecimal minAmount;

    /**
     * 最大交易金额
     */
    @Schema(description = "最大交易金额", example = "1000.00")
    private BigDecimal maxAmount;

    /**
     * 交易时间开始
     */
    @Schema(description = "交易时间开始", example = "2025-12-21T00:00:00")
    private LocalDateTime startTime;

    /**
     * 交易时间结束
     */
    @Schema(description = "交易时间结束", example = "2025-12-21T23:59:59")
    private LocalDateTime endTime;

    /**
     * 订单号（精确查询）
     */
    @Schema(description = "订单号", example = "ORDER20251221001")
    private String orderNo;

    /**
     * 排序字段
     */
    @Schema(description = "排序字段", example = "transactionTime")
    private String sortField = "transactionTime";

    /**
     * 排序方向
     */
    @Schema(description = "排序方向", example = "desc")
    private String sortOrder = "desc";

    /**
     * 是否包含已删除记录
     */
    @Schema(description = "是否包含已删除记录", example = "false")
    private Boolean includeDeleted = false;

    /**
     * 是否只查询异常交易
     */
    @Schema(description = "是否只查询异常交易", example = "false")
    private Boolean abnormal = false;
}