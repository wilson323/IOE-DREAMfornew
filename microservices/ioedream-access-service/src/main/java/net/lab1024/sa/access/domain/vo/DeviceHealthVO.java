package net.lab1024.sa.access.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设备健康状态视图对象
 * <p>
 * 设备健康监控结果的数据传输对象
 * 严格遵循CLAUDE.md规范：
 * - 使用@Data注解
 * - 完整的字段文档注解
 * - 构建者模式支持
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
@Schema(description = "设备健康状态信息")
public class DeviceHealthVO {

    /**
     * 设备ID
     */
    @Schema(description = "设备ID", example = "1001")
    private Long deviceId;

    /**
     * 设备名称
     */
    @Schema(description = "设备名称", example = "主入口门禁控制器")
    private String deviceName;

    /**
     * 设备编码
     */
    @Schema(description = "设备编码", example = "ACC-CTRL-001")
    private String deviceCode;

    /**
     * 设备类型
     */
    @Schema(description = "设备类型", example = "ACCESS_CONTROLLER")
    private String deviceType;

    /**
     * 健康评分（0-100）
     */
    @Schema(description = "健康评分（0-100）", example = "92.5")
    private BigDecimal healthScore;

    /**
     * 健康状态
     * HEALTHY - 健康
     * WARNING - 亚健康
     * CRITICAL - 危险
     * OFFLINE - 离线
     */
    @Schema(description = "健康状态", example = "HEALTHY")
    private String healthStatus;

    /**
     * 在线状态
     */
    @Schema(description = "在线状态", example = "true")
    private Boolean onlineStatus;

    /**
     * 响应时间（毫秒）
     */
    @Schema(description = "响应时间（毫秒）", example = "156")
    private Long responseTime;

    /**
     * CPU使用率（%）
     */
    @Schema(description = "CPU使用率（%）", example = "35.6")
    private BigDecimal cpuUsage;

    /**
     * 内存使用率（%）
     */
    @Schema(description = "内存使用率（%）", example = "42.3")
    private BigDecimal memoryUsage;

    /**
     * 磁盘使用率（%）
     */
    @Schema(description = "磁盘使用率（%）", example = "28.7")
    private BigDecimal diskUsage;

    /**
     * 网络连接质量
     */
    @Schema(description = "网络连接质量", example = "EXCELLENT")
    private String networkQuality;

    /**
     * 24小时内错误次数
     */
    @Schema(description = "24小时内错误次数", example = "2")
    private Integer errorCount24h;

    /**
     * 24小时内成功率（%）
     */
    @Schema(description = "24小时内成功率（%）", example = "99.8")
    private BigDecimal successRate24h;

    /**
     * 最后在线时间
     */
    @Schema(description = "最后在线时间", example = "2025-01-30T10:30:00")
    private LocalDateTime lastOnlineTime;

    /**
     * 连续运行时长（小时）
     */
    @Schema(description = "连续运行时长（小时）", example = "720")
    private Long uptimeHours;

    /**
     * 异常指标列表
     */
    @Schema(description = "异常指标列表")
    private List<AnomalousMetricVO> anomalousMetrics;

    /**
     * 性能指标详情
     */
    @Schema(description = "性能指标详情")
    private Map<String, Object> performanceMetrics;

    /**
     * 健康趋势（最近7天）
     */
    @Schema(description = "健康趋势（最近7天）")
    private List<HealthTrendVO> healthTrends;

    /**
     * 建议操作
     */
    @Schema(description = "建议操作", example = "建议在下次维护时检查网络连接稳定性")
    private String recommendation;

    /**
     * 监控时间
     */
    @Schema(description = "监控时间", example = "2025-01-30T15:45:00")
    private LocalDateTime monitorTime;

    /**
     * 异常指标内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "异常指标")
    public static class AnomalousMetricVO {

        @Schema(description = "指标名称", example = "CPU使用率")
        private String metricName;

        @Schema(description = "当前值", example = "85.6")
        private BigDecimal currentValue;

        @Schema(description = "正常范围", example = "0-70")
        private String normalRange;

        @Schema(description = "异常级别", example = "WARNING")
        private String anomalyLevel;

        @Schema(description = "异常描述", example = "CPU使用率超过正常阈值")
        private String description;
    }

    /**
     * 健康趋势内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "健康趋势")
    public static class HealthTrendVO {

        @Schema(description = "日期", example = "2025-01-30")
        private String date;

        @Schema(description = "健康评分", example = "92.5")
        private BigDecimal healthScore;

        @Schema(description = "状态", example = "HEALTHY")
        private String status;
    }
}