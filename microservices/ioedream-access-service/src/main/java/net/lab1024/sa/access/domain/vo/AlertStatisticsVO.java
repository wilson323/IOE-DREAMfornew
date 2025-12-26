package net.lab1024.sa.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 告警统计VO
 * <p>
 * 用于返回告警统计数据
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "告警统计VO")
public class AlertStatisticsVO {

    /**
     * 总告警数
     */
    @Schema(description = "总告警数", example = "100")
    private Long totalAlerts;

    /**
     * 未确认告警数
     */
    @Schema(description = "未确认告警数", example = "10")
    private Long unconfirmedAlerts;

    /**
     * 已确认告警数
     */
    @Schema(description = "已确认告警数", example = "5")
    private Long confirmedAlerts;

    /**
     * 已处理告警数
     */
    @Schema(description = "已处理告警数", example = "80")
    private Long handledAlerts;

    /**
     * 已忽略告警数
     */
    @Schema(description = "已忽略告警数", example = "5")
    private Long ignoredAlerts;

    /**
     * 紧急告警数（级别4）
     */
    @Schema(description = "紧急告警数", example = "2")
    private Long criticalAlerts;

    /**
     * 高级告警数（级别3）
     */
    @Schema(description = "高级告警数", example = "8")
    private Long highAlerts;

    /**
     * 中级告警数（级别2）
     */
    @Schema(description = "中级告警数", example = "30")
    private Long mediumAlerts;

    /**
     * 低级告警数（级别1）
     */
    @Schema(description = "低级告警数", example = "60")
    private Long lowAlerts;

    /**
     * 今日新增告警数
     */
    @Schema(description = "今日新增告警数", example = "15")
    private Long todayAlerts;

    /**
     * 本周新增告警数
     */
    @Schema(description = "本周新增告警数", example = "50")
    private Long thisWeekAlerts;

    /**
     * 本月新增告警数
     */
    @Schema(description = "本月新增告警数", example = "100")
    private Long thisMonthAlerts;

    /**
     * 平均处理时间（分钟）
     */
    @Schema(description = "平均处理时间（分钟）", example = "30")
    private Double avgHandleMinutes;

    /**
     * 告警处理率（百分比）
     */
    @Schema(description = "告警处理率（%）", example = "85.5")
    private Double handleRate;

    /**
     * 各类型告警数量统计
     * key: 告警类型（DEVICE_OFFLINE, DEVICE_FAULT等）
     * value: 数量
     */
    @Schema(description = "各类型告警数量统计")
    private Map<String, Long> alertTypeCount;

    /**
     * 各设备告警数量统计（TOP 10）
     * key: 设备名称
     * value: 告警数量
     */
    @Schema(description = "各设备告警数量统计（TOP 10）")
    private Map<String, Long> deviceAlertCount;

    /**
     * 最近7天告警趋势
     * key: 日期（2025-01-24）
     * value: 告警数量
     */
    @Schema(description = "最近7天告警趋势")
    private Map<String, Long> recentTrend;
}
