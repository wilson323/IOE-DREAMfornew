package net.lab1024.sa.audit.domain.vo;

import java.time.LocalDate;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 每日统计VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Data
@Accessors(chain = true)
public class DailyStatisticsVO {

    /**
     * 操作日期
     */
    private LocalDate operationDate;

    /**
     * 总操作数
     */
    private Long totalOperations;

    /**
     * 成功操作数
     */
    private Long successOperations;

    /**
     * 失败操作数
     */
    private Long failureOperations;

    /**
     * 成功率
     */
    private Double successRate;
}
