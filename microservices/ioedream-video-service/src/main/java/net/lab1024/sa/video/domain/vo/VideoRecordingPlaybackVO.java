package net.lab1024.sa.video.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 录像播放视图对象
 * <p>
 * 用于返回录像播放信息
 * 严格遵循CLAUDE.md规范：
 * - 使用VO后缀命名
 * - 包含播放相关信息
 * - 支持多协议播放
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Accessors(chain = true)
public class VideoRecordingPlaybackVO {

    /**
     * 录像ID
     */
    private Long recordingId;

    /**
     * 录像文件名
     */
    private String fileName;

    /**
     * 播放令牌
     */
    private String playbackToken;

    /**
     * 播放令牌过期时间
     */
    private LocalDateTime tokenExpireTime;

    /**
     * 原始播放地址
     */
    private String originalPlayUrl;

    /**
     * HLS播放地址
     */
    private String hlsPlayUrl;

    /**
     * DASH播放地址
     */
    private String dashPlayUrl;

    /**
     * WebM播放地址
     */
    private String webmPlayUrl;

    /**
     * MP4播放地址
     */
    private String mp4PlayUrl;

    /**
     * FLV播放地址
     */
    private String flvPlayUrl;

    /**
     * RTMP播放地址
     */
    private String rtmpPlayUrl;

    /**
     * 播放地址列表
     */
    private Map<String, String> playUrls;

    /**
     * 支持的播放协议
     */
    private List<String> supportedProtocols;

    /**
     * 推荐播放协议
     */
    private String recommendedProtocol;

    /**
     * 视频时长（秒）
     */
    private Integer duration;

    /**
     * 视频时长（格式化）
     */
    private String durationFormatted;

    /**
     * 视频分辨率
     */
    private String resolution;

    /**
     * 视频质量
     */
    private String quality;

    /**
     * 视频码率（kbps）
     */
    private Integer bitrate;

    /**
     * 视频帧率
     */
    private Integer frameRate;

    /**
     * 视频编码格式
     */
    private String videoCodec;

    /**
     * 音频编码格式
     */
    private String audioCodec;

    /**
     * 是否包含音频
     */
    private Boolean hasAudio;

    /**
     * 字幕信息
     */
    private List<Map<String, Object>> subtitleInfo;

    /**
     * 章节信息
     */
    private List<Map<String, Object>> chapterInfo;

    /**
     * 预加载图片URL
     */
    private String posterUrl;

    /**
     * 缩略图URL
     */
    private String thumbnailUrl;

    /**
     * 预览片段URL
     */
    private String previewUrl;

    /**
     * 播放权限级别
     */
    private Integer playbackPermissionLevel;

    /**
     * 播放权限级别描述
     */
    private String playbackPermissionDesc;

    /**
     * 最大播放次数限制
     */
    private Integer maxPlayCount;

    /**
     * 剩余播放次数
     */
    private Integer remainingPlayCount;

    /**
     * 播放开始时间限制
     */
    private LocalDateTime playbackStartTimeLimit;

    /**
     * 播放结束时间限制
     */
    private LocalDateTime playbackEndTimeLimit;

    /**
     * 允许的IP地址列表
     */
    private List<String> allowedIpAddresses;

    /**
     * 禁止的IP地址列表
     */
    private List<String> forbiddenIpAddresses;

    /**
     * 播放水印信息
     */
    private String playbackWatermark;

    /**
     * 播放加密信息
     */
    private Map<String, Object> playbackEncryption;

    /**
     * 播放速度控制
     */
    private List<Double> playbackSpeeds;

    /**
     * 支持的播放功能
     */
    private List<String> supportedFeatures;

    /**
     * 时间轴标记
     */
    private List<Map<String, Object>> timelineMarkers;

    /**
     * 关键事件时间点
     */
    private List<Map<String, Object>> keyEvents;

    /**
     * 智能分析结果
     */
    private Map<String, Object> analysisResults;

    /**
     * 版权信息
     */
    private String copyrightInfo;

    /**
     * 使用条款
     */
    private String usageTerms;

    /**
     * 播放统计信息
     */
    private Map<String, Object> playbackStatistics;

    /**
     * 缓存策略
     */
    private Map<String, Object> cacheStrategy;

    /**
     * CDN信息
     */
    private Map<String, Object> cdnInfo;

    /**
     * 服务器信息
     */
    private Map<String, Object> serverInfo;

    /**
     * 网络要求
     */
    private Map<String, Object> networkRequirements;

    /**
     * 播放器配置
     */
    private Map<String, Object> playerConfiguration;

    /**
     * 错误处理信息
     */
    private Map<String, Object> errorHandling;

    /**
     * 扩展播放参数
     */
    private Map<String, Object> extendedPlaybackParams;

    /**
     * 播放会话ID
     */
    private String playbackSessionId;

    /**
     * 播放会话创建时间
     */
    private LocalDateTime sessionCreateTime;

    /**
     * 播放会话过期时间
     */
    private LocalDateTime sessionExpireTime;

    /**
     * 播放日志记录
     */
    private Boolean enablePlaybackLogging;

    /**
     * 播放分析数据收集
     */
    private Boolean enableAnalyticsCollection;

    /**
     * 播放质量监控
     */
    private Boolean enableQualityMonitoring;

    /**
     * 自定义播放器参数
     */
    private Map<String, Object> customPlayerParams;
}
