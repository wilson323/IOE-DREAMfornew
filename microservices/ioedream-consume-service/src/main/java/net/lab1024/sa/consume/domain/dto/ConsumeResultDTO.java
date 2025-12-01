package net.lab1024.sa.consume.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * 消费结果DTO
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Data
public class ConsumeResultDTO {

    /**
     * 消费记录ID
     */
    private Long consumeId;

    /**
     * 消费单号
     */
    private String consumeNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 消费金额
     */
    private BigDecimal amount;

    /**
     * 消费状态
     */
    private String status;

    /**
     * 消费时间
     */
    private LocalDateTime consumeTime;

    /**
     * 消费类型
     */
    private String consumeType;

    /**
     * 消费模式
     */
    private String consumptionMode;

    /**
     * 账户余额（消费后）
     */
    private BigDecimal balanceAfter;

    /**
     * 结果消息
     */
    private String message;

    /**
     * 扩展信息
     */
    private java.util.Map<String, Object> extraInfo;
}