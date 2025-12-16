package net.lab1024.sa.consume.domain.vo;

import java.math.BigDecimal;
import lombok.Data;

/**
 * 移动端统计数据VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class ConsumeMobileStatsVO {

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 今日交易笔数
     */
    private Integer todayCount;

    /**
     * 今日交易金额
     */
    private BigDecimal todayAmount;

    /**
     * 今日交易人数
     */
    private Integer todayUserCount;
}



