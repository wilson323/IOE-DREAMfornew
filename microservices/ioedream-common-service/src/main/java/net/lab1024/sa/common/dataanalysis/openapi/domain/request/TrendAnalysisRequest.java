package net.lab1024.sa.common.dataanalysis.openapi.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * 趋势分析请求
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "趋势分析请求")
public class TrendAnalysisRequest {

    @Schema(description = "分析指标列表", example = "[\"user_count\", \"access_times\", \"device_usage\"]")
    @NotNull(message = "分析指标不能为空")
    private List<String> metrics;

    @Schema(description = "时间范围", example = "7d", allowableValues = {"1d", "7d", "30d", "90d", "1y", "custom"})
    @NotBlank(message = "时间范围不能为空")
    private String timeRange;

    @Schema(description = "开始时间(自定义时间范围时使用)", example = "2025-12-01")
    private String startTime;

    @Schema(description = "结束时间(自定义时间范围时使用)", example = "2025-12-16")
    private String endTime;

    @Schema(description = "分析类型", example = "growth", allowableValues = {"growth", "seasonal", "correlation", "anomaly"})
    @NotBlank(message = "分析类型不能为空")
    private String analysisType;

    @Schema(description = "数据粒度", example = "hour", allowableValues = {"minute", "hour", "day", "week", "month"})
    private String granularity;

    @Schema(description = "区域ID列表", example = "[1,2,3]")
    private List<Long> areaIds;

    @Schema(description = "分组维度", example = "area")
    private String groupByDimension;

    @Schema(description = "对比基准", example = "last_period", allowableValues = {"last_period", "last_year", "custom"})
    private String compareBaseline;

    @Schema(description = "预测周期", example = "7d")
    private String forecastPeriod;

    @Schema(description = "置信度", example = "0.95")
    private Double confidenceLevel;

    @Schema(description = "是否包含预测数据", example = "true")
    private Boolean includeForecast;

    @Schema(description = "过滤条件", example = "{\"device_type\": \"camera\"}")
    private Map<String, Object> filters;

    @Schema(description = "分析参数", example = "")
    private Map<String, Object> analysisParams;
}