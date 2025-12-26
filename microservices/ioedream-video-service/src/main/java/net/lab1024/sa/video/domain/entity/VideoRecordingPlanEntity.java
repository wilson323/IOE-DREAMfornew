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
 * 录像计划实体
 * <p>
 * 管理视频录像的计划任务，支持：
 * - 定时录像：按时间表自动录像
 * - 事件录像：触发事件时录像
 * - 手动录像：用户手动控制
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_video_recording_plan")
@Schema(description = "录像计划实体")
public class VideoRecordingPlanEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "计划ID", example = "10001")
    private Long planId;

    @Schema(description = "计划名称", example = "主入口全天录像")
    private String planName;

    @Schema(description = "计划类型", example = "SCHEDULE", allowableValues = {"SCHEDULE", "EVENT", "MANUAL"})
    private Integer planType;

    @Schema(description = "设备ID", example = "CAM001")
    private String deviceId;

    @Schema(description = "通道ID", example = "1")
    private Integer channelId;

    @Schema(description = "录像类型", example = "FULL_TIME", allowableValues = {"FULL_TIME", "TIMED", "EVENT_TRIGGERED"})
    private Integer recordingType;

    @Schema(description = "录像质量", example = "HIGH", allowableValues = {"LOW", "MEDIUM", "HIGH", "ULTRA"})
    private Integer quality;

    @Schema(description = "开始时间", example = "2025-01-30 00:00:00")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2025-01-30 23:59:59")
    private LocalDateTime endTime;

    @Schema(description = "星期设置", example = "1,2,3,4,5,6,7")
    private String weekdays;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "优先级", example = "1")
    private Integer priority;

    @Schema(description = "前置录像时长(秒)", example = "5")
    private Integer preRecordSeconds;

    @Schema(description = "后置录像时长(秒)", example = "10")
    private Integer postRecordSeconds;

    @Schema(description = "事件类型(JSON数组)", example = "[\"MOTION_DETECTED\", \"FACE_DETECTED\"]")
    private String eventTypes;

    @Schema(description = "存储位置", example = "/recordings/cam001/")
    private String storageLocation;

    @Schema(description = "最大录像时长(分钟)", example = "480")
    private Integer maxDurationMinutes;

    @Schema(description = "循环录像", example = "true")
    private Boolean loopRecording;

    @Schema(description = "检测区域(JSON格式)", example = "{\"polygon\":[[100,200],[300,400],[500,600]]}")
    private String detectionArea;

    @Schema(description = "备注", example = "主入口全天录像计划")
    private String remarks;

    /**
     * 计划类型枚举
     */
    public enum PlanType {
        SCHEDULE(1, "定时录像"),
        EVENT(2, "事件录像"),
        MANUAL(3, "手动录像");

        private final int code;
        private final String description;

        PlanType(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public int getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static PlanType fromCode(int code) {
            for (PlanType type : values()) {
                if (type.code == code) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Invalid plan type code: " + code);
        }
    }

    /**
     * 录像类型枚举
     */
    public enum RecordingType {
        FULL_TIME(1, "全天录像"),
        TIMED(2, "定时录像"),
        EVENT_TRIGGERED(3, "事件触发录像");

        private final int code;
        private final String description;

        RecordingType(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public int getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static RecordingType fromCode(int code) {
            for (RecordingType type : values()) {
                if (type.code == code) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Invalid recording type code: " + code);
        }
    }

    /**
     * 录像质量枚举
     */
    public enum RecordingQuality {
        LOW(1, "低质量", 360, "500Kbps"),
        MEDIUM(2, "中等质量", 720, "1.5Mbps"),
        HIGH(3, "高质量", 1080, "3Mbps"),
        ULTRA(4, "超清质量", 2160, "8Mbps");

        private final int code;
        private final String description;
        private final int resolution;
        private final String bitrate;

        RecordingQuality(int code, String description, int resolution, String bitrate) {
            this.code = code;
            this.description = description;
            this.resolution = resolution;
            this.bitrate = bitrate;
        }

        public int getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public int getResolution() {
            return resolution;
        }

        public String getBitrate() {
            return bitrate;
        }

        public static RecordingQuality fromCode(int code) {
            for (RecordingQuality quality : values()) {
                if (quality.code == code) {
                    return quality;
                }
            }
            throw new IllegalArgumentException("Invalid recording quality code: " + code);
        }
    }
}
