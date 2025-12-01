package net.lab1024.sa.admin.module.video.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 视频流实体
 * 严格遵循repowiki规范：视频流信息的数据库实体定义
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_video_stream")
public class VideoStreamEntity extends BaseEntity {

    /**
     * 流ID
     */
    private Long streamId;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 流名称
     */
    private String streamName;

    /**
     * 流类型：LIVE-实时流，PLAYBACK-回放流
     */
    private String streamType;

    /**
     * 流协议：RTSP, HTTP-FLV, WEBRTC
     */
    private String protocol;

    /**
     * 流地址
     */
    private String streamUrl;

    /**
     * 备用流地址
     */
    private String backupStreamUrl;

    /**
     * 流状态：ACTIVE-活跃，INACTIVE-非活跃，ERROR-错误
     */
    private String status;

    /**
     * 视频分辨率
     */
    private String resolution;

    /**
     * 视频帧率
     */
    private Integer frameRate;

    /**
     * 视频码率
     */
    private Long bitrate;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 在线观看人数
     */
    private Integer viewerCount;

    /**
     * 总观看次数
     */
    private Long totalViews;

    /**
     * 录制标志
     */
    private Boolean recordingFlag;

    /**
     * 备注
     */
    private String remark;

    /**
     * 检查流状态是否有效
     *
     * @return 是否有效
     */
    public boolean isValidStatus() {
        return status != null && (
            "ACTIVE".equals(status) ||
            "INACTIVE".equals(status) ||
            "ERROR".equals(status)
        );
    }

    /**
     * 检查流是否正在直播
     *
     * @return 是否正在直播
     */
    public boolean isLive() {
        return "ACTIVE".equals(status);
    }

    /**
     * 检查是否启用录制
     *
     * @return 是否启用录制
     */
    public boolean isRecording() {
        return Boolean.TRUE.equals(recordingFlag);
    }
}