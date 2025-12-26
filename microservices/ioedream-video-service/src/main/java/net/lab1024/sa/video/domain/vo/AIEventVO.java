package net.lab1024.sa.video.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI事件视图对象
 * <p>
 * 用于AI智能分析事件的前端展示：
 * 1. 完整的事件信息
 * 2. 处理状态和结果
 * 3. 关联媒体文件
 * 4. 统计和分析数据
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Schema(description = "AI事件视图对象")
public class AIEventVO {

    @Schema(description = "主键ID", example = "1001")
    private Long id;

    @Schema(description = "事件ID", example = "AI_EVENT_20251216_001")
    private String eventId;

    @Schema(description = "设备ID", example = "1001")
    private Long deviceId;

    @Schema(description = "设备名称", example = "东门摄像头-001")
    private String deviceName;

    @Schema(description = "事件类型", example = "FACE_RECOGNITION")
    private String eventType;

    @Schema(description = "事件类型名称", example = "人脸识别")
    private String eventTypeName;

    @Schema(description = "事件子类型", example = "STRANGER_DETECTION")
    private String eventSubType;

    @Schema(description = "事件子类型名称", example = "陌生人检测")
    private String eventSubTypeName;

    @Schema(description = "事件标题", example = "检测到陌生人进入")
    private String eventTitle;

    @Schema(description = "事件描述", example = "在区域A1检测到未知人员，识别置信度92%")
    private String eventDescription;

    @Schema(description = "优先级(1-10)", example = "8")
    private Integer priority;

    @Schema(description = "优先级名称", example = "高优先级")
    private String priorityName;

    @Schema(description = "严重程度(1-低, 2-中, 3-高, 4-紧急)", example = "3")
    private Integer severity;

    @Schema(description = "严重程度名称", example = "高级")
    private String severityName;

    @Schema(description = "置信度(0.0-1.0)", example = "0.92")
    private BigDecimal confidence;

    @Schema(description = "置信度百分比", example = "92%")
    private String confidencePercent;

    @Schema(description = "事件数据(JSON格式)", example = "{\"faceId\": \"face_001\", \"age\": 25, \"gender\": \"male\"}")
    private String eventData;

    @Schema(description = "视频文件URL", example = "/videos/2025/12/16/ai_event_001.mp4")
    private String videoUrl;

    @Schema(description = "图片文件URL", example = "/images/2025/12/16/ai_event_001.jpg")
    private String imageUrl;

    @Schema(description = "事件发生位置", example = "东门入口A区")
    private String location;

    @Schema(description = "事件状态(1-待处理, 2-已处理, 3-已忽略)", example = "1")
    private Integer eventStatus;

    @Schema(description = "事件状态名称", example = "待处理")
    private String eventStatusName;

    @Schema(description = "处理结果", example = "已确认陌生人，通知安保人员处理")
    private String processResult;

    @Schema(description = "处理时间", example = "2025-12-16T14:35:00")
    private LocalDateTime processTime;

    @Schema(description = "处理次数", example = "1")
    private Integer processTimes;

    @Schema(description = "处理人ID", example = "1001")
    private Long processUserId;

    @Schema(description = "处理人姓名", example = "张三")
    private String processUserName;

    @Schema(description = "创建时间", example = "2025-12-16T14:30:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-12-16T14:35:00")
    private LocalDateTime updateTime;

    @Schema(description = "是否高优先级", example = "true")
    private Boolean isHighPriority;

    @Schema(description = "是否已处理", example = "true")
    private Boolean isProcessed;

    @Schema(description = "处理耗时(秒)", example = "300")
    private Long processDurationSeconds;

    @Schema(description = "处理耗时描述", example = "5分钟")
    private String processDurationDesc;

    @Schema(description = "标签列表", example = "[\"high_priority\", \"security_alert\", \"face_recognition\"]")
    private java.util.List<String> tagList;

    @Schema(description = "相关联的事件ID列表", example = "[1002, 1003]")
    private java.util.List<Long> relatedEventIds;

    @Schema(description = "预警级别", example = "橙色预警")
    private String alertLevel;

    @Schema(description = "预警描述", example = "检测到高危人员，需要立即处理")
    private String alertDescription;

    @Schema(description = "是否需要人工审核", example = "true")
    private Boolean needManualReview;

    @Schema(description = "AI模型版本", example = "v2.1.0")
    private String aiModelVersion;

    @Schema(description = "AI模型名称", example = "FaceRecognitionV2")
    private String aiModelName;

    @Schema(description = "分析耗时(毫秒)", example = "1250")
    private Long analysisDurationMs;

    @Schema(description = "分析耗时描述", example = "1.25秒")
    private String analysisDurationDesc;
}
