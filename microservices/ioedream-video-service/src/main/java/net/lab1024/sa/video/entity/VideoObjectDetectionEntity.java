package net.lab1024.sa.video.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import io.swagger.v3.oas.annotations.media.Schema;
import net.lab1024.sa.common.entity.BaseEntity;

import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * 视频目标检测实体
 * 记录视频中检测到的目标对象信息
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_video_object_detection")
@Schema(description = "视频目标检测实体")
public class VideoObjectDetectionEntity extends BaseEntity {

    /**
     * 检测记录ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "检测记录ID", example = "1689854272000000001")
    private Long id;

    /**
     * 设备ID
     */
    @Schema(description = "设备ID", example = "CAM001")
    @TableField("device_id")
    private String deviceId;

    /**
     * 通道ID
     */
    @Schema(description = "通道ID", example = "1")
    @TableField("channel_id")
    private Integer channelId;

    /**
     * 检测时间
     */
    @Schema(description = "检测时间")
    @TableField("detection_time")
    private LocalDateTime detectionTime;

    /**
     * 目标类型(person-人,vehicle-车辆,face-人脸,object-物体)
     */
    @Schema(description = "目标类型", example = "person")
    @TableField("object_type")
    private String objectType;

    /**
     * 目标标签
     */
    @Schema(description = "目标标签", example = "adult_male")
    @TableField("object_label")
    private String objectLabel;

    /**
     * 置信度(0-1)
     */
    @Schema(description = "置信度", example = "0.95")
    @TableField("confidence")
    private BigDecimal confidence;

    /**
     * 边界框-X坐标
     */
    @Schema(description = "边界框-X坐标", example = "100")
    @TableField("bbox_x")
    private Integer bboxX;

    /**
     * 边界框-Y坐标
     */
    @Schema(description = "边界框-Y坐标", example = "200")
    @TableField("bbox_y")
    private Integer bboxY;

    /**
     * 边界框-宽度
     */
    @Schema(description = "边界框-宽度", example = "150")
    @TableField("bbox_width")
    private Integer bboxWidth;

    /**
     * 边界框-高度
     */
    @Schema(description = "边界框-高度", example = "300")
    @TableField("bbox_height")
    private Integer bboxHeight;

    /**
     * 截图URL
     */
    @Schema(description = "截图URL", example = "https://example.com/snapshot/001.jpg")
    @TableField("snapshot_url")
    private String snapshotUrl;

    /**
     * 特征向量(JSON格式)
     */
    @Schema(description = "特征向量")
    @TableField("feature_vector")
    private String featureVector;

    /**
     * 属性信息(JSON格式)
     */
    @Schema(description = "属性信息")
    @TableField("attributes")
    private String attributes;

    /**
     * 追踪ID(与跟踪关联)
     */
    @Schema(description = "追踪ID", example = "TRK001")
    @TableField("tracking_id")
    private String trackingId;

    /**
     * 区域ID
     */
    @Schema(description = "区域ID", example = "AREA001")
    @TableField("area_id")
    private String areaId;

    /**
     * 场景类型
     */
    @Schema(description = "场景类型", example = "entrance")
    @TableField("scene_type")
    private String sceneType;

    /**
     * 状态(0-无效,1-有效)
     */
    @Schema(description = "状态", example = "1")
    @TableField("status")
    private Integer status;

    /**
     * 备注
     */
    @Schema(description = "备注")
    @TableField("remark")
    private String remark;
}
