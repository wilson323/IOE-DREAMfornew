package net.lab1024.sa.consume.domain.vo;

import java.math.BigDecimal;
import lombok.Data;

/**
 * 移动端用户消费信息VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class ConsumeMobileUserInfoVO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 总余额
     */
    private BigDecimal totalBalance;

    /**
     * 可用余额
     */
    private BigDecimal availableBalance;

    /**
     * 冻结余额
     */
    private BigDecimal frozenBalance;
}



