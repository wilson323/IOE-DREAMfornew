package net.lab1024.sa.consume.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;
import java.math.BigDecimal;

/**
 * 退款结果
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-19
 */
@Data
@Accessors(chain = true)
public class RefundResult {
    /**
     * 退款ID
     */
    private Long refundId;
    /**
     * 退款编号
     */
    private String refundNo;
    /**
     * 是否成功
     */
    private Boolean success;
    /**
     * 返回消息
     */
    private String message;
    /**
     * 退款金额
     */
    private java.math.BigDecimal refundAmount;}
