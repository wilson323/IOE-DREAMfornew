package net.lab1024.sa.video.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 边缘AI事件数据传输对象
 * <p>
 * 用于接收边缘设备上报的AI识别事件，并通过WebSocket推送到前端
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "边缘AI事件")
public class EdgeAIEventDTO {

    @Schema(description = "事件ID（唯一标识）", example = "evt_20250130_120000_001")
    @NotBlank(message = "事件ID不能为空")
    private String eventId;

    @Schema(description = "设备ID", example = "CAM001")
    @NotBlank(message = "设备ID不能为空")
    private String deviceId;

    @Schema(description = "设备名称", example = "A栋1楼大厅摄像头")
    private String deviceName;

    @Schema(description = "事件类型", example = "FACE_DETECTED")
    @NotBlank(message = "事件类型不能为空")
    private String eventType;

    /**
     * 支持的事件类型：
     * - FACE_DETECTED: 人脸检测
     * - FACE_RECOGNIZED: 人脸识别
     * - PERSON_COUNT: 人数统计
     * - ABNORMAL_BEHAVIOR: 异常行为
     * - AREA_INTRUSION: 区域入侵
     * - LOITERING_DETECTED: 徘徊检测
     * - FALL_DETECTED: 跌倒检测
     * - FIGHT_DETECTED: 打架检测
     */
    @Schema(description = "事件数据(JSON格式，存储事件详细信息)", example = "{\"faceId\":123, \"age\":25, \"gender\":\"male\"}")
    private Map<String, Object> eventData;

    @Schema(description = "置信度(0-1)", example = "0.95")
    @NotNull(message = "置信度不能为空")
    @DecimalMin(value = "0.0", message = "置信度最小值为0")
    @DecimalMax(value = "1.0", message = "置信度最大值为1")
    private BigDecimal confidence;

    @Schema(description = "事件时间戳", example = "2025-01-30T12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventTime;

    @Schema(description = "事件时间戳(Unix毫秒)", example = "1706592000000")
    private Long timestamp;

    @Schema(description = "抓拍图片URL", example = "https://minio.example.com/video-events/2025/01/30/evt_001.jpg")
    private String imageUrl;

    @Schema(description = "区域ID", example = "2001")
    private Long areaId;

    @Schema(description = "区域名称", example = "A栋1楼大厅")
    private String areaName;

    @Schema(description = "经度", example = "116.397128")
    private BigDecimal longitude;

    @Schema(description = "纬度", example = "39.916527")
    private BigDecimal latitude;

    /**
     * 创建事件时间戳（自动设置）
     *
     * @return 当前时间戳
     */
    public Long getTimestamp() {
        if (timestamp == null && eventTime != null) {
            timestamp = eventTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
        return timestamp;
    }
}
