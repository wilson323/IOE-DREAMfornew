package net.lab1024.sa.base.common.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import net.lab1024.sa.base.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 监控事件实体
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class MonitorEventEntity extends BaseEntity {

    /**
     * 事件ID
     */
    private Long eventId;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备编码
     */
    private String deviceCode;

    /**
     * 事件类型 (MOTION-移动检测, FACE_DETECT-人脸检测, INTRUSION-入侵检测, LINE_CROSS-越线检测)
     */
    private String eventType;

    /**
     * 事件级别 (LOW-低, MEDIUM-中, HIGH-高, CRITICAL-紧急)
     */
    private String eventLevel;

    /**
     * 事件发生时间
     */
    private LocalDateTime eventTime;

    /**
     * 事件描述
     */
    private String eventDescription;

    /**
     * 事件图片路径(如果有截图)
     */
    private String eventImagePath;

    /**
     * 事件视频片段路径
     */
    private String eventVideoPath;

    /**
     * 检测到的目标数量
     */
    private Integer targetCount;

    /**
     * 目标类型 (PERSON-人员, VEHICLE-车辆, OBJECT-物体)
     */
    private String targetType;

    /**
     * 事件区域坐标
     */
    private String eventArea;

    /**
     * 置信度 (0-100)
     */
    private Integer confidence;

    /**
     * 事件状态 (PENDING-待处理, PROCESSING-处理中, COMPLETED-已完成, IGNORED-已忽略)
     */
    private String eventStatus;

    /**
     * 是否已处理 (0-未处理, 1-已处理, 2-忽略)
     */
    private Integer isHandled;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    /**
     * 处理人ID
     */
    private Long handleUserId;

    /**
     * 处理意见
     */
    private String handleComment;

    /**
     * 是否已通知 (0-未通知, 1-已通知)
     */
    private Integer isNotified;

    /**
     * 通知时间
     */
    private LocalDateTime notifyTime;

    /**
     * 通知方式 (EMAIL-邮件, SMS-短信, PUSH-推送)
     */
    private String notifyMethod;

    /**
     * 关联录像ID
     */
    private Long relatedRecordId;

    /**
     * 纬度
     */
    private Double longitude;

    /**
     * 纬度
     */
    private Double latitude;

    /**
     * 设备快照(事件发生时的图片)
     */
    private String deviceSnapshot;

    /**
     * 元数据(JSON格式，存储额外的检测信息)
     */
    private String metadata;

    /**
     * 备注
     */
    private String remark;
}