package net.lab1024.sa.consume.domain.vo;

import java.math.BigDecimal;

import lombok.Data;

/**
 * 移动端用户信息
 *
 * @author IOE-DREAM Team
 * @since 2025-12-22
 */
@Data
public class ConsumeMobileUserVO {

    private Long userId;

    private String userName;

    private String phone;

    private String cardNumber;

    private BigDecimal accountBalance;

    private Integer accountStatus;
}
