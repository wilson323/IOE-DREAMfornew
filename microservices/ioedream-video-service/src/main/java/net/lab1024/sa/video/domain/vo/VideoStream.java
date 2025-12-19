package net.lab1024.sa.video.domain.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 视频流对象
 * <p>
 * 表示一个视频流的完整信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Data
@Builder
public class VideoStream {

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 视频流URL
     */
    private String streamUrl;

    /**
     * 协议类型（RTSP、RTMP、HTTP等）
     */
    private String protocol;

    /**
     * 流ID
     */
    private String streamId;

    /**
     * 分辨率宽度
     */
    private Integer width;

    /**
     * 分辨率高度
     */
    private Integer height;

    /**
     * 帧率
     */
    private Integer frameRate;

    /**
     * 码率（kbps）
     */
    private Integer bitrate;
}
