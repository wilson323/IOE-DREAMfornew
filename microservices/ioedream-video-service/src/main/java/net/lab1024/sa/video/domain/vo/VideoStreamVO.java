package net.lab1024.sa.video.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * 视频流视图对象
 *
 * @author SmartAdmin Team
 * @version 1.0
 * @since 2025-11-16
 */
@Data
@Schema(description = "视频流视图对象")
public class VideoStreamVO {

    /**
     * 视频流ID
     */
    @Schema(description = "视频流ID", example = "1")
    private Long streamId;

    /**
     * 设备ID
     */
    @Schema(description = "设备ID", example = "1")
    private Long deviceId;

    /**
     * 设备编码
     */
    @Schema(description = "设备编码", example = "CAM001")
    private String deviceCode;

    /**
     * 设备名称
     */
    @Schema(description = "设备名称", example = "主入口摄像头")
    private String deviceName;

    /**
     * 流媒体类型
     */
    @Schema(description = "流媒体类型", example = "RTSP")
    private String streamType;

    /**
     * 流媒体URL
     */
    @Schema(description = "流媒体URL", example = "rtsp://192.168.1.100:554/stream")
    private String streamUrl;

    /**
     * 流媒体状态
     */
    @Schema(description = "流媒体状态", example = "ACTIVE")
    private String streamStatus;

    /**
     * 流媒体状态描述
     */
    @Schema(description = "流媒体状态描述", example = "活跃")
    private String streamStatusDesc;

    /**
     * 分辨率
     */
    @Schema(description = "分辨率", example = "1920x1080")
    private String resolution;

    /**
     * 帧率
     */
    @Schema(description = "帧率", example = "25")
    private Integer frameRate;

    /**
     * 码率(kbps)
     */
    @Schema(description = "码率(kbps)", example = "2048")
    private Integer bitrate;

    /**
     * 编码格式
     */
    @Schema(description = "编码格式", example = "H.264")
    private String encodingFormat;

    /**
     * 音频支持
     */
    @Schema(description = "音频支持", example = "1")
    private Integer audioSupported;

    /**
     * 音频编码
     */
    @Schema(description = "音频编码", example = "AAC")
    private String audioEncoding;

    /**
     * 是否录制
     */
    @Schema(description = "是否录制", example = "1")
    private Integer isRecording;

    /**
     * 录像文件路径
     */
    @Schema(description = "录像文件路径", example = "/recordings/2025/01/15/")
    private String recordPath;

    /**
     * 预览截图URL
     */
    @Schema(description = "预览截图URL", example = "http://server.com/snapshots/cam001.jpg")
    private String snapshotUrl;

    /**
     * 最后访问时间
     */
    @Schema(description = "最后访问时间", example = "2025-01-15 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastAccessTime;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2025-01-01 09:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2025-01-15 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 通道号
     */
    @Schema(description = "通道号", example = "1")
    private Integer channel;

    /**
     * 格式
     */
    @Schema(description = "格式", example = "HLS")
    private String format;
}