package net.lab1024.sa.video.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 视频录像任务VO
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Data
@Schema(description = "视频录像任务VO")
public class VideoRecordingTaskVO {

    @Schema(description = "任务ID", example = "20001")
    private Long taskId;

    @Schema(description = "计划ID", example = "10001")
    private Long planId;

    @Schema(description = "计划名称", example = "主入口全天录像")
    private String planName;

    @Schema(description = "设备ID", example = "CAM001")
    private String deviceId;

    @Schema(description = "设备名称", example = "主入口摄像头")
    private String deviceName;

    @Schema(description = "通道ID", example = "1")
    private Integer channelId;

    @Schema(description = "任务状态", example = "RUNNING")
    private String status;

    @Schema(description = "任务状态描述", example = "录像中")
    private String statusName;

    @Schema(description = "录像文件路径", example = "/recordings/2025/01/30/cam001_001.mp4")
    private String filePath;

    @Schema(description = "文件大小（字节）", example = "104857600")
    private Long fileSize;

    @Schema(description = "文件大小（可读）", example = "100.00 MB")
    private String fileSizeReadable;

    @Schema(description = "开始时间", example = "2025-01-30 09:00:00")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2025-01-30 18:00:00")
    private LocalDateTime endTime;

    @Schema(description = "录像时长（秒）", example = "32400")
    private Integer durationSeconds;

    @Schema(description = "录像时长（可读）", example = "9小时0分钟")
    private String durationReadable;

    @Schema(description = "触发类型", example = "SCHEDULE")
    private String triggerType;

    @Schema(description = "触发类型描述", example = "定时触发")
    private String triggerTypeName;

    @Schema(description = "触发事件类型", example = "MOTION_DETECTED")
    private String eventTriggerType;

    @Schema(description = "录像质量", example = "HIGH")
    private String quality;

    @Schema(description = "录像质量描述", example = "高质量")
    private String qualityName;

    @Schema(description = "错误信息", example = "设备连接失败")
    private String errorMessage;

    @Schema(description = "重试次数", example = "3")
    private Integer retryCount;

    @Schema(description = "最大重试次数", example = "5")
    private Integer maxRetryCount;

    @Schema(description = "完成时间", example = "2025-01-30 18:05:00")
    private LocalDateTime completedTime;

    @Schema(description = "是否可以重试", example = "true")
    private Boolean canRetry;

    @Schema(description = "是否运行中", example = "true")
    private Boolean isRunning;

    @Schema(description = "是否已完成", example = "false")
    private Boolean isCompleted;

    @Schema(description = "是否失败", example = "false")
    private Boolean isFailed;

    @Schema(description = "创建时间", example = "2025-01-30 08:55:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-01-30 18:05:00")
    private LocalDateTime updateTime;
}
