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
 * 视频告警规则实体类
 * <p>
 * 智能视频分析告警规则管理实体，支持多种检测类型和联动控制
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_video_alarm_rule")
public class VideoAlarmRuleEntity extends BaseEntity {

    /**
     * 规则ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long ruleId;

    /**
     * 规则名称
     */
    @TableField("rule_name")
    private String ruleName;

    /**
     * 规则编码
     */
    @TableField("rule_code")
    private String ruleCode;

    /**
     * 规则类型：1-入侵检测 2-越界检测 3-区域入侵 4-徘徊检测 5-物品遗留 6-物品丢失 7-人员聚集 8-快速移动 9-车牌识别 10-人脸识别
     */
    @TableField("rule_type")
    private Integer ruleType;

    /**
     * 规则子类型
     */
    @TableField("rule_sub_type")
    private Integer ruleSubType;

    /**
     * 设备ID列表（JSON数组）
     */
    @TableField("device_ids")
    private String deviceIds;

    /**
     * 通道号列表（JSON数组）
     */
    @TableField("channel_numbers")
    private String channelNumbers;

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
     * 检测区域坐标（JSON格式：多边形顶点坐标）
     */
    @TableField("detection_area")
    private String detectionArea;

    /**
     * 排除区域坐标（JSON格式：多边形顶点坐标）
     */
    @TableField("exclusion_area")
    private String exclusionArea;

    /**
     * 规则优先级：1-低 2-中 3-高 4-紧急
     */
    @TableField("priority")
    private Integer priority;

    /**
     * 告警级别：1-提示 2-警告 3-严重 4-紧急
     */
    @TableField("alarm_level")
    private Integer alarmLevel;

    /**
     * 规则状态：1-启用 2-禁用 3-测试
     */
    @TableField("rule_status")
    private Integer ruleStatus;

    /**
     * 生效时间
     */
    @TableField("effective_time")
    private LocalDateTime effectiveTime;

    /**
     * 失效时间
     */
    @TableField("expire_time")
    private LocalDateTime expireTime;

    /**
     * 时间计划配置（JSON格式）
     */
    @TableField("time_schedule")
    private String timeSchedule;

    /**
     * 检测灵敏度：1-低 2-中 3-高 4-极高
     */
    @TableField("sensitivity")
    private Integer sensitivity;

    /**
     * 最小目标大小（像素）
     */
    @TableField("min_target_size")
    private Integer minTargetSize;

    /**
     * 最大目标大小（像素）
     */
    @TableField("max_target_size")
    private Integer maxTargetSize;

    /**
     * 最小持续时间（秒）
     */
    @TableField("min_duration")
    private Integer minDuration;

    /**
     * 最大持续时间（秒）
     */
    @TableField("max_duration")
    private Integer maxDuration;

    /**
     * 目标方向：1-任意 2-进入 3-离开 4-双向
     */
    @TableField("target_direction")
    private Integer targetDirection;

    /**
     * 目标速度阈值（km/h）
     */
    @TableField("speed_threshold")
    private Double speedThreshold;

    /**
     * 人数阈值
     */
    @TableField("people_count_threshold")
    private Integer peopleCountThreshold;

    /**
     * 车辆类型：1-小车 2-大车 3-摩托车 4-自行车
     */
    @TableField("vehicle_type")
    private Integer vehicleType;

    /**
     * 人脸相似度阈值（0-100）
     */
    @TableField("face_similarity_threshold")
    private Integer faceSimilarityThreshold;

    /**
     * 车牌号码（支持模糊匹配）
     */
    @TableField("license_plate")
    private String licensePlate;

    /**
     * 车牌颜色：1-蓝色 2-黄色 3-绿色 4-白色 5-黑色
     */
    @TableField("plate_color")
    private Integer plateColor;

    /**
     * 是否联动录像：0-不联动 1-联动
     */
    @TableField("linkage_record")
    private Integer linkageRecord;

    /**
     * 录像预录时间（秒）
     */
    @TableField("pre_record_time")
    private Integer preRecordTime;

    /**
     * 录像后录时间（秒）
     */
    @TableField("post_record_time")
    private Integer postRecordTime;

    /**
     * 是否联动抓拍：0-不联动 1-联动
     */
    @TableField("linkage_snapshot")
    private Integer linkageSnapshot;

    /**
     * 抓拍数量
     */
    @TableField("snapshot_count")
    private Integer snapshotCount;

    /**
     * 是否联动声光报警：0-不联动 1-联动
     */
    @TableField("linkage_alarm")
    private Integer linkageAlarm;

    /**
     * 报警持续时间（秒）
     */
    @TableField("alarm_duration")
    private Integer alarmDuration;

    /**
     * 是否联动门禁：0-不联动 1-联动
     */
    @TableField("linkage_access")
    private Integer linkageAccess;

    /**
     * 门禁控制动作：1-开门 2-关门 3-锁定
     */
    @TableField("access_action")
    private Integer accessAction;

    /**
     * 是否联动广播：0-不联动 1-联动
     */
    @TableField("linkage_broadcast")
    private Integer linkageBroadcast;

    /**
     * 广播内容
     */
    @TableField("broadcast_content")
    private String broadcastContent;

    /**
     * 是否联动短信通知：0-不通知 1-通知
     */
    @TableField("linkage_sms")
    private Integer linkageSms;

    /**
     * 短信接收人列表（JSON数组）
     */
    @TableField("sms_receivers")
    private String smsReceivers;

    /**
     * 是否联动邮件通知：0-不通知 1-通知
     */
    @TableField("linkage_email")
    private Integer linkageEmail;

    /**
     * 邮件接收人列表（JSON数组）
     */
    @TableField("email_receivers")
    private String emailReceivers;

    /**
     * 是否联动APP推送：0-不推送 1-推送
     */
    @TableField("linkage_push")
    private Integer linkagePush;

    /**
     * 推送用户列表（JSON数组）
     */
    @TableField("push_users")
    private String pushUsers;

    /**
     * 算法参数配置（JSON格式）
     */
    @TableField("algorithm_params")
    private String algorithmParams;

    /**
     * 规则描述
     */
    @TableField("description")
    private String description;

    /**
     * 创建人ID
     */
    @TableField("created_by")
    private Long createdBy;

    /**
     * 创建人姓名
     */
    @TableField("created_by_name")
    private String createdByName;

    /**
     * 审核状态：1-待审核 2-审核通过 3-审核拒绝
     */
    @TableField("audit_status")
    private Integer auditStatus;

    /**
     * 审核人ID
     */
    @TableField("audit_by")
    private Long auditBy;

    /**
     * 审核人姓名
     */
    @TableField("audit_by_name")
    private String auditByName;

    /**
     * 审核时间
     */
    @TableField("audit_time")
    private LocalDateTime auditTime;

    /**
     * 审核意见
     */
    @TableField("audit_comment")
    private String auditComment;

    /**
     * 规则标签（JSON数组）
     */
    @TableField("tags")
    private String tags;

    /**
     * 扩展属性（JSON格式）
     */
    @TableField("extended_attributes")
    private String extendedAttributes;
}