package net.lab1024.sa.video.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 录像视图对象
 * <p>
 * 用于返回录像信息
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
public class VideoRecordingVO {

    /**
     * 录像ID
     */
    private Long recordingId;

    /**
     * 录像文件名
     */
    private String fileName;

    /**
     * 录像文件路径
     */
    private String filePath;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件大小（MB）
     */
    private Double fileSizeMB;

    /**
     * 文件格式
     */
    private String fileFormat;

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
     * 录像类型
     * <p>
     * manual - 手动录像
     * scheduled - 定时录像
     * event - 事件录像
     * alarm - 报警录像
     * </p>
     */
    private String recordingType;

    /**
     * 录像类型描述
     */
    private String recordingTypeDesc;

    /**
     * 录像质量
     * <p>
     * high - 高清
     * medium - 标清
     * low - 流畅
     * </p>
     */
    private String quality;

    /**
     * 录像质量描述
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
     * 音频编码
     */
    private String audioCodec;

    /**
     * 是否启用音频
     */
    private Integer audioEnabled;

    /**
     * 是否启用音频描述
     */
    private String audioEnabledDesc;

    /**
     * 录制开始时间
     */
    private LocalDateTime recordingStartTime;

    /**
     * 录制结束时间
     */
    private LocalDateTime recordingEndTime;

    /**
     * 录制时长（秒）
     */
    private Integer duration;

    /**
     * 录制时长（格式化）
     */
    private String durationFormatted;

    /**
     * 是否重要录像
     */
    private Integer important;

    /**
     * 是否重要录像描述
     */
    private String importantDesc;

    /**
     * 重要标记时间
     */
    private LocalDateTime importantTime;

    /**
     * 重要备注
     */
    private String importantRemark;

    /**
     * 标记人ID
     */
    private Long markerId;

    /**
     * 标记人姓名
     */
    private String markerName;

    /**
     * 事件类型
     */
    private String eventType;

    /**
     * 事件类型描述
     */
    private String eventTypeDesc;

    /**
     * 事件描述
     */
    private String eventDescription;

    /**
     * 事件发生时间
     */
    private LocalDateTime eventTime;

    /**
     * 缩略图路径
     */
    private String thumbnailPath;

    /**
     * 预览视频路径
     */
    private String previewPath;

    /**
     * 存储位置
     */
    private String storageLocation;

    /**
     * 存储服务器
     */
    private String storageServer;

    /**
     * 备份状态
     * <p>
     * 0 - 未备份
     * 1 - 备份中
     * 2 - 已备份
     * 3 - 备份失败
     * </p>
     */
    private Integer backupStatus;

    /**
     * 备份状态描述
     */
    private String backupStatusDesc;

    /**
     * 备份时间
     */
    private LocalDateTime backupTime;

    /**
     * 文件完整性状态
     * <p>
     * 0 - 未检查
     * 1 - 完整
     * 2 - 损坏
     * 3 - 修复中
     * 4 - 已修复
     * </p>
     */
    private Integer integrityStatus;

    /**
     * 完整性状态描述
     */
    private String integrityStatusDesc;

    /**
     * 完整性检查时间
     */
    private LocalDateTime integrityCheckTime;

    /**
     * 下载次数
     */
    private Integer downloadCount;

    /**
     * 播放次数
     */
    private Integer playCount;

    /**
     * 最后播放时间
     */
    private LocalDateTime lastPlayTime;

    /**
     * 最后下载时间
     */
    private LocalDateTime lastDownloadTime;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 区域名称
     */
    private String areaName;

    /**
     * 创建人ID
     */
    private Long createUserId;

    /**
     * 创建人姓名
     */
    private String createUserName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 扩展参数
     */
    private Object extendedParams;
}
