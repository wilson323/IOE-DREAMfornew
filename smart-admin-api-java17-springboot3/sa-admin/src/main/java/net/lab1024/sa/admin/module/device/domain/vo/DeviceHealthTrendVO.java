package net.lab1024.sa.admin.module.device.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 设备健康趋势VO
 *
 * @author SmartAdmin Team
 * @date 2025-11-25
 */
@Data
@Schema(description = "设备健康趋势")
public class DeviceHealthTrendVO {

    @Schema(description = "设备ID")
    private Long deviceId;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "时间范围开始")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "时间范围结束")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @Schema(description = "健康评分趋势数据")
    private List<HealthScorePoint> scoreTrend;

    @Schema(description = "CPU使用率趋势")
    private List<MetricTrendPoint> cpuTrend;

    @Schema(description = "温度趋势")
    private List<MetricTrendPoint> temperatureTrend;

    @Schema(description = "指令成功率趋势")
    private List<MetricTrendPoint> commandSuccessTrend;

    @Schema(description = "告警数量趋势")
    private List<MetricTrendPoint> alarmTrend;

    @Schema(description = "趋势分析结果")
    private TrendAnalysis trendAnalysis;

    @Data
    @Schema(description = "健康评分数据点")
    public static class HealthScorePoint {
        @Schema(description = "时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime time;

        @Schema(description = "健康评分")
        private BigDecimal score;

        @Schema(description = "健康等级")
        private String level;
    }

    @Data
    @Schema(description = "指标趋势数据点")
    public static class MetricTrendPoint {
        @Schema(description = "时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime time;

        @Schema(description = "指标值")
        private BigDecimal value;

        @Schema(description = "是否超过阈值")
        private Boolean exceedThreshold;
    }

    @Data
    @Schema(description = "趋势分析结果")
    public static class TrendAnalysis {
        @Schema(description = "总体趋势")
        private String overallTrend;

        @Schema(description = "评分变化率")
        private BigDecimal scoreChangeRate;

        @Schema(description = "预测下次维护时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime predictedMaintenanceTime;

        @Schema(description = "风险等级")
        private String riskLevel;

        @Schema(description = "建议措施")
        private List<String> recommendations;
    }
}