package net.lab1024.sa.consume.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;
import java.math.BigDecimal;

/**
 * 退款上下文
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-19
 */
@Data
@Accessors(chain = true)
public class RefundContext {
    /**
     * 原支付ID
     */
    private Long paymentId;
    /**
     * 退款金额
     */
    private java.math.BigDecimal refundAmount;
    /**
     * 退款原因
     */
    private String refundReason;
    /**
     * 备注
     */
    private String remark;}
