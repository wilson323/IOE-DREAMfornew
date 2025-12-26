package net.lab1024.sa.video.openapi.domain.request;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI分析查询请求
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI分析查询请求")
public class AIAnalysisQueryRequest {

    @Schema(description = "设备ID", example = "CAM001")
    private String deviceId;

    @Schema(description = "设备ID列表")
    private List<String> deviceIds;

    @Schema(description = "分析类型", example = "face_detection", allowableValues = { "face_detection", "object_detection",
            "behavior_analysis", "plate_recognition" })
    private String analysisType;

    @Schema(description = "分析类型列表")
    private List<String> analysisTypes;

    @Schema(description = "开始时间", example = "2025-12-16T00:00:00")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2025-12-16T23:59:59")
    private LocalDateTime endTime;

    @Schema(description = "状态", example = "completed", allowableValues = { "pending", "processing", "completed",
            "failed" })
    private String status;

    @Schema(description = "对象类型", example = "person")
    private String objectType;

    @Schema(description = "对象标签", example = "intrusion")
    private String label;

    @Schema(description = "最低置信度", example = "0.8")
    private Double minConfidence;

    @Schema(description = "最低置信度（兼容字段）", example = "0.8")
    private Double confidenceThreshold;

    @Schema(description = "开始时间字符串（兼容字段）", example = "2025-12-16T00:00:00")
    private String startTimeStr;

    @Schema(description = "结束时间字符串（兼容字段）", example = "2025-12-16T23:59:59")
    private String endTimeStr;

    @Schema(description = "页码", example = "1")
    private Integer pageNum;

    @Schema(description = "每页大小", example = "20")
    private Integer pageSize;

    @Schema(description = "排序字段", example = "analysisTime")
    private String sortField;

    @Schema(description = "排序方向", example = "desc", allowableValues = { "asc", "desc" })
    private String sortOrder;
}
