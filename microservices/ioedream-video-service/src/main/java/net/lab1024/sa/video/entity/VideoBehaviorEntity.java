package net.lab1024.sa.video.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * 视频行为检测实体
 * 记录视频AI检测到的各种行为事件
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_video_behavior")
@Schema(description = "视频行为检测实体")
public class VideoBehaviorEntity extends BaseEntity {

    /**
     * 行为ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "行为ID", example = "1698745325654325123")
    private Long behaviorId;

    /**
     * 设备ID
     */
    @TableField("device_id")
    @NotNull(message = "设备ID不能为空")
    @Schema(description = "设备ID", example = "1001")
    private Long deviceId;

    /**
     * 设备编码
     */
    @TableField("device_code")
    @Size(max = 64, message = "设备编码长度不能超过64个字符")
    @Schema(description = "设备编码", example = "CAM001")
    private String deviceCode;

    /**
     * 通道ID
     */
    @TableField("channel_id")
    @Schema(description = "通道ID", example = "1")
    private Long channelId;

    /**
     * 流ID
     */
    @TableField("stream_id")
    @Schema(description = "流ID", example = "1698745325654325123")
    private Long streamId;

    /**
     * 检测时间
     */
    @TableField("detection_time")
    @NotNull(message = "检测时间不能为空")
    @Schema(description = "检测时间", example = "2025-12-16T10:30:00")
    private LocalDateTime detectionTime;

    /**
     * 行为类型
     * 1-人员检测 2-车辆检测 3-物体检测 4-人脸检测 5-异常行为 6-正常行为 7-其他行为
     */
    @TableField("behavior_type")
    @NotNull(message = "行为类型不能为空")
    @Min(value = 1, message = "行为类型必须在1-7之间")
    @Max(value = 7, message = "行为类型必须在1-7之间")
    @Schema(description = "行为类型", example = "5")
    private Integer behaviorType;

    /**
     * 行为子类型
     */
    @TableField("behavior_sub_type")
    @Min(value = 1, message = "行为子类型不能小于1")
    @Schema(description = "行为子类型", example = "1")
    private Integer behaviorSubType;

    /**
     * 行为类型描述
     */
    @TableField("behavior_type_desc")
    @Size(max = 100, message = "行为类型描述长度不能超过100个字符")
    @Schema(description = "行为类型描述", example = "人员徘徊")
    private String behaviorTypeDesc;

    /**
     * 行为严重程度
     * 1-低风险 2-中风险 3-高风险 4-极高风险
     */
    @TableField("severity_level")
    @Min(value = 1, message = "严重程度必须在1-4之间")
    @Max(value = 4, message = "严重程度必须在1-4之间")
    @Schema(description = "行为严重程度", example = "2")
    private Integer severityLevel;

    /**
     * 置信度
     */
    @TableField("confidence_score")
    @Min(value = 0, message = "置信度不能小于0")
    @Max(value = 100, message = "置信度不能大于100")
    @Schema(description = "置信度", example = "85.5")
    private BigDecimal confidenceScore;

    /**
     * 检测目标数量
     */
    @TableField("target_count")
    @Min(value = 0, message = "目标数量不能小于0")
    @Schema(description = "检测目标数量", example = "3")
    private Integer targetCount;

    /**
     * 目标ID列表（逗号分隔）
     */
    @TableField("target_ids")
    @Size(max = 1000, message = "目标ID列表长度不能超过1000个字符")
    @Schema(description = "目标ID列表", example = "1001,1002,1003")
    private String targetIds;

    /**
     * 人员ID（如果检测到特定人员）
     */
    @TableField("person_id")
    @Schema(description = "人员ID", example = "1001")
    private Long personId;

    /**
     * 人员编号
     */
    @TableField("person_code")
    @Size(max = 64, message = "人员编号长度不能超过64个字符")
    @Schema(description = "人员编号", example = "EMP001")
    private String personCode;

    /**
     * 人员姓名
     */
    @TableField("person_name")
    @Size(max = 100, message = "人员姓名长度不能超过100个字符")
    @Schema(description = "人员姓名", example = "张三")
    private String personName;

    /**
     * 行为描述
     */
    @TableField("behavior_description")
    @Size(max = 500, message = "行为描述长度不能超过500个字符")
    @Schema(description = "行为描述", example = "检测到人员在门口徘徊超过5分钟")
    private String behaviorDescription;

    /**
     * 行为区域坐标（JSON格式：[{\"x\":100,\"y\":200,\"w\":150,\"h\":180}]）
     */
    @TableField("behavior_regions")
    @Size(max = 2000, message = "行为区域坐标长度不能超过2000个字符")
    @Schema(description = "行为区域坐标", example = "[{\"x\":100,\"y\":200,\"w\":150,\"h\":180}]")
    private String behaviorRegions;

    /**
     * 行为持续时间（秒）
     */
    @TableField("duration_seconds")
    @Min(value = 0, message = "持续时间不能小于0")
    @Schema(description = "行为持续时间（秒）", example = "300")
    private Long durationSeconds;

    /**
     * 行为开始时间
     */
    @TableField("start_time")
    @Schema(description = "行为开始时间", example = "2025-12-16T10:25:00")
    private LocalDateTime startTime;

    /**
     * 行为结束时间
     */
    @TableField("end_time")
    @Schema(description = "行为结束时间", example = "2025-12-16T10:30:00")
    private LocalDateTime endTime;

    /**
     * 场景图片URL
     */
    @TableField("scene_image_url")
    @Size(max = 500, message = "场景图片URL长度不能超过500个字符")
    @Schema(description = "场景图片URL", example = "/uploads/behavior/scene_1698745325654325123.jpg")
    private String sceneImageUrl;

    /**
     * 行为快照URL列表（逗号分隔）
     */
    @TableField("snapshot_urls")
    @Size(max = 2000, message = "快照URL列表长度不能超过2000个字符")
    @Schema(description = "行为快照URL列表", example = "url1.jpg,url2.jpg,url3.jpg")
    private String snapshotUrls;

    /**
     * 视频片段URL
     */
    @TableField("video_segment_url")
    @Size(max = 500, message = "视频片段URL长度不能超过500个字符")
    @Schema(description = "视频片段URL", example = "/uploads/behavior/video_1698745325654325123.mp4")
    private String videoSegmentUrl;

    /**
     * 检测算法类型
     * 1-YOLO 2-SSD 3-Faster R-CNN 4-Mask R-CNN 5-OpenPose 6-自定义算法 7-商汤 8-旷视
     */
    @TableField("detection_algorithm")
    @Min(value = 1, message = "检测算法类型必须在1-8之间")
    @Max(value = 8, message = "检测算法类型必须在1-8之间")
    @Schema(description = "检测算法类型", example = "1")
    private Integer detectionAlgorithm;

    /**
     * 检测模型版本
     */
    @TableField("detection_model")
    @Size(max = 64, message = "检测模型版本长度不能超过64个字符")
    @Schema(description = "检测模型版本", example = "YOLOv5s-v6.0")
    private String detectionModel;

    /**
     * 检测耗时（毫秒）
     */
    @TableField("detection_duration")
    @Min(value = 0, message = "检测耗时不能小于0")
    @Schema(description = "检测耗时（毫秒）", example = "250")
    private Long detectionDuration;

    /**
     * 告警标志
     * 0-未触发 1-已触发
     */
    @TableField("alarm_triggered")
    @Min(value = 0, message = "告警标志必须在0-1之间")
    @Max(value = 1, message = "告警标志必须在0-1之间")
    @Schema(description = "告警标志", example = "1")
    private Integer alarmTriggered;

    /**
     * 告警类型
     */
    @TableField("alarm_types")
    @Size(max = 200, message = "告警类型长度不能超过200个字符")
    @Schema(description = "告警类型", example = "异常行为,安全隐患")
    private String alarmTypes;

    /**
     * 告警级别
     * 1-一般 2-重要 3-紧急 4-严重
     */
    @TableField("alarm_level")
    @Min(value = 1, message = "告警级别必须在1-4之间")
    @Max(value = 4, message = "告警级别必须在1-4之间")
    @Schema(description = "告警级别", example = "2")
    private Integer alarmLevel;

    /**
     * 是否需要人工确认
     * 0-不需要 1-需要
     */
    @TableField("need_manual_confirm")
    @Min(value = 0, message = "人工确认标志必须在0-1之间")
    @Max(value = 1, message = "人工确认标志必须在0-1之间")
    @Schema(description = "是否需要人工确认", example = "1")
    private Integer needManualConfirm;

    /**
     * 处理状态
     * 0-未处理 1-已确认 2-已忽略 3-处理中 4-已解决
     */
    @TableField("process_status")
    @Min(value = 0, message = "处理状态必须在0-4之间")
    @Max(value = 4, message = "处理状态必须在0-4之间")
    @Schema(description = "处理状态", example = "0")
    private Integer processStatus;

    /**
     * 处理时间
     */
    @TableField("process_time")
    @Schema(description = "处理时间", example = "2025-12-16T10:35:00")
    private LocalDateTime processTime;

    /**
     * 处理人ID
     */
    @TableField("process_user_id")
    @Schema(description = "处理人ID", example = "1001")
    private Long processUserId;

    /**
     * 处理人姓名
     */
    @TableField("process_user_name")
    @Size(max = 100, message = "处理人姓名长度不能超过100个字符")
    @Schema(description = "处理人姓名", example = "管理员")
    private String processUserName;

    /**
     * 处理备注
     */
    @TableField("process_remark")
    @Size(max = 500, message = "处理备注长度不能超过500个字符")
    @Schema(description = "处理备注", example = "确认为正常访客，已登记")
    private String processRemark;

    /**
     * 关联事件ID列表
     */
    @TableField("related_event_ids")
    @Size(max = 1000, message = "关联事件ID列表长度不能超过1000个字符")
    @Schema(description = "关联事件ID列表", example = "1001,1002")
    private String relatedEventIds;

    /**
     * 行为标签（逗号分隔）
     */
    @TableField("behavior_tags")
    @Size(max = 500, message = "行为标签长度不能超过500个字符")
    @Schema(description = "行为标签", example = "徘徊,可疑,重点关注")
    private String behaviorTags;

    /**
     * 影响等级
     * 1-无影响 2-轻微影响 3-一般影响 4-较大影响 5-严重影响
     */
    @TableField("impact_level")
    @Min(value = 1, message = "影响等级必须在1-5之间")
    @Max(value = 5, message = "影响等级必须在1-5之间")
    @Schema(description = "影响等级", example = "3")
    private Integer impactLevel;

    /**
     * 风险等级
     * 1-无风险 2-低风险 3-中风险 4-高风险 5-极高风险
     */
    @TableField("risk_level")
    @Min(value = 1, message = "风险等级必须在1-5之间")
    @Max(value = 5, message = "风险等级必须在1-5之间")
    @Schema(description = "风险等级", example = "3")
    private Integer riskLevel;

    /**
     * 处理优先级
     * 1-低 2-中 3-高 4-紧急
     */
    @TableField("process_priority")
    @Min(value = 1, message = "处理优先级必须在1-4之间")
    @Max(value = 4, message = "处理优先级必须在1-4之间")
    @Schema(description = "处理优先级", example = "2")
    private Integer processPriority;

    /**
     * 环境信息（JSON格式：光照、天气、温湿度等）
     */
    @TableField("environment_info")
    @Size(max = 1000, message = "环境信息长度不能超过1000个字符")
    @Schema(description = "环境信息", example = "{\"lighting\":\"良好\",\"weather\":\"晴天\",\"temperature\":25}")
    private String environmentInfo;

    /**
     * 扩展属性（JSON格式）
     */
    @TableField("extended_attributes")
    @Size(max = 2000, message = "扩展属性长度不能超过2000个字符")
    @Schema(description = "扩展属性", example = "{\"custom_field1\":\"value1\",\"custom_field2\":\"value2\"}")
    private String extendedAttributes;

    /**
     * 删除标志
     */
    @TableLogic
    @TableField("deleted_flag")
    @Schema(description = "删除标志", example = "0")
    private Integer deletedFlag;
}
