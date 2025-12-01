package net.lab1024.sa.admin.module.access.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 门禁告警实体类
 * <p>
 * 记录门禁系统特有告警事件，实现安全监控和事件管理
 * 严格遵循扩展表架构设计，避免重复建设，基于现有事件管理增强和完善
 *
 * 关联架构：SmartDeviceEntity + AreaEntity + SmartAccessAlertEntity
 *
 * @author SmartAdmin Team
 * @date 2025-11-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_smart_access_alert")
public class SmartAccessAlertEntity extends BaseEntity {

    /**
     * 告警ID（主键）
     */
    @TableId(type = com.baomidou.mybatisplus.annotation.IdType.AUTO)
    private Long alertId;

    /**
     * 设备ID（关联SmartDeviceEntity.deviceId，可选）
     */
    @TableField("device_id")
    private Long deviceId;

    /**
     * 区域ID（关联AreaEntity.areaId，可选）
     */
    @TableField("area_id")
    private Long areaId;

    /**
     * 人员ID（关联人员表，可选）
     */
    @TableField("person_id")
    private Long personId;

    /**
     * 告警类型 (DEVICE_OFFLINE-设备离线, DEVICE_FAULT-设备故障,
     * ILLEGAL_ACCESS-非法访问, FORCED_OPEN-胁迫开门,
     * DOOR_FORCED-门被强制, TAMPER_ALARM-防拆报警,
     * POWER_FAILURE-断电报警, NETWORK_ERROR-网络错误)
     */
    @NotNull(message = "告警类型不能为空")
    @TableField("alert_type")
    private String alertType;

    /**
     * 严重级别 (LOW-低, MEDIUM-中, HIGH-高, CRITICAL-紧急)
     */
    @NotNull(message = "严重级别不能为空")
    @TableField("severity_level")
    private String severityLevel;

    /**
     * 告警标题
     */
    @NotNull(message = "告警标题不能为空")
    @TableField("alert_title")
    private String alertTitle;

    /**
     * 告警内容
     */
    @NotNull(message = "告警内容不能为空")
    @TableField("alert_content")
    private String alertContent;

    /**
     * 告警详细数据 (JSON格式，包含详细的告警信息)
     */
    @TableField("alert_data")
    private String alertData;

    /**
     * 事件时间
     */
    @NotNull(message = "事件时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("event_time")
    private LocalDateTime eventTime;

    /**
     * 告警发生位置 (区域、设备位置等)
     */
    @TableField("alert_location")
    private String alertLocation;

    /**
     * 告警源 (DEVICE-设备, SYSTEM-系统, USER-用户, SENSOR-传感器)
     */
    @TableField("alert_source")
    private String alertSource;

    /**
     * 处理状态 (PENDING-待处理, PROCESSING-处理中, RESOLVED-已解决, IGNORED-已忽略)
     */
    @TableField("process_status")
    private String processStatus;

    /**
     * 处理说明
     */
    @TableField("process_note")
    private String processNote;

    /**
     * 处理人员ID
     */
    @TableField("process_user_id")
    private Long processUserId;

    /**
     * 处理人员姓名
     */
    @TableField("process_user_name")
    private String processUserName;

    /**
     * 处理时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("process_time")
    private LocalDateTime processTime;

    /**
     * 告警持续时间(秒)
     */
    @TableField("alert_duration")
    private Integer alertDuration;

    /**
     * 是否需要确认 (0-不需要, 1-需要)
     */
    @TableField("confirmation_required")
    private Integer confirmationRequired;

    /**
     * 确认人员ID
     */
    @TableField("confirmation_user_id")
    private Long confirmationUserId;

    /**
     * 确认人员姓名
     */
    @TableField("confirmation_user_name")
    private String confirmationUserName;

    /**
     * 确认时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("confirmation_time")
    private LocalDateTime confirmationTime;

    /**
     * 是否自动处理 (0-手动, 1-自动)
     */
    @TableField("auto_process")
    private Integer autoProcess;

    /**
     * 自动处理规则
     */
    @TableField("auto_process_rule")
    private String autoProcessRule;

    /**
     * 告警通知方式 (JSON格式: [\"EMAIL\", \"SMS\", \"PUSH\", \"ALARM\"])
     */
    @TableField("notification_methods")
    private String notificationMethods;

    /**
     * 通知人员列表 (JSON格式: [{userId: 1, userName: \"管理员\"}])
     */
    @TableField("notification_users")
    private String notificationUsers;

    /**
     * 通知时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("notification_time")
    private LocalDateTime notificationTime;

    /**
     * 相关图片URL列表 (JSON格式: [\"url1\", \"url2\"])
     */
    @TableField("related_image_urls")
    private String relatedImageUrls;

    /**
     * 相关视频URL列表 (JSON格式: [\"video1\", \"video2\"])
     */
    @TableField("related_video_urls")
    private String relatedVideoUrls;

    /**
     * 音频文件URL
     */
    @TableField("audio_file_url")
    private String audioFileUrl;

    /**
     * 告警级别更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("severity_update_time")
    private LocalDateTime severityUpdateTime;

    /**
     * 重复告警次数
     */
    @TableField("repeat_count")
    private Integer repeatCount;

    /**
     * 告警分组ID
     */
    @TableField("alert_group_id")
    private String alertGroupId;

    /**
     * 告警标签 (JSON格式: [\"安全\", \"设备\", \"区域\"])
     */
    @TableField("alert_tags")
    private String alertTags;

    /**
     * 告警描述
     */
    @TableField("alert_description")
    private String alertDescription;

    /**
     * 处理建议
     */
    @TableField("process_suggestion")
    private String processSuggestion;

    /**
     * 预防措施
     */
    @TableField("prevention_measures")
    private String preventionMeasures;

    /**
     * 影响范围 (影响的人员、区域、设备等)
     */
    @TableField("impact_scope")
    private String impactScope;

    /**
     * 风险等级 (RISK-风险, WARNING-警告, INFO-信息)
     */
    @TableField("risk_level")
    private String riskLevel;

    /**
     * 扩展配置1
     */
    @TableField("ext_config1")
    private String extConfig1;

    /**
     * 扩展配置2
     */
    @TableField("ext_config2")
    private String extConfig2;

    /**
     * 扩展配置3
     */
    @TableField("ext_config3")
    private String extConfig3;
}