package net.lab1024.sa.common.video.entity;

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
}