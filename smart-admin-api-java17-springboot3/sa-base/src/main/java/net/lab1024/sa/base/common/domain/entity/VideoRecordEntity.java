package net.lab1024.sa.base.common.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import net.lab1024.sa.base.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 视频记录实体
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class VideoRecordEntity extends BaseEntity {

    /**
     * 记录ID
     */
    private Long recordId;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备编码
     */
    private String deviceCode;

    /**
     * 录像开始时间
     */
    private LocalDateTime recordStartTime;

    /**
     * 录像结束时间
     */
    private LocalDateTime recordEndTime;

    /**
     * 录像时长(秒)
     */
    private Integer recordDuration;

    /**
     * 录像类型 (MANUAL-手动, SCHEDULE-定时, EVENT-事件)
     */
    private String recordType;

    /**
     * 录像文件路径
     */
    private String recordFilePath;

    /**
     * 录像文件名
     */
    private String recordFileName;

    /**
     * 录像文件大小(字节)
     */
    private Long recordFileSize;

    /**
     * 录像质量 (HIGH-高, MEDIUM-中, LOW-低)
     */
    private String recordQuality;

    /**
     * 分辨率
     */
    private String resolution;

    /**
     * 帧率
     */
    private Integer frameRate;

    /**
     * 视频格式
     */
    private String videoFormat;

    /**
     * 视频编码格式
     */
    private String videoCodec;

    /**
     * 音频编码格式
     */
    private String audioCodec;

    /**
     * 事件类型 (如果有)
     */
    private String eventType;

    /**
     * 事件描述
     */
    private String eventDescription;

    /**
     * 是否已备份 (0-未备份, 1-已备份)
     */
    private Integer isBackedUp;

    /**
     * 备份时间
     */
    private LocalDateTime backupTime;

    /**
     * 备份路径
     */
    private String backupPath;

    /**
     * 是否加密 (0-未加密, 1-已加密)
     */
    private Integer isEncrypted;

    /**
     * 缩略图路径
     */
    private String thumbnailPath;

    /**
     * 录像状态 (RECORDING-录制中, COMPLETED-已完成, FAILED-失败)
     */
    private String recordStatus;

    /**
     * 备注
     */
    private String remark;
}