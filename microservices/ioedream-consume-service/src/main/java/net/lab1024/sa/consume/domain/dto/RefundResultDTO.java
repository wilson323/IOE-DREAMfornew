package net.lab1024.sa.consume.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * 退款结果DTO
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Data
public class RefundResultDTO {

    /**
     * 退款单号
     */
    private String refundNo;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 退款原因
     */
    private String refundReason;

    /**
     * 退款状态
     */
    private String status;

    /**
     * 退款类型（FULL_REFUND-全额退款，PARTIAL_REFUND-部分退款）
     */
    private String refundType;

    /**
     * 消息
     */
    private String message;

    /**
     * 申请时间
     */
    private LocalDateTime applyTime;

    /**
     * 退款时间
     */
    private LocalDateTime refundTime;
}