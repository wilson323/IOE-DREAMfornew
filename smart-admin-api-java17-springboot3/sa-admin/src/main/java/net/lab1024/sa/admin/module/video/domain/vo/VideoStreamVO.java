package net.lab1024.sa.admin.module.video.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 视频流VO
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */

@Schema(description = "视频流信息")
public class VideoStreamVO {

    @Schema(description = "视频流ID")
    private Long streamId;

    @Schema(description = "通道号")
    private Integer channel;

    @Schema(description = "视频格式")
    private String format;

    @Schema(description = "设备ID")
    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "流地址")
    @NotBlank(message = "流地址不能为空")
    private String streamUrl;

    @Schema(description = "流类型")
    private String streamType;

    @Schema(description = "分辨率")
    private String resolution;

    @Schema(description = "帧率")
    private Integer frameRate;

    @Schema(description = "码率(kbps)")
    private Integer bitrate;

    @Schema(description = "是否在线")
    private Boolean online;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}