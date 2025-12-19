package net.lab1024.sa.video.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.common.domain.PageParam;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 行为分析表单
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
@Data
@Schema(description = "行为分析表单")
public class VideoBehaviorAnalysisForm extends PageParam {

    /**
     * 设备ID列表
     */
    @Schema(description = "设备ID列表", example = "[1001,1002,1003]")
    private List<Long> deviceIds;

    /**
     * 行为类型列表
     */
    @Schema(description = "行为类型列表", example = "[1,2,5]")
    private List<Integer> behaviorTypes;

    /**
     * 严重程度最小值
     */
    @Schema(description = "严重程度最小值", example = "2")
    @Min(value = 1, message = "严重程度最小值不能小于1")
    @Max(value = 4, message = "严重程度最小值不能大于4")
    private Integer minSeverityLevel;

    /**
     * 严重程度最大值
     */
    @Schema(description = "严重程度最大值", example = "4")
    @Min(value = 1, message = "严重程度最大值不能小于1")
    @Max(value = 4, message = "严重程度最大值不能大于4")
    private Integer maxSeverityLevel;

    /**
     * 置信度最小值
     */
    @Schema(description = "置信度最小值", example = "70.0")
    @DecimalMin(value = "0.0", message = "置信度最小值不能小于0")
    @DecimalMax(value = "100.0", message = "置信度最小值不能大于100")
    private Double minConfidenceScore;

    /**
     * 置信度最大值
     */
    @Schema(description = "置信度最大值", example = "100.0")
    @DecimalMin(value = "0.0", message = "置信度最大值不能小于0")
    @DecimalMax(value = "100.0", message = "置信度最大值不能大于100")
    private Double maxConfidenceScore;

    /**
     * 是否包含告警
     */
    @Schema(description = "是否包含告警", example = "true")
    private Boolean includeAlarms;

    /**
     * 是否只显示未处理记录
     */
    @Schema(description = "是否只显示未处理记录", example = "false")
    private Boolean onlyUnprocessed;

    /**
     * 是否只显示需要人工确认
     */
    @Schema(description = "是否只显示需要人工确认", example = "false")
    private Boolean onlyNeedingManualConfirm;

    /**
     * 检测开始时间
     */
    @Schema(description = "检测开始时间", example = "2025-12-15T00:00:00")
    private LocalDateTime startTime;

    /**
     * 检测结束时间
     */
    @Schema(description = "检测结束时间", example = "2025-12-16T23:59:59")
    private LocalDateTime endTime;

    /**
     * 人员ID列表
     */
    @Schema(description = "人员ID列表", example = "[1001,1002]")
    private List<Long> personIds;

    /**
     * 处理状态列表
     */
    @Schema(description = "处理状态列表", example = "[0,1,2]")
    private List<Integer> processStatuses;

    /**
     * 告警级别列表
     */
    @Schema(description = "告警级别列表", example = "[1,2,3,4]")
    private List<Integer> alarmLevels;

    /**
     * 目标数量最小值
     */
    @Schema(description = "目标数量最小值", example = "1")
    @Min(value = 0, message = "目标数量最小值不能小于0")
    private Integer minTargetCount;

    /**
     * 目标数量最大值
     */
    @Schema(description = "目标数量最大值", example = "10")
    @Min(value = 0, message = "目标数量最大值不能小于0")
    private Integer maxTargetCount;

    /**
     * 持续时间最小值（秒）
     */
    @Schema(description = "持续时间最小值（秒）", example = "60")
    @Min(value = 0, message = "持续时间最小值不能小于0")
    private Long minDurationSeconds;

    /**
     * 持续时间最大值（秒）
     */
    @Schema(description = "持续时间最大值（秒）", example = "3600")
    @Min(value = 0, message = "持续时间最大值不能小于0")
    private Long maxDurationSeconds;

    /**
     * 检测算法类型
     */
    @Schema(description = "检测算法类型", example = "1")
    @Min(value = 1, message = "检测算法类型必须在1-8之间")
    @Max(value = 8, message = "检测算法类型必须在1-8之间")
    private Integer detectionAlgorithm;

    /**
     * 分析维度
     */
    @Schema(description = "分析维度", example = "time")
    private String analysisDimension;

    /**
     * 统计类型
     */
    @Schema(description = "统计类型", example = "daily")
    private String statisticsType;

    /**
     * 是否包含详情
     */
    @Schema(description = "是否包含详情", example = "false")
    private Boolean includeDetails;

    /**
     * 排序字段
     */
    @Schema(description = "排序字段", example = "detection_time")
    private String sortField;

    /**
     * 排序方式
     */
    @Schema(description = "排序方式", example = "desc")
    private String sortOrder;

    /**
     * 导出格式
     */
    @Schema(description = "导出格式", example = "excel")
    private String exportFormat;

    /**
     * 是否生成图表
     */
    @Schema(description = "是否生成图表", example = "true")
    private Boolean generateCharts;

    /**
     * 图表类型
     */
    @Schema(description = "图表类型", example = "[\"pie\",\"line\",\"bar\"]")
    private List<String> chartTypes;

    /**
     * 分析目的
     */
    @Schema(description = "分析目的", example = "安全监控分析")
    private String analysisPurpose;

    /**
     * 报告标题
     */
    @Schema(description = "报告标题", example = "行为分析报告")
    private String reportTitle;

    /**
     * 备注信息
     */
    @Schema(description = "备注信息", example = "重点关注异常行为模式")
    @Size(max = 500, message = "备注信息长度不能超过500个字符")
    private String remark;
}