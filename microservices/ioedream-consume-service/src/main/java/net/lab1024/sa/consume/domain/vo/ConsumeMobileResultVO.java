package net.lab1024.sa.consume.domain.vo;

import java.math.BigDecimal;
import lombok.Data;

/**
 * 移动端消费结果VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class ConsumeMobileResultVO {

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 消息
     */
    private String message;

    /**
     * 交易ID
     */
    private Long transactionId;

    /**
     * 消费金额
     */
    private BigDecimal amount;

    /**
     * 账户余额
     */
    private BigDecimal balance;
}
