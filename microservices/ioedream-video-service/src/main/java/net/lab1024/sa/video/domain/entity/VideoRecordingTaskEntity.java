package net.lab1024.sa.video.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 录像任务实体
 * <p>
 * 记录像像执行任务的状态和结果
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_video_recording_task")
@Schema(description = "录像任务实体")
public class VideoRecordingTaskEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "任务ID", example = "20001")
    private Long taskId;

    @Schema(description = "计划ID", example = "10001")
    private Long planId;

    @Schema(description = "设备ID", example = "CAM001")
    private String deviceId;

    @Schema(description = "通道ID", example = "1")
    private Integer channelId;

    @Schema(description = "任务状态", example = "RUNNING", allowableValues = {"PENDING", "RUNNING", "STOPPED", "FAILED", "COMPLETED"})
    private Integer status;

    @Schema(description = "录像文件路径", example = "/recordings/2025/01/30/cam001_001.mp4")
    private String filePath;

    @Schema(description = "文件大小(字节)", example = "104857600")
    private Long fileSize;

    @Schema(description = "开始时间", example = "2025-01-30 09:00:00")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2025-01-30 18:00:00")
    private LocalDateTime endTime;

    @Schema(description = "录像时长(秒)", example = "32400")
    private Integer durationSeconds;

    @Schema(description = "触发类型", example = "SCHEDULE", allowableValues = {"SCHEDULE", "MANUAL", "EVENT"})
    private Integer triggerType;

    @Schema(description = "触发事件类型", example = "MOTION_DETECTED")
    private String eventTriggerType;

    @Schema(description = "录像质量", example = "HIGH")
    private Integer quality;

    @Schema(description = "错误信息", example = "设备连接失败")
    private String errorMessage;

    @Schema(description = "重试次数", example = "3")
    private Integer retryCount;

    @Schema(description = "最大重试次数", example = "5")
    private Integer maxRetryCount;

    @Schema(description = "完成时间", example = "2025-01-30 18:05:00")
    private LocalDateTime completedTime;

    @Schema(description = "最大录像时长(分钟)", example = "480")
    private Integer maxDurationMinutes;

    @Schema(description = "备注", example = "正常完成")
    private String remarks;

    /**
     * 任务状态枚举
     */
    public enum TaskStatus {
        PENDING(0, "待执行"),
        RUNNING(1, "录像中"),
        STOPPED(2, "已停止"),
        FAILED(3, "失败"),
        COMPLETED(4, "已完成");

        private final int code;
        private final String description;

        TaskStatus(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public int getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static TaskStatus fromCode(int code) {
            for (TaskStatus status : values()) {
                if (status.code == code) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Invalid task status code: " + code);
        }
    }

    /**
     * 触发类型枚举
     */
    public enum TriggerType {
        SCHEDULE(1, "定时触发"),
        MANUAL(2, "手动触发"),
        EVENT(3, "事件触发");

        private final int code;
        private final String description;

        TriggerType(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public int getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static TriggerType fromCode(int code) {
            for (TriggerType type : values()) {
                if (type.code == code) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Invalid trigger type code: " + code);
        }
    }

    /**
     * 判断任务是否运行中
     */
    public boolean isRunning() {
        return status != null && status == TaskStatus.RUNNING.getCode();
    }

    /**
     * 判断任务是否已完成
     */
    public boolean isCompleted() {
        return status != null && status == TaskStatus.COMPLETED.getCode();
    }

    /**
     * 判断任务是否失败
     */
    public boolean isFailed() {
        return status != null && status == TaskStatus.FAILED.getCode();
    }

    /**
     * 判断是否可以重试
     */
    public boolean canRetry() {
        return isFailed() && retryCount != null && maxRetryCount != null
                && retryCount < maxRetryCount;
    }
}
