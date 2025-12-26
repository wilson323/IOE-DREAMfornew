package net.lab1024.sa.consume.domain.vo;

import java.math.BigDecimal;

import lombok.Data;

/**
 * 实时交易汇总
 *
 * @author IOE-DREAM Team
 * @since 2025-12-22
 */
@Data
public class ConsumeMobileSummaryVO {

    private String areaId;

    private Integer todayConsumeCount;

    private BigDecimal todayConsumeAmount;

    private Integer todayUserCount;

    private Integer realtimeConsumeCount;

    private BigDecimal realtimeConsumeAmount;
}
