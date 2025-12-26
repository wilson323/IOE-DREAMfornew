package net.lab1024.sa.video.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI事件添加表单
 * <p>
 * 用于创建AI智能分析事件的请求参数：
 * 1. 基础事件信息
 * 2. 设备和位置信息
 * 3. 事件详细描述
 * 4. 媒体文件路径
 * 5. 优先级和严重性
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Schema(description = "AI事件添加表单")
public class AIEventAddForm {

    @Schema(description = "事件ID（唯一标识）", example = "AI_EVENT_20251216_001")
    @NotBlank(message = "事件ID不能为空")
    @Size(max = 100, message = "事件ID长度不能超过100个字符")
    private String eventId;

    @Schema(description = "设备ID", example = "1001")
    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    @Schema(description = "事件类型", example = "FACE_RECOGNITION")
    @NotBlank(message = "事件类型不能为空")
    @Size(max = 50, message = "事件类型长度不能超过50个字符")
    private String eventType;

    @Schema(description = "事件子类型", example = "STRANGER_DETECTION")
    @Size(max = 50, message = "事件子类型长度不能超过50个字符")
    private String eventSubType;

    @Schema(description = "事件标题", example = "检测到陌生人进入")
    @NotBlank(message = "事件标题不能为空")
    @Size(max = 200, message = "事件标题长度不能超过200个字符")
    private String eventTitle;

    @Schema(description = "事件描述", example = "在区域A1检测到未知人员，识别置信度92%")
    @Size(max = 1000, message = "事件描述长度不能超过1000个字符")
    private String eventDescription;

    @Schema(description = "优先级(1-10)", example = "8")
    @NotNull(message = "优先级不能为空")
    private Integer priority;

    @Schema(description = "严重程度(1-低, 2-中, 3-高, 4-紧急)", example = "3")
    @NotNull(message = "严重程度不能为空")
    private Integer severity;

    @Schema(description = "置信度(0.0-1.0)", example = "0.92")
    @NotNull(message = "置信度不能为空")
    private BigDecimal confidence;

    @Schema(description = "事件数据(JSON格式)", example = "{\"faceId\": \"face_001\", \"age\": 25, \"gender\": \"male\"}")
    private String eventData;

    @Schema(description = "视频文件URL", example = "/videos/2025/12/16/ai_event_001.mp4")
    private String videoUrl;

    @Schema(description = "图片文件URL", example = "/images/2025/12/16/ai_event_001.jpg")
    private String imageUrl;

    @Schema(description = "事件发生位置", example = "东门入口A区")
    @Size(max = 200, message = "事件位置长度不能超过200个字符")
    private String location;

    @Schema(description = "事件发生时间", example = "2025-12-16T14:30:00")
    private LocalDateTime eventTime;

    @Schema(description = "附加标签", example = "[\"high_priority\", \"security_alert\"]")
    private String tags;
}
