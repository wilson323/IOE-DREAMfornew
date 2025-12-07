package net.lab1024.sa.consume.domain.vo;

import java.math.BigDecimal;
import lombok.Data;

/**
 * 消费实时统计数据VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class ConsumeRealtimeStatisticsVO {

    /**
     * 今日消费金额
     */
    private BigDecimal todayAmount;

    /**
     * 今日消费笔数
     */
    private Integer todayCount;

    /**
     * 当前在线设备数
     */
    private Integer onlineDeviceCount;

    /**
     * 当前消费人数
     */
    private Integer currentConsumeUserCount;

    /**
     * 最近1小时消费金额
     */
    private BigDecimal lastHourAmount;

    /**
     * 最近1小时消费笔数
     */
    private Integer lastHourCount;
}
