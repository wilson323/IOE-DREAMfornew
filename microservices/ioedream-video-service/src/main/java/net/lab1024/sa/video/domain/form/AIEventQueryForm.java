package net.lab1024.sa.video.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI事件查询表单
 * <p>
 * 用于AI智能分析事件的查询条件：
 * 1. 基础查询条件
 * 2. 时间范围筛选
 * 3. 分页参数
 * 4. 排序条件
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Schema(description = "AI事件查询表单")
public class AIEventQueryForm {

    @Schema(description = "页码", example = "1")
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "20")
    @Min(value = 1, message = "每页大小必须大于0")
    private Integer pageSize = 20;

    @Schema(description = "事件ID", example = "AI_EVENT_20251216_001")
    private String eventId;

    @Schema(description = "设备ID", example = "1001")
    private Long deviceId;

    @Schema(description = "事件类型", example = "FACE_RECOGNITION")
    private String eventType;

    @Schema(description = "事件子类型", example = "STRANGER_DETECTION")
    private String eventSubType;

    @Schema(description = "事件状态(1-待处理, 2-已处理, 3-已忽略)", example = "1")
    private Integer eventStatus;

    @Schema(description = "最小优先级", example = "5")
    @Min(value = 1, message = "最小优先级必须大于0")
    private Integer priority;

    @Schema(description = "严重程度(1-低, 2-中, 3-高, 4-紧急)", example = "3")
    private Integer severity;

    @Schema(description = "最小置信度", example = "0.8")
    private Double minConfidence;

    @Schema(description = "最大置信度", example = "1.0")
    private Double maxConfidence;

    @Schema(description = "事件位置", example = "东门")
    private String location;

    @Schema(description = "开始时间", example = "2025-12-16T00:00:00")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2025-12-16T23:59:59")
    private LocalDateTime endTime;

    @Schema(description = "关键词搜索（标题、描述）", example = "陌生人")
    private String keyword;

    @Schema(description = "是否已处理", example = "false")
    private Boolean processed;

    @Schema(description = "排序字段", example = "createTime")
    private String sortField = "createTime";

    @Schema(description = "排序方向", example = "desc")
    private String sortDirection = "desc";
}
