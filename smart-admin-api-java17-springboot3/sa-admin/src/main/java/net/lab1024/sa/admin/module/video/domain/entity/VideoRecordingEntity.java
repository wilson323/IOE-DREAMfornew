package net.lab1024.sa.admin.module.video.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 视频录制实体
 * 严格遵循repowiki规范：视频录制记录的数据库实体定义
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_video_recording")
public class VideoRecordingEntity extends BaseEntity {

    /**
     * 录制ID
     */
    private Long recordingId;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 录制文件路径
     */
    private String filePath;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 录制时长（秒）
     */
    private Integer duration;

    /**
     * 录制状态：RECORDING-录制中，COMPLETED-已完成，FAILED-失败
     */
    private String status;

    /**
     * 录制类型：MANUAL-手动，SCHEDULED-计划，ALARM-告警
     */
    private String recordingType;

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
     * 录制原因
     */
    private String reason;

    /**
     * 备注
     */
    private String remark;

    // Note: createUserId, createTime, updateTime, updateUserId, deletedFlag, version 
    // are inherited from BaseEntity, do not redefine them

    /**
     * 检查录制状态是否有效
     *
     * @return 是否有效
     */
    public boolean isValidStatus() {
        return status != null && (
            "RECORDING".equals(status) ||
            "COMPLETED".equals(status) ||
            "FAILED".equals(status)
        );
    }

    /**
     * 检查录制是否完成
     *
     * @return 是否完成
     */
    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }

    /**
     * 检查录制是否正在进行
     *
     * @return 是否正在录制
     */
    public boolean isRecording() {
        return "RECORDING".equals(status);
    }
}