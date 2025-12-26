package net.lab1024.sa.consume.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * 设备今日消费统计
 *
 * @author IOE-DREAM Team
 * @since 2025-12-22
 */
@Data
public class ConsumeMobileStatsVO {

    private Long deviceId;

    private Integer todayConsumeCount;

    private BigDecimal todayConsumeAmount;

    private Integer todayUserCount;

    private LocalDateTime lastConsumeTime;
}
