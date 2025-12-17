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
 * 视频录像实体类
 * <p>
 * 视频监控录像记录管理实体，支持录像检索、回放和智能分析
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_video_record")
public class VideoRecordEntity extends BaseEntity {

    /**
     * 录像ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long recordId;

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
     * 设备名称
     */
    @TableField("device_name")
    private String deviceName;

    /**
     * 录像文件名称
     */
    @TableField("file_name")
    private String fileName;

    /**
     * 录像文件路径
     */
    @TableField("file_path")
    private String filePath;

    /**
     * 录像文件大小（字节）
     */
    @TableField("file_size")
    private Long fileSize;

    /**
     * 录像开始时间
     */
    @TableField("start_time")
    private LocalDateTime startTime;

    /**
     * 录像结束时间
     */
    @TableField("end_time")
    private LocalDateTime endTime;

    /**
     * 录像时长（秒）
     */
    @TableField("duration")
    private Long duration;

    /**
     * 录像类型：1-计划录像 2-手动录像 3-报警录像 4-移动侦测录像
     */
    @TableField("record_type")
    private Integer recordType;

    /**
     * 录像质量：1-流畅 2-标准 3-高清 4-超清
     */
    @TableField("record_quality")
    private Integer recordQuality;

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
     * 编码格式：1-H264 2-H265 3-MJPEG 4-MPEG4
     */
    @TableField("codec_format")
    private Integer codecFormat;

    /**
     * 是否包含音频：0-无音频 1-有音频
     */
    @TableField("has_audio")
    private Integer hasAudio;

    /**
     * 音频编码格式
     */
    @TableField("audio_codec")
    private String audioCodec;

    /**
     * 存储位置：1-本地存储 2-网络存储 3-云存储
     */
    @TableField("storage_location")
    private Integer storageLocation;

    /**
     * 存储服务器地址
     */
    @TableField("storage_server")
    private String storageServer;

    /**
     * 备份状态：0-未备份 1-备份中 2-备份完成 3-备份失败
     */
    @TableField("backup_status")
    private Integer backupStatus;

    /**
     * 备份时间
     */
    @TableField("backup_time")
    private LocalDateTime backupTime;

    /**
     * 预览缩略图
     */
    @TableField("thumbnail_path")
    private String thumbnailPath;

    /**
     * 录像状态：1-正常 2-损坏 3-删除
     */
    @TableField("record_status")
    private Integer recordStatus;

    /**
     * 访问次数
     */
    @TableField("access_count")
    private Integer accessCount;

    /**
     * 最后访问时间
     */
    @TableField("last_access_time")
    private LocalDateTime lastAccessTime;

    /**
     * 访问权限：1-公开 2-限制 3-私有
     */
    @TableField("access_permission")
    private Integer accessPermission;

    /**
     * 关联事件ID（如报警事件）
     */
    @TableField("event_id")
    private String eventId;

    /**
     * 关联事件类型：1-门禁事件 2-报警事件 3-异常事件
     */
    @TableField("event_type")
    private Integer eventType;

    /**
     * 事件描述
     */
    @TableField("event_description")
    private String eventDescription;

    /**
     * 标签（JSON数组）
     */
    @TableField("tags")
    private String tags;

    /**
     * 录像描述
     */
    @TableField("description")
    private String description;

    /**
     * 过期时间
     */
    @TableField("expire_time")
    private LocalDateTime expireTime;

    /**
     * 扩展信息（JSON格式）
     */
    @TableField("extended_info")
    private String extendedInfo;
}
