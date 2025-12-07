package net.lab1024.sa.consume.domain.vo;

import java.math.BigDecimal;
import lombok.Data;

/**
 * 移动端交易汇总VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class ConsumeMobileSummaryVO {

    /**
     * 区域ID
     */
    private String areaId;

    /**
     * 总交易笔数
     */
    private Integer totalCount;

    /**
     * 总交易金额
     */
    private BigDecimal totalAmount;

    /**
     * 交易人数
     */
    private Integer userCount;
}
