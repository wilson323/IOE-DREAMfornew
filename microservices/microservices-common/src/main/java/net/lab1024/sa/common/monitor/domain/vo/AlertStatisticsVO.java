package net.lab1024.sa.common.monitor.domain.vo;

import lombok.Data;

/**
 * 告警统计VO
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@Data注解自动生成getter/setter
 * - 完整的字段注释
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02
 */
@Data
public class AlertStatisticsVO {

    /**
     * 总告警数
     */
    private Long totalAlerts;

    /**
     * 已解决告警数
     */
    private Long resolvedAlerts;

    /**
     * 活跃告警数
     */
    private Long activeAlerts;

    /**
     * 严重告警数
     */
    private Long criticalAlerts;

    /**
     * 错误告警数
     */
    private Long errorAlerts;

    /**
     * 警告告警数
     */
    private Long warningAlerts;

    /**
     * 信息告警数
     */
    private Long infoAlerts;

    /**
     * 解决率（百分比）
     */
    private Double resolutionRate;

    /**
     * 平均解决时间（分钟）
     */
    private Double avgResolutionTime;
}

