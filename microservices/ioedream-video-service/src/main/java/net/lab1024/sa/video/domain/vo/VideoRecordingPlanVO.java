package net.lab1024.sa.video.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 视频录像计划VO
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Data
@Schema(description = "视频录像计划VO")
public class VideoRecordingPlanVO {

    @Schema(description = "计划ID", example = "10001")
    private Long planId;

    @Schema(description = "计划名称", example = "主入口全天录像")
    private String planName;

    @Schema(description = "计划类型", example = "SCHEDULE")
    private String planType;

    @Schema(description = "计划类型描述", example = "定时录像")
    private String planTypeName;

    @Schema(description = "设备ID", example = "CAM001")
    private String deviceId;

    @Schema(description = "设备名称", example = "主入口摄像头")
    private String deviceName;

    @Schema(description = "通道ID", example = "1")
    private Integer channelId;

    @Schema(description = "录像类型", example = "FULL_TIME")
    private String recordingType;

    @Schema(description = "录像类型描述", example = "全天录像")
    private String recordingTypeName;

    @Schema(description = "录像质量", example = "HIGH")
    private String quality;

    @Schema(description = "录像质量描述", example = "高质量")
    private String qualityName;

    @Schema(description = "分辨率", example = "1080")
    private Integer resolution;

    @Schema(description = "码率", example = "3Mbps")
    private String bitrate;

    @Schema(description = "开始时间", example = "2025-01-30 00:00:00")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2025-01-30 23:59:59")
    private LocalDateTime endTime;

    @Schema(description = "星期设置", example = "1,2,3,4,5")
    private String weekdays;

    @Schema(description = "星期设置描述", example = "工作日")
    private String weekdaysDesc;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "优先级", example = "1")
    private Integer priority;

    @Schema(description = "前置录像时长（秒）", example = "5")
    private Integer preRecordSeconds;

    @Schema(description = "后置录像时长（秒）", example = "10")
    private Integer postRecordSeconds;

    @Schema(description = "事件类型", example = "[\"MOTION_DETECTED\", \"FACE_DETECTED\"]")
    private String eventTypes;

    @Schema(description = "存储位置", example = "/recordings/cam001/")
    private String storageLocation;

    @Schema(description = "最大录像时长（分钟）", example = "480")
    private Integer maxDurationMinutes;

    @Schema(description = "循环录像", example = "true")
    private Boolean loopRecording;

    @Schema(description = "检测区域", example = "{\"polygon\":[[100,200],[300,400],[500,600]]}")
    private String detectionArea;

    @Schema(description = "备注", example = "主入口全天录像计划")
    private String remarks;

    @Schema(description = "创建时间", example = "2025-01-30 10:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-01-30 10:00:00")
    private LocalDateTime updateTime;
}
