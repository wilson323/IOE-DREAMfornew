package net.lab1024.sa.monitor.domain.vo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 告警统计VO
 *
 * 用于展示告警统计分析结果，包括按级别、类型、服务的统计数据
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Data
@Schema(description = "告警统计VO")
public class AlertStatisticsVO {

    /**
     * 总告警数
     */
    @Schema(description = "总告警数", example = "100")
    private Long totalAlerts;

    /**
     * 活跃告警数
     */
    @Schema(description = "活跃告警数", example = "25")
    private Long activeAlerts;

    /**
     * 已解决告警数
     */
    @Schema(description = "已解决告警数", example = "75")
    private Long resolvedAlerts;

    /**
     * 按级别统计
     */
    @Schema(description = "按级别统计")
    private List<Map<String, Object>> levelStatistics;

    /**
     * 按类型统计
     */
    @Schema(description = "按类型统计")
    private List<Map<String, Object>> typeStatistics;

    /**
     * 按服务统计
     */
    @Schema(description = "按服务统计")
    private List<Map<String, Object>> serviceStatistics;

    /**
     * 严重级别告警数 (CRITICAL)
     */
    @Schema(description = "严重级别告警数", example = "5")
    private Long criticalCount;

    /**
     * 错误级别告警数 (ERROR)
     */
    @Schema(description = "错误级别告警数", example = "15")
    private Long errorCount;

    /**
     * 警告级别告警数 (WARNING)
     */
    @Schema(description = "警告级别告警数", example = "30")
    private Long warningCount;

    /**
     * 信息级别告警数 (INFO)
     */
    @Schema(description = "信息级别告警数", example = "50")
    private Long infoCount;

    /**
     * 统计时间范围
     */
    @Schema(description = "统计时间范围", example = "7 days")
    private String timeRange;

    /**
     * 开始时间
     */
    @Schema(description = "开始时间", example = "2025-01-23 00:00:00")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @Schema(description = "结束时间", example = "2025-01-30 23:59:59")
    private LocalDateTime endTime;

    /**
     * 统计生成时间
     */
    @Schema(description = "统计生成时间", example = "2025-01-30 12:00:00")
    private LocalDateTime statisticsTime;
}
