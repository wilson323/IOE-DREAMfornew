/*
 * 邮件统计信息
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-17
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.consume.domain.vo;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 邮件统计信息
 * 封装邮件发送的统计信息
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailStatistics {

    /**
     * 时间范围
     */
    private String timeRange;

    /**
     * 总发送数量
     */
    private Integer totalSent;

    /**
     * 成功发送数量
     */
    private Integer successSent;

    /**
     * 失败发送数量
     */
    private Integer failedSent;

    /**
     * 成功率
     */
    private Double successRate;

    /**
     * 平均响应时间（毫秒）
     */
    private Long averageResponseTimeMs;

    /**
     * 各优先级统计
     */
    private Map<String, Integer> priorityCount;

    /**
     * 各业务类型统计
     */
    private Map<String, Integer> businessTypeCount;

    /**
     * 统计开始时间
     */
    private LocalDateTime startTime;

    /**
     * 统计结束时间
     */
    private LocalDateTime endTime;
}
