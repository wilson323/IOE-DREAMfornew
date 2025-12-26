package net.lab1024.sa.common.entity.access;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 门禁报警实体
 * <p>
 * 记录门禁系统的各种报警事件：
 * - 设备离线报警
 * - 非法闯入报警
 * - 门超时未关报警
 * - 强力破门报警
 * - 胁迫报警
 * - 卡片异常报警
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@TableName("t_access_alarm")
public class AccessAlarmEntity {

    /**
     * 报警ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long alarmId;

    /**
     * 报警类型
     * DEVICE_OFFLINE - 设备离线
     * ILLEGAL_ENTRY - 非法闯入
     * DOOR_TIMEOUT - 门超时未关
     * FORCED_ENTRY - 强力破门
     * DURESS_ALARM - 胁迫报警
     * CARD_ABNORMAL - 卡片异常
     */
    @TableField("alarm_type")
    private String alarmType;

    /**
     * 报警级别
     * 1 - 低级（信息提示）
     * 2 - 中级（需要注意）
     * 3 - 高级（严重警告）
     * 4 - 紧急（立即处理）
     * 5 - 严重（危急）
     */
    @TableField("alarm_level")
    private Integer alarmLevel;

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
     * 区域ID
     */
    @TableField("area_id")
    private Long areaId;

    /**
     * 区域名称
     */
    @TableField("area_name")
    private String areaName;

    /**
     * 用户ID（可能为空，如设备离线报警）
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户姓名
     */
    @TableField("user_name")
    private String userName;

    /**
     * 通行记录ID（可能为空）
     */
    @TableField("access_record_id")
    private Long accessRecordId;

    /**
     * 报警时间
     */
    @TableField("alarm_time")
    private LocalDateTime alarmTime;

    /**
     * 报警日期（用于按日期查询）
     */
    @TableField("alarm_date")
    private LocalDate alarmDate;

    /**
     * 报警描述
     */
    @TableField("alarm_description")
    private String alarmDescription;

    /**
     * 报警详情（JSON格式）
     */
    @TableField("alarm_detail")
    private String alarmDetail;

    /**
     * 报警图片URL（如果有）
     */
    @TableField("alarm_image_url")
    private String alarmImageUrl;

    /**
     * 报警视频片段URL（如果有）
     */
    @TableField("alarm_video_url")
    private String alarmVideoUrl;

    /**
     * 处理状态
     * 0 - 未处理
     * 1 - 处理中
     * 2 - 已处理
     * 3 - 已忽略
     */
    @TableField("process_status")
    private Integer processStatus;

    /**
     * 是否已处理
     */
    @TableField("processed")
    private Boolean processed;

    /**
     * 处理人ID
     */
    @TableField("handler_id")
    private Long handlerId;

    /**
     * 处理人姓名
     */
    @TableField("handler_name")
    private String handlerName;

    /**
     * 处理时间
     */
    @TableField("handle_time")
    private LocalDateTime handleTime;

    /**
     * 处理备注
     */
    @TableField("handle_remark")
    private String handleRemark;

    /**
     * 处理结果
     */
    @TableField("handle_result")
    private String handleResult;

    /**
     * 是否已确认
     */
    @TableField("confirmed")
    private Boolean confirmed;

    /**
     * 确认人ID
     */
    @TableField("confirmer_id")
    private Long confirmerId;

    /**
     * 确认时间
     */
    @TableField("confirm_time")
    private LocalDateTime confirmTime;

    /**
     * 是否已发送通知
     */
    @TableField("notification_sent")
    private Boolean notificationSent;

    /**
     * 通知发送时间
     */
    @TableField("notification_time")
    private LocalDateTime notificationTime;

    /**
     * 通知接收人列表（JSON格式）
     */
    @TableField("notification_recipients")
    private String notificationRecipients;

    /**
     * 报警源
     * SYSTEM - 系统自动检测
     * MANUAL - 人工上报
     * DEVICE - 设备上报
     */
    @TableField("alarm_source")
    private String alarmSource;

    /**
     * 报警状态
     * ACTIVE - 活跃（未处理）
     * ACKNOWLEDGED - 已确认
     * PROCESSING - 处理中
     * RESOLVED - 已解决
     * CLOSED - 已关闭
     * IGNORED - 已忽略
     */
    @TableField("alarm_status")
    private String alarmStatus;

    /**
     * 优先级（用于排序）
     */
    @TableField("priority")
    private Integer priority;

    /**
     * 重复次数（同一类报警的重复次数）
     */
    @TableField("repeat_count")
    private Integer repeatCount;

    /**
     * 首次报警时间（用于重复报警）
     */
    @TableField("first_alarm_time")
    private LocalDateTime firstAlarmTime;

    /**
     * 最后报警时间（用于重复报警）
     */
    @TableField("last_alarm_time")
    private LocalDateTime lastAlarmTime;

    /**
     * 扩展属性（JSON格式，存储额外信息）
     */
    @TableField("extended_attributes")
    private String extendedAttributes;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建人ID
     */
    @TableField("create_user_id")
    private Long createUserId;

    /**
     * 更新人ID
     */
    @TableField("update_user_id")
    private Long updateUserId;

    /**
     * 删除标记（0-未删除 1-已删除）
     */
    @TableField("deleted_flag")
    @TableLogic
    private Boolean deletedFlag;

    /**
     * 乐观锁版本号
     */
    @Version
    @TableField("version")
    private Integer version;
}
