package net.lab1024.sa.consume.domain.vo;

import java.math.BigDecimal;

import lombok.Data;

/**
 * 移动端用户消费信息
 *
 * @author IOE-DREAM Team
 * @since 2025-12-22
 */
@Data
public class ConsumeMobileUserInfoVO {

    private Long userId;

    private String userName;

    private BigDecimal accountBalance;

    private BigDecimal allowanceBalance;

    private BigDecimal frozenBalance;

    private BigDecimal todayConsumeAmount;

    private Integer todayConsumeCount;

    private Integer accountStatus;
}
