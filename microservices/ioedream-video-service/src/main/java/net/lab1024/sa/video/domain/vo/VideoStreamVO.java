package net.lab1024.sa.video.domain.vo;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 视频流视图对象
 * <p>
 * 用于返回视频流信息
 * 严格遵循CLAUDE.md规范：
 * - 使用VO后缀命名
 * - 包含完整的业务字段
 * - 符合企业级VO设计
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Accessors(chain = true)
public class VideoStreamVO {

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
     * <p>
     * main - 主流
     * sub - 子流
     * mobile - 移动流
     * </p>
     */
    private String streamType;

    /**
     * 流类型描述
     */
    private String streamTypeDesc;

    /**
     * 流协议
     * <p>
     * 1 - RTSP
     * 2 - RTMP
     * 3 - HLS
     * 4 - WebRTC
     * 5 - HTTP-FLV
     * </p>
     */
    private Integer protocol;

    /**
     * 流协议描述
     */
    private String protocolDesc;

    /**
     * 流状态
     * <p>
     * 1 - 活跃
     * 2 - 暂停
     * 3 - 停止
     * 4 - 错误
     * </p>
     */
    private Integer streamStatus;

    /**
     * 流状态描述
     */
    private String streamStatusDesc;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 区域名称
     */
    private String areaName;

    /**
     * 视频质量
     * <p>
     * high - 高清
     * medium - 标清
     * low - 流畅
     * </p>
     */
    private String quality;

    /**
     * 视频质量描述
     */
    private String qualityDesc;

    /**
     * 分辨率
     */
    private String resolution;

    /**
     * 帧率
     */
    private Integer frameRate;

    /**
     * 码率（kbps）
     */
    private Integer bitrate;

    /**
     * 编码格式
     */
    private String codec;

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
     * <p>
     * 0 - 不启用
     * 1 - 启用
     * </p>
     */
    private Integer audioEnabled;

    /**
     * 是否启用音频描述
     */
    private String audioEnabledDesc;

    /**
     * 音频编码
     */
    private String audioCodec;

    /**
     * 是否录制中
     * <p>
     * 0 - 未录制
     * 1 - 录制中
     * </p>
     */
    private Integer recording;

    /**
     * 是否录制中描述
     */
    private String recordingDesc;

    /**
     * 观看人数
     */
    private Integer viewerCount;

    /**
     * 最大观看人数
     */
    private Integer maxViewerCount;

    /**
     * 流媒体服务器
     */
    private String streamServer;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 会话超时时间（分钟）
     */
    private Integer sessionTimeout;

    /**
     * 是否启用动态码率
     * <p>
     * 0 - 不启用
     * 1 - 启用
     * </p>
     */
    private Integer dynamicBitrate;

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
     * 创建人ID
     */
    private Long createUserId;

    /**
     * 创建人姓名
     */
    private String createUserName;

    /**
     * 更新人ID
     */
    private Long updateUserId;

    /**
     * 更新人姓名
     */
    private String updateUserName;

    /**
     * 备注
     */
    private String remark;
}