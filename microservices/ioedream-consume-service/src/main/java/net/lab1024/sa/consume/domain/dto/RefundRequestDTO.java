package net.lab1024.sa.consume.domain.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 退款申请DTO
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Data
public class RefundRequestDTO {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    @Positive(message = "用户ID必须大于0")
    private Long userId;

    /**
     * 消费记录ID
     */
    @NotNull(message = "消费记录ID不能为空")
    @Positive(message = "消费记录ID必须大于0")
    private Long consumeRecordId;

    /**
     * 退款金额
     */
    @NotNull(message = "退款金额不能为空")
    @Positive(message = "退款金额必须大于0")
    private BigDecimal refundAmount;

    /**
     * 退款原因
     */
    @NotNull(message = "退款原因不能为空")
    private String refundReason;

    /**
     * 退款描述
     */
    private String refundDescription;
}