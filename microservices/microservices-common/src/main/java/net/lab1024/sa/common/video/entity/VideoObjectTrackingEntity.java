package net.lab1024.sa.common.video.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * 视频目标跟踪记录实体
 * 记录目标在连续帧中的运动轨迹和跟踪信息
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_video_object_tracking")
@Schema(description = "视频目标跟踪记录实体")
public class VideoObjectTrackingEntity extends BaseEntity {

    /**
     * 跟踪记录ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "跟踪记录ID", example = "1689854272000000001")
    private Long trackingId;

    /**
     * 目标ID（跨帧的唯一标识）
     */
    @NotBlank(message = "目标ID不能为空")
    @Size(max = 64, message = "目标ID长度不能超过64个字符")
    @TableField("object_id")
    @Schema(description = "目标ID", example = "OBJ_001")
    private String objectId;

    /**
     * 设备ID
     */
    @NotNull(message = "设备ID不能为空")
    @TableField("device_id")
    @Schema(description = "设备ID", example = "1001")
    private Long deviceId;

    /**
     * 通道ID
     */
    @TableField("channel_id")
    @Schema(description = "通道ID", example = "1")
    private Integer channelId;

    /**
     * 跟踪开始时间
     */
    @NotNull(message = "跟踪开始时间不能为空")
    @TableField("tracking_start_time")
    @Schema(description = "跟踪开始时间", example = "2025-12-16T10:30:00")
    private LocalDateTime trackingStartTime;

    /**
     * 跟踪结束时间
     */
    @TableField("tracking_end_time")
    @Schema(description = "跟踪结束时间", example = "2025-12-16T10:35:00")
    private LocalDateTime trackingEndTime;

    /**
     * 跟踪持续时间（秒）
     */
    @TableField("tracking_duration")
    @Schema(description = "跟踪持续时间", example = "300")
    private Long trackingDuration;

    /**
     * 目标类型 1-人员 2-车辆 3-物体 4-动物 5-其他
     */
    @NotNull(message = "目标类型不能为空")
    @Min(value = 1, message = "目标类型必须在1-5之间")
    @Max(value = 5, message = "目标类型必须在1-5之间")
    @TableField("object_type")
    @Schema(description = "目标类型", example = "1")
    private Integer objectType;

    /**
     * 目标类型描述
     */
    @Size(max = 100, message = "目标类型描述长度不能超过100个字符")
    @TableField("object_type_desc")
    @Schema(description = "目标类型描述", example = "人员")
    private String objectTypeDesc;

    /**
     * 初始位置X
     */
    @TableField("initial_x")
    @Schema(description = "初始位置X", example = "100.0")
    private BigDecimal initialX;

    /**
     * 初始位置Y
     */
    @TableField("initial_y")
    @Schema(description = "初始位置Y", example = "200.0")
    private BigDecimal initialY;

    /**
     * 最终位置X
     */
    @TableField("final_x")
    @Schema(description = "最终位置X", example = "500.0")
    private BigDecimal finalX;

    /**
     * 最终位置Y
     */
    @TableField("final_y")
    @Schema(description = "最终位置Y", example = "400.0")
    private BigDecimal finalY;

    /**
     * 总位移距离（像素）
     */
    @TableField("total_distance")
    @Schema(description = "总位移距离", example = "640.3")
    private BigDecimal totalDistance;

    /**
     * 平均速度（像素/秒）
     */
    @TableField("average_speed")
    @Schema(description = "平均速度", example = "25.6")
    private BigDecimal averageSpeed;

    /**
     * 最大速度（像素/秒）
     */
    @TableField("max_speed")
    @Schema(description = "最大速度", example = "85.2")
    private BigDecimal maxSpeed;

    /**
     * 主要运动方向（角度）
     */
    @TableField("main_direction")
    @Schema(description = "主要运动方向", example = "45.5")
    private BigDecimal mainDirection;

    /**
     * 运动轨迹（JSON格式）
     */
    @Size(max = 5000, message = "运动轨迹长度不能超过5000个字符")
    @TableField("trajectory_path")
    @Schema(description = "运动轨迹", example = "[{\"x\":100,\"y\":200,\"t\":0},{\"x\":105,\"y\":202,\"t\":0.1}]")
    private String trajectoryPath;

    /**
     * 轨迹点数量
     */
    @TableField("trajectory_points")
    @Schema(description = "轨迹点数量", example = "150")
    private Integer trajectoryPoints;

    /**
     * 丢失次数
     */
    @TableField("lost_count")
    @Schema(description = "丢失次数", example = "3")
    private Integer lostCount;

    /**
     * 重获次数
     */
    @TableField("reacquire_count")
    @Schema(description = "重获次数", example = "2")
    private Integer reacquireCount;

    /**
     * 跟踪质量评分
     */
    @DecimalMin(value = "0.0", message = "跟踪质量评分不能小于0")
    @DecimalMax(value = "1.0", message = "跟踪质量评分不能大于1")
    @TableField("tracking_quality_score")
    @Schema(description = "跟踪质量评分", example = "0.85")
    private BigDecimal trackingQualityScore;

    /**
     * 平均置信度
     */
    @DecimalMin(value = "0.0", message = "平均置信度不能小于0")
    @DecimalMax(value = "1.0", message = "平均置信度不能大于1")
    @TableField("average_confidence")
    @Schema(description = "平均置信度", example = "0.92")
    private BigDecimal averageConfidence;

    /**
     * 最低置信度
     */
    @DecimalMin(value = "0.0", message = "最低置信度不能小于0")
    @DecimalMax(value = "1.0", message = "最低置信度不能大于1")
    @TableField("min_confidence")
    @Schema(description = "最低置信度", example = "0.75")
    private BigDecimal minConfidence;

    /**
     * 最高置信度
     */
    @DecimalMin(value = "0.0", message = "最高置信度不能小于0")
    @DecimalMax(value = "1.0", message = "最高置信度不能大于1")
    @TableField("max_confidence")
    @Schema(description = "最高置信度", example = "0.98")
    private BigDecimal maxConfidence;

    /**
     * 进入区域列表（JSON数组）
     */
    @Size(max = 1000, message = "进入区域列表长度不能超过1000个字符")
    @TableField("entered_areas")
    @Schema(description = "进入区域列表", example = "[\"A1\",\"B2\",\"C3\"]")
    private String enteredAreas;

    /**
     * 离开区域列表（JSON数组）
     */
    @Size(max = 1000, message = "离开区域列表长度不能超过1000个字符")
    @TableField("exited_areas")
    @Schema(description = "离开区域列表", example = "[\"A1\",\"B2\"]")
    private String exitedAreas;

    /**
     * 区域违规次数
     */
    @TableField("area_violations")
    @Schema(description = "区域违规次数", example = "2")
    private Integer areaViolations;

    /**
     * 停留区域ID
     */
    @TableField("loitering_area_id")
    @Schema(description = "停留区域ID", example = "1001")
    private Long loiteringAreaId;

    /**
     * 停留开始时间
     */
    @TableField("loitering_start_time")
    @Schema(description = "停留开始时间", example = "2025-12-16T10:32:00")
    private LocalDateTime loiteringStartTime;

    /**
     * 停留持续时间（秒）
     */
    @TableField("loitering_duration")
    @Schema(description = "停留持续时间", example = "180")
    private Long loiteringDuration;

    /**
     * 跟踪状态 1-活跃 2-丢失 3-完成 4-中断
     */
    @TableField("tracking_status")
    @Schema(description = "跟踪状态", example = "1")
    private Integer trackingStatus;

    /**
     * 丢失原因 1-遮挡 2-快速移动 3-离开画面 4-目标变形 5-光照变化
     */
    @TableField("lost_reason")
    @Schema(description = "丢失原因", example = "1")
    private Integer lostReason;

    /**
     * 关联检测记录数量
     */
    @TableField("detection_count")
    @Schema(description = "关联检测记录数量", example = "150")
    private Integer detectionCount;

    /**
     * 关联告警数量
     */
    @TableField("alert_count")
    @Schema(description = "关联告警数量", example = "3")
    private Integer alertCount;

    /**
     * 关联人脸ID（如果有）
     */
    @Size(max = 64, message = "关联人脸ID长度不能超过64个字符")
    @TableField("associated_face_id")
    @Schema(description = "关联人脸ID", example = "FACE_001")
    private String associatedFaceId;

    /**
     * 关联人员ID（如果有）
     */
    @TableField("associated_person_id")
    @Schema(description = "关联人员ID", example = "1001")
    private Long associatedPersonId;

    /**
     * 关联车牌号（如果有）
     */
    @Size(max = 50, message = "关联车牌号长度不能超过50个字符")
    @TableField("associated_plate_number")
    @Schema(description = "关联车牌号", example = "京A12345")
    private String associatedPlateNumber;

    /**
     * 轨迹分析结果（JSON格式）
     */
    @Size(max = 2000, message = "轨迹分析结果长度不能超过2000个字符")
    @TableField("trajectory_analysis")
    @Schema(description = "轨迹分析结果", example = "{\"pattern\":\"circular\",\"anomaly\":false}")
    private String trajectoryAnalysis;

    /**
     * 行为预测（JSON格式）
     */
    @Size(max = 1000, message = "行为预测长度不能超过1000个字符")
    @TableField("behavior_prediction")
    @Schema(description = "行为预测", example = "{\"next_action\":\"exit\",\"confidence\":0.8}")
    private String behaviorPrediction;

    /**
     * 异常标记 0-正常 1-异常轨迹 2-可疑行为 3-重点关注
     */
    @TableField("anomaly_flag")
    @Schema(description = "异常标记", example = "0")
    private Integer anomalyFlag;

    /**
     * 异常描述
     */
    @Size(max = 500, message = "异常描述长度不能超过500个字符")
    @TableField("anomaly_description")
    @Schema(description = "异常描述", example = "轨迹异常，在禁区内徘徊")
    private String anomalyDescription;

    /**
     * 跟踪算法版本
     */
    @Size(max = 50, message = "跟踪算法版本长度不能超过50个字符")
    @TableField("tracking_algorithm_version")
    @Schema(description = "跟踪算法版本", example = "DeepSort v1.2.0")
    private String trackingAlgorithmVersion;

    /**
     * 处理状态 0-未处理 1-处理中 2-已处理 3-已忽略
     */
    @TableField("process_status")
    @Schema(description = "处理状态", example = "0")
    private Integer processStatus;

    /**
     * 优先级 1-低 2-中 3-高 4-紧急
     */
    @TableField("priority")
    @Schema(description = "优先级", example = "2")
    private Integer priority;

    /**
     * 备注信息
     */
    @Size(max = 500, message = "备注信息长度不能超过500个字符")
    @TableField("remark")
    @Schema(description = "备注信息", example = "重要跟踪记录")
    private String remark;

    // 审计字段（继承自BaseEntity）
    // create_time, update_time, create_user_id, update_user_id, deleted_flag, version
}