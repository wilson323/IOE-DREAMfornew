package net.lab1024.sa.video.domain.vo;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 视频流会话视图对象
 * <p>
 * 用于返回视频流会话信息
 * 严格遵循CLAUDE.md规范：
 * - 使用VO后缀命名
 * - 包含会话相关信息
 * - 符合企业级VO设计
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Accessors(chain = true)
public class VideoStreamSessionVO {

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 流ID
     */
    private Long streamId;

    /**
     * 流标识
     */
    private String streamKey;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备编号
     */
    private String deviceCode;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 通道ID
     */
    private Long channelId;

    /**
     * 通道名称
     */
    private String channelName;

    /**
     * 流类型
     */
    private String streamType;

    /**
     * 流协议
     */
    private Integer protocol;

    /**
     * 视频质量
     */
    private String quality;

    /**
     * 分辨率
     */
    private String resolution;

    /**
     * 帧率
     */
    private Integer frameRate;

    /**
     * 码率
     */
    private Integer bitrate;

    /**
     * 流地址列表
     */
    private Map<String, String> streamUrls;

    /**
     * RTSP地址
     */
    private String rtspUrl;

    /**
     * RTMP地址
     */
    private String rtmpUrl;

    /**
     * HLS地址
     */
    private String hlsUrl;

    /**
     * WebRTC地址
     */
    private String webrtcUrl;

    /**
     * HTTP-FLV地址
     */
    private String flvUrl;

    /**
     * 是否启用音频
     */
    private Integer audioEnabled;

    /**
     * 音频编码
     */
    private String audioCodec;

    /**
     * 是否启用录制
     */
    private Integer recordEnabled;

    /**
     * 录制文件路径
     */
    private String recordFilePath;

    /**
     * 录制开始时间
     */
    private LocalDateTime recordStartTime;

    /**
     * 录制时长（秒）
     */
    private Integer recordDuration;

    /**
     * 会话状态
     * <p>
     * active - 活跃
     * paused - 暂停
     * closed - 已关闭
     * error - 错误
     * </p>
     */
    private String sessionStatus;

    /**
     * 会话状态描述
     */
    private String sessionStatusDesc;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 会话超时时间（分钟）
     */
    private Integer sessionTimeout;

    /**
     * 是否启用动态码率
     */
    private Integer dynamicBitrate;

    /**
     * 当前码率
     */
    private Integer currentBitrate;

    /**
     * 网络质量
     * <p>
     * excellent - 优秀
     * good - 良好
     * fair - 一般
     * poor - 较差
     * </p>
     */
    private String networkQuality;

    /**
     * 网络延迟（毫秒）
     */
    private Integer latency;

    /**
     * 丢包率（百分比）
     */
    private Double packetLoss;

    /**
     * 抖动（毫秒）
     */
    private Integer jitter;

    /**
     * 扩展参数
     */
    private Map<String, Object> extendedParams;

    /**
     * 备注
     */
    private String remark;
}