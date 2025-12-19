package net.lab1024.sa.video.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 视频流实体类
 * <p>
 * 视频流管理实体，支持多种流媒体协议和实时传输
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_video_stream")
public class VideoStreamEntity extends BaseEntity {

    /**
     * 流ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long streamId;

    /**
     * 设备ID
     */
    @TableField("device_id")
    private Long deviceId;

    /**
     * 设备编码
     */
    @TableField("device_code")
    private String deviceCode;

    /**
     * 通道号
     */
    @TableField("channel_no")
    private Integer channelNo;

    /**
     * 流名称
     */
    @TableField("stream_name")
    private String streamName;

    /**
     * 流类型：1-主码流 2-子码流 3-移动码流
     */
    @TableField("stream_type")
    private Integer streamType;

    /**
     * 流协议：1-RTSP 2-RTMP 3-HLS 4-WebRTC 5-HTTP-FLV
     */
    @TableField("protocol")
    private Integer protocol;

    /**
     * 流地址
     */
    @TableField("stream_url")
    private String streamUrl;

    /**
     * 拉流地址
     */
    @TableField("pull_url")
    private String pullUrl;

    /**
     * 推流地址
     */
    @TableField("push_url")
    private String pushUrl;

    /**
     * 转码配置（JSON格式）
     */
    @TableField("transcode_config")
    private String transcodeConfig;

    /**
     * 分辨率
     */
    @TableField("resolution")
    private String resolution;

    /**
     * 帧率
     */
    @TableField("frame_rate")
    private Integer frameRate;

    /**
     * 码率（Kbps）
     */
    @TableField("bitrate")
    private Integer bitrate;

    /**
     * 编码格式：1-H264 2-H265 3-MJPEG
     */
    @TableField("codec_format")
    private Integer codecFormat;

    /**
     * 音频编码：1-AAC 2-G711A 3-G711U 4-PCM
     */
    @TableField("audio_codec")
    private Integer audioCodec;

    /**
     * 流状态：1-正常 2-中断 3-异常 4-停止
     */
    @TableField("stream_status")
    private Integer streamStatus;

    /**
     * 是否录制：0-不录制 1-录制
     */
    @TableField("record_enabled")
    private Integer recordEnabled;

    /**
     * 是否智能分析：0-不分析 1-分析
     */
    @TableField("analysis_enabled")
    private Integer analysisEnabled;

    /**
     * 观看人数
     */
    @TableField("viewer_count")
    private Integer viewerCount;

    /**
     * 最大观看人数
     */
    @TableField("max_viewers")
    private Integer maxViewers;

    /**
     * 流质量：1-流畅 2-标准 3-高清 4-超清 5-4K
     */
    @TableField("stream_quality")
    private Integer streamQuality;

    /**
     * 延迟时间（毫秒）
     */
    @TableField("latency")
    private Integer latency;

    /**
     * 丢包率（百分比）
     */
    @TableField("packet_loss")
    private Double packetLoss;

    /**
     * 带宽占用（Kbps）
     */
    @TableField("bandwidth_usage")
    private Integer bandwidthUsage;

    /**
     * 开始时间
     */
    @TableField("start_time")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @TableField("end_time")
    private LocalDateTime endTime;

    /**
     * 断流次数
     */
    @TableField("disconnect_count")
    private Integer disconnectCount;

    /**
     * 最后断流时间
     */
    @TableField("last_disconnect_time")
    private LocalDateTime lastDisconnectTime;

    /**
     * 重连时间
     */
    @TableField("reconnect_time")
    private LocalDateTime reconnectTime;

    /**
     * 录制文件路径
     */
    @TableField("record_path")
    private String recordPath;

    /**
     * 缩略图路径
     */
    @TableField("thumbnail_path")
    private String thumbnailPath;

    /**
     * 访问权限：1-公开 2-授权 3-私有
     */
    @TableField("access_permission")
    private Integer accessPermission;

    /**
     * 访问密码（加密存储）
     */
    @TableField("access_password")
    private String accessPassword;

    /**
     * 流标签（JSON数组）
     */
    @TableField("tags")
    private String tags;

    /**
     * 流描述
     */
    @TableField("description")
    private String description;

    /**
     * 扩展属性（JSON格式）
     */
    @TableField("extended_attributes")
    private String extendedAttributes;

    // ==================== 兼容性字段/方法（历史代码迁移，避免编译阻塞） ====================

    /**
     * 流Key（历史字段，当前表结构未持久化）
     */
    @TableField(exist = false)
    private String streamKey;

    /**
     * 音频启用（历史字段，当前表结构未持久化）
     */
    @TableField(exist = false)
    private Integer audioEnabled;

    /**
     * 录制时长（历史字段，当前表结构未持久化）
     */
    @TableField(exist = false)
    private Integer recordDuration;

    /**
     * 客户端IP（历史字段，当前表结构未持久化）
     */
    @TableField(exist = false)
    private String clientIp;

    /**
     * UserAgent（历史字段，当前表结构未持久化）
     */
    @TableField(exist = false)
    private String userAgent;

    /**
     * 会话超时（历史字段，当前表结构未持久化）
     */
    @TableField(exist = false)
    private Integer sessionTimeout;

    /**
     * 动态码率（历史字段，当前表结构未持久化）
     */
    @TableField(exist = false)
    private Integer dynamicBitrate;

    /**
     * 会话ID（历史字段，当前表结构未持久化）
     */
    @TableField(exist = false)
    private String sessionId;

    /**
     * 录制开始时间（历史字段，当前表结构未持久化）
     */
    @TableField(exist = false)
    private LocalDateTime recordStartTime;

    public String getStreamKey() {
        return streamKey;
    }

    public void setStreamKey(String streamKey) {
        this.streamKey = streamKey;
    }

    /**
     * 兼容历史字段：channelId（Long）→ channelNo（Integer）
     */
    public Long getChannelId() {
        return this.channelNo == null ? null : this.channelNo.longValue();
    }

    public void setChannelId(Long channelId) {
        this.channelNo = channelId == null ? null : channelId.intValue();
    }

    public void setChannelId(long channelId) {
        this.channelNo = (int) channelId;
    }

    /**
     * 兼容历史字段：streamType（String）→ streamType（Integer）
     * <p>
     * 支持：main/sub/mobile；无法识别时默认主码流（1）。
     * </p>
     */
    public void setStreamType(String streamType) {
        if (streamType == null) {
            this.streamType = 1;
            return;
        }
        String type = streamType.trim().toLowerCase();
        switch (type) {
            case "sub":
                this.streamType = 2;
                break;
            case "mobile":
                this.streamType = 3;
                break;
            case "main":
            default:
                this.streamType = 1;
                break;
        }
    }

    /**
     * 兼容历史字段：quality（String）→ streamQuality（Integer）
     * <p>
     * 支持：low/medium/high/4k；无法识别时默认标准（2）。
     * </p>
     */
    public void setQuality(String quality) {
        if (quality == null) {
            this.streamQuality = 2;
            return;
        }
        String q = quality.trim().toLowerCase();
        switch (q) {
            case "low":
                this.streamQuality = 1;
                break;
            case "high":
                this.streamQuality = 3;
                break;
            case "4k":
                this.streamQuality = 5;
                break;
            case "medium":
            default:
                this.streamQuality = 2;
                break;
        }
    }

    public Integer getAudioEnabled() {
        return audioEnabled;
    }

    public void setAudioEnabled(int audioEnabled) {
        this.audioEnabled = audioEnabled;
    }

    public Integer getRecordDuration() {
        return recordDuration;
    }

    public void setRecordDuration(Integer recordDuration) {
        this.recordDuration = recordDuration;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Integer getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public Integer getDynamicBitrate() {
        return dynamicBitrate;
    }

    public void setDynamicBitrate(int dynamicBitrate) {
        this.dynamicBitrate = dynamicBitrate;
    }

    /**
     * 兼容历史字段：maxViewerCount → maxViewers
     */
    public void setMaxViewerCount(int maxViewerCount) {
        this.maxViewers = maxViewerCount;
    }

    /**
     * 获取会话ID
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * 设置会话ID
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * 获取质量（String格式，兼容历史代码）
     * <p>
     * 将 streamQuality（Integer）转换为 String 格式
     * </p>
     */
    public String getQuality() {
        if (streamQuality == null) {
            return "medium";
        }
        switch (streamQuality) {
            case 1:
                return "low";
            case 3:
                return "high";
            case 4:
                return "ultra";
            case 5:
                return "4k";
            case 2:
            default:
                return "medium";
        }
    }

    /**
     * 获取录制状态（兼容历史代码）
     * <p>
     * 返回 recordEnabled 的值
     * </p>
     */
    public Integer getRecording() {
        return recordEnabled;
    }

    /**
     * 设置录制状态（兼容历史代码）
     */
    public void setRecording(Integer recording) {
        this.recordEnabled = recording;
    }

    /**
     * 获取录制开始时间
     */
    public LocalDateTime getRecordStartTime() {
        return recordStartTime;
    }

    /**
     * 设置录制开始时间
     */
    public void setRecordStartTime(LocalDateTime recordStartTime) {
        this.recordStartTime = recordStartTime;
    }
}
