package net.lab1024.sa.admin.module.consume.domain.dto;

import lombok.Data;
import net.lab1024.sa.admin.module.consume.domain.enums.ConsumeModeEnum;
import net.lab1024.sa.admin.module.consume.domain.enums.ConsumeStatusEnum;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 消费结果DTO
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Data
public class ConsumeResultDTO {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 消费金额
     */
    private BigDecimal amount;

    /**
     * 消息
     */
    private String message;

    /**
     * 消费状态
     */
    private ConsumeStatusEnum status;

    /**
     * 消费时间
     */
    private LocalDateTime consumeTime;

    /**
     * 新余额
     */
    private BigDecimal newBalance;

    /**
     * 消费模式
     */
    private ConsumeModeEnum consumeMode;

    /**
     * 支付方式
     */
    private String payMethod;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 交易流水号
     */
    private String transactionNo;
}