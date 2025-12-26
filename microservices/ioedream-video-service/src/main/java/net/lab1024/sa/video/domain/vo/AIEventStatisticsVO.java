package net.lab1024.sa.video.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * AI事件统计视图对象
 * <p>
 * 用于AI智能分析事件的统计分析展示：
 * 1. 总体统计数据
 * 2. 分类统计信息
 * 3. 趋势分析数据
 * 4. 处理效率指标
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Schema(description = "AI事件统计视图对象")
public class AIEventStatisticsVO {

    @Schema(description = "总事件数", example = "1250")
    private Long totalEvents;

    @Schema(description = "已处理事件数", example = "1100")
    private Long processedEvents;

    @Schema(description = "未处理事件数", example = "150")
    private Long unprocessedEvents;

    @Schema(description = "处理率", example = "88.0%")
    private String processRate;

    @Schema(description = "按事件类型统计", example = "{\"FACE_RECOGNITION\": 500, \"BEHAVIOR_ANALYSIS\": 300}")
    private Map<String, Long> typeStatistics;

    @Schema(description = "按优先级统计", example = "{\"1\": 50, \"2\": 100, \"3\": 200}")
    private Map<Integer, Long> priorityStatistics;

    @Schema(description = "按严重程度统计", example = "{\"1\": 400, \"2\": 600, \"3\": 200, \"4\": 50}")
    private Map<Integer, Long> severityStatistics;

    @Schema(description = "按设备统计", example = "{\"1001\": 150, \"1002\": 120}")
    private Map<String, Long> deviceStatistics;

    @Schema(description = "按时间统计（小时）", example = "{\"0\": 20, \"1\": 15, \"2\": 25}")
    private Map<Integer, Long> hourlyStatistics;

    @Schema(description = "按天统计", example = "{\"2025-12-16\": 150, \"2025-12-15\": 180}")
    private Map<String, Long> dailyStatistics;

    @Schema(description = "平均置信度", example = "0.85")
    private Double averageConfidence;

    @Schema(description = "置信度百分比", example = "85%")
    private String averageConfidencePercent;

    @Schema(description = "高优先级事件数", example = "85")
    private Long highPriorityEvents;

    @Schema(description = "紧急事件数", example = "25")
    private Long urgentEvents;

    @Schema(description = "平均处理时间(分钟)", example = "15.5")
    private Double averageProcessTimeMinutes;

    @Schema(description = "平均处理时间描述", example = "15.5分钟")
    private String averageProcessTimeDesc;

    @Schema(description = "今日事件数", example = "85")
    private Long todayEvents;

    @Schema(description = "昨日事件数", example = "92")
    private Long yesterdayEvents;

    @Schema(description = "今日增长率", example = "-7.6%")
    private String todayGrowthRate;

    @Schema(description = "本周事件数", example = "580")
    private Long weeklyEvents;

    @Schema(description = "上周事件数", example = "620")
    private Long lastWeekEvents;

    @Schema(description = "本周增长率", example = "-6.5%")
    private String weeklyGrowthRate;

    @Schema(description = "本月事件数", example = "2450")
    private Long monthlyEvents;

    @Schema(description = "上月事件数", example = "2180")
    private Long lastMonthEvents;

    @Schema(description = "本月增长率", example = "12.4%")
    private String monthlyGrowthRate;

    @Schema(description = "误报率", example = "3.2%")
    private String falsePositiveRate;

    @Schema(description = "准确率", example = "96.8%")
    private String accuracyRate;

    @Schema(description = "AI模型统计", example = "{\"FaceRecognitionV2\": 600, \"BehaviorAnalysisV1\": 400}")
    private Map<String, Long> modelStatistics;

    @Schema(description = "活跃设备数", example = "45")
    private Integer activeDeviceCount;

    @Schema(description = "总设备数", example = "50")
    private Integer totalDeviceCount;

    @Schema(description = "设备活跃率", example = "90.0%")
    private String deviceActiveRate;

    @Schema(description = "存储使用量(GB)", example = "125.5")
    private Double storageUsageGB;

    @Schema(description = "存储使用率", example = "62.8%")
    private String storageUsageRate;
}
