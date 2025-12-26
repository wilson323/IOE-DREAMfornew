package net.lab1024.sa.consume.domain.vo;

import java.math.BigDecimal;

import lombok.Data;

/**
 * 移动端消费结果
 *
 * @author IOE-DREAM Team
 * @since 2025-12-22
 */
@Data
public class ConsumeMobileResultVO {

    private String transactionNo;

    private Boolean success;

    private String message;

    private BigDecimal balance;

    private BigDecimal consumeAmount;
}
