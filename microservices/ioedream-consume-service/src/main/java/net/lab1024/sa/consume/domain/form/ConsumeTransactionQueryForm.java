package net.lab1024.sa.consume.domain.form;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 消费交易查询表单
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用Form后缀命名
 * - 使用Jakarta验证注解
 * - 继承分页参数规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class ConsumeTransactionQueryForm {

    /**
     * 页码（从1开始）
     */
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    @NotNull(message = "每页大小不能为空")
    @Min(value = 1, message = "每页大小必须大于0")
    private Integer pageSize = 20;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 交易流水号
     */
    private String transactionNo;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 区域ID
     */
    private String areaId;

    /**
     * 开始日期
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    /**
     * 消费模式
     * <p>
     * 枚举值：
     * - FIXED - 定值
     * - AMOUNT - 金额
     * - PRODUCT - 商品
     * - COUNT - 计次
     * </p>
     */
    private String consumeMode;

    /**
     * 交易状态
     * <p>
     * 枚举值：
     * - SUCCESS - 成功
     * - FAILED - 失败
     * - REFUND - 已退款
     * </p>
     */
    private String status;
}




