package net.lab1024.sa.common.video.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import net.lab1024.sa.common.entity.BaseEntity;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * 视频目标检测记录实体
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 纯数据模型，无业务逻辑方法
 * - Entity≤200行，理想≤100行
 * - 包含数据字段、基础注解、构造方法
 * - 无static方法，无业务计算逻辑
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0 (重构版)
 * @since 2025-12-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_video_object_detection")
@Schema(description = "视频目标检测记录实体")
public class VideoObjectDetectionEntity extends BaseEntity {

    /**
     * 检测记录ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "检测记录ID", example = "1689854272000000001")
    private Long detectionId;

    /**
     * 设备ID
     */
    @NotNull(message = "设备ID不能为空")
    @TableField("device_id")
    @Schema(description = "设备ID", example = "1001")
    private Long deviceId;

    /**
     * 设备编码
     */
    @NotBlank(message = "设备编码不能为空")
    @Size(max = 64, message = "设备编码长度不能超过64个字符")
    @TableField("device_code")
    @Schema(description = "设备编码", example = "CAM001")
    private String deviceCode;

    /**
     * 通道ID
     */
    @TableField("channel_id")
    @Schema(description = "通道ID", example = "1")
    private Integer channelId;

    /**
     * 检测时间
     */
    @NotNull(message = "检测时间不能为空")
    @TableField("detection_time")
    @Schema(description = "检测时间", example = "2025-12-16T10:30:00")
    private LocalDateTime detectionTime;

    /**
     * 检测算法ID
     */
    @TableField("detection_algorithm")
    @Schema(description = "检测算法ID", example = "YOLOv8")
    private String detectionAlgorithm;

    /**
     * 目标类型 1-人员 2-车辆 3-物体 4-动物 5-人脸 6-车牌 7-行李 8-危险品 9-其他
     */
    @NotNull(message = "目标类型不能为空")
    @Min(value = 1, message = "目标类型必须在1-9之间")
    @Max(value = 9, message = "目标类型必须在1-9之间")
    @TableField("object_type")
    @Schema(description = "目标类型", example = "1")
    private Integer objectType;

    /**
     * 目标子类型
     */
    @TableField("object_sub_type")
    @Schema(description = "目标子类型", example = "1")
    private Integer objectSubType;

    /**
     * 目标ID（跟踪ID）
     */
    @TableField("object_id")
    @Schema(description = "目标ID", example = "OBJ_001")
    private String objectId;

    /**
     * 置信度
     */
    @DecimalMin(value = "0.0", message = "置信度不能小于0")
    @DecimalMax(value = "1.0", message = "置信度不能大于1")
    @TableField("confidence_score")
    @Schema(description = "置信度", example = "0.95")
    private BigDecimal confidenceScore;

    /**
     * 边界框坐标（JSON格式）{"x":100,"y":200,"width":150,"height":200}
     */
    @Size(max = 500, message = "边界框坐标长度不能超过500个字符")
    @TableField("bounding_box")
    @Schema(description = "边界框坐标", example = "{\"x\":100,\"y\":200,\"width\":150,\"height\":200}")
    private String boundingBox;

    /**
     * 中心点坐标X
     */
    @TableField("center_x")
    @Schema(description = "中心点坐标X", example = "175.5")
    private BigDecimal centerX;

    /**
     * 中心点坐标Y
     */
    @TableField("center_y")
    @Schema(description = "中心点坐标Y", example = "300.0")
    private BigDecimal centerY;

    /**
     * 区域ID
     */
    @TableField("area_id")
    @Schema(description = "区域ID", example = "1001")
    private Long areaId;

    /**
     * 告警级别 1-信息 2-提醒 3-警告 4-严重 5-紧急
     */
    @TableField("alert_level")
    @Schema(description = "告警级别", example = "2")
    private Integer alertLevel;

    /**
     * 是否触发告警
     */
    @TableField("alert_triggered")
    @Schema(description = "是否触发告警", example = "0")
    private Integer alertTriggered;

    /**
     * 告警类型
     */
    @Size(max = 100, message = "告警类型长度不能超过100个字符")
    @TableField("alert_type")
    @Schema(description = "告警类型", example = "area_intrusion")
    private String alertType;

    /**
     * 处理状态 0-未处理 1-处理中 2-已处理 3-已忽略
     */
    @TableField("process_status")
    @Schema(description = "处理状态", example = "0")
    private Integer processStatus;

    /**
     * 验证结果 0-未验证 1-正确 2-错误 3-不确定
     */
    @TableField("verification_result")
    @Schema(description = "验证结果", example = "0")
    private Integer verificationResult;

    /**
     * 验证人员ID
     */
    @TableField("verified_by")
    @Schema(description = "验证人员ID", example = "1001")
    private Long verifiedBy;

    /**
     * 验证时间
     */
    @TableField("verification_time")
    @Schema(description = "验证时间", example = "2025-12-16T10:35:00")
    private LocalDateTime verificationTime;

    /**
     * 目标图像URL
     */
    @Size(max = 500, message = "目标图像URL长度不能超过500个字符")
    @TableField("object_image_url")
    @Schema(description = "目标图像URL", example = "/images/detection/20251216/obj_001.jpg")
    private String objectImageUrl;

    /**
     * 目标缩略图URL
     */
    @Size(max = 500, message = "目标缩略图URL长度不能超过500个字符")
    @TableField("thumbnail_url")
    @Schema(description = "目标缩略图URL", example = "/images/thumbnail/20251216/obj_001_thumb.jpg")
    private String thumbnailUrl;

    /**
     * 扩展属性（JSON格式）
     */
    @Size(max = 2000, message = "扩展属性长度不能超过2000个字符")
    @TableField("extended_attributes")
    @Schema(description = "扩展属性", example = "{\"custom_field\":\"custom_value\"}")
    private String extendedAttributes;

    /**
     * 备注信息
     */
    @Size(max = 500, message = "备注信息长度不能超过500个字符")
    @TableField("remark")
    @Schema(description = "备注信息", example = "重要检测记录")
    private String remark;

    // ==================== 业务状态常量 ====================

    /**
     * 目标类型常量
     */
    public static class ObjectType {
        public static final int PERSON = 1;        // 人员
        public static final int VEHICLE = 2;       // 车辆
        public static final int OBJECT = 3;        // 物体
        public static final int ANIMAL = 4;        // 动物
        public static final int FACE = 5;          // 人脸
        public static final int LICENSE_PLATE = 6; // 车牌
        public static final int LUGGAGE = 7;       // 行李
        public static final int DANGEROUS_GOODS = 8; // 危险品
        public static final int OTHER = 9;         // 其他
    }

    /**
     * 告警级别常量
     */
    public static class AlertLevel {
        public static final int INFO = 1;     // 信息
        public static final int NOTICE = 2;   // 提醒
        public static final int WARNING = 3;  // 警告
        public static final int CRITICAL = 4; // 严重
        public static final int URGENT = 5;   // 紧急
    }

    /**
     * 处理状态常量
     */
    public static class ProcessStatus {
        public static final int PENDING = 0;    // 未处理
        public static final int PROCESSING = 1; // 处理中
        public static final int PROCESSED = 2;  // 已处理
        public static final int IGNORED = 3;    // 已忽略
    }

    /**
     * 验证结果常量
     */
    public static class VerificationResult {
        public static final int UNVERIFIED = 0; // 未验证
        public static final int CORRECT = 1;    // 正确
        public static final int ERROR = 2;      // 错误
        public static final int UNCERTAIN = 3;  // 不确定
    }
}
