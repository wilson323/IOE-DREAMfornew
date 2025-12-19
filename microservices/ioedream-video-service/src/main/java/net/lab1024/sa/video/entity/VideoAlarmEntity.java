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
 * 视频报警实体类
 * <p>
 * 视频监控智能分析报警记录管理实体，支持多种报警类型和联动处理
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_video_alarm")
public class VideoAlarmEntity extends BaseEntity {

    /**
     * 报警ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long alarmId;

    /**
     * 报警编号
     */
    @TableField("alarm_no")
    private String alarmNo;

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
     * 报警类型：1-移动侦测 2-入侵检测 3-人脸识别 4-车牌识别 5-异常行为 6-区域入侵
     */
    @TableField("alarm_type")
    private Integer alarmType;

    /**
     * 报警子类型
     */
    @TableField("alarm_sub_type")
    private Integer alarmSubType;

    /**
     * 报警级别：1-低级 2-中级 3-高级 4-紧急
     */
    @TableField("alarm_level")
    private Integer alarmLevel;

    /**
     * 报警状态：1-待处理 2-处理中 3-已处理 4-误报 5-已忽略
     */
    @TableField("alarm_status")
    private Integer alarmStatus;

    /**
     * 报警时间
     */
    @TableField("alarm_time")
    private LocalDateTime alarmTime;

    /**
     * 报警描述
     */
    @TableField("alarm_description")
    private String alarmDescription;

    /**
     * 报警图片路径
     */
    @TableField("alarm_image_path")
    private String alarmImagePath;

    /**
     * 报警视频片段路径
     */
    @TableField("alarm_video_path")
    private String alarmVideoPath;

    /**
     * 检测置信度（0-100）
     */
    @TableField("confidence")
    private Integer confidence;

    /**
     * 目标数量
     */
    @TableField("target_count")
    private Integer targetCount;

    /**
     * 目标类型：1-人 2-车辆 3-动物 4-物体
     */
    @TableField("target_type")
    private Integer targetType;

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
     * 位置描述
     */
    @TableField("location")
    private String location;

    /**
     * 经度
     */
    @TableField("longitude")
    private Double longitude;

    /**
     * 纬度
     */
    @TableField("latitude")
    private Double latitude;

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
     * 处理结果：1-确认为真实报警 2-误报 3-需要进一步调查
     */
    @TableField("handle_result")
    private Integer handleResult;

    /**
     * 处理说明
     */
    @TableField("handle_comment")
    private String handleComment;

    /**
     * 联动动作：1-发送通知 2-录像 3-广播 4-灯光控制 5-门禁控制
     */
    @TableField("linkage_actions")
    private String linkageActions;

    /**
     * 通知状态：0-未通知 1-已通知 2-通知失败
     */
    @TableField("notification_status")
    private Integer notificationStatus;

    /**
     * 通知时间
     */
    @TableField("notification_time")
    private LocalDateTime notificationTime;

    /**
     * 通知人员（JSON数组）
     */
    @TableField("notify_persons")
    private String notifyPersons;

    /**
     * 是否已确认：0-未确认 1-已确认
     */
    @TableField("confirmed")
    private Integer confirmed;

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
     * 录像记录ID
     */
    @TableField("record_id")
    private Long recordId;

    /**
     * 标签（JSON数组）
     */
    @TableField("tags")
    private String tags;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 扩展属性（JSON格式）
     */
    @TableField("extended_attributes")
    private String extendedAttributes;
}
