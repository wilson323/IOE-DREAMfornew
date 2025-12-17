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
 * 人脸检测记录实体
 * 记录视频流中实时检测到的人脸信息
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_video_face_detection")
@Schema(description = "人脸检测记录实体")
public class VideoFaceDetectionEntity extends BaseEntity {

    /**
     * 检测ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "检测ID", example = "1698745325654325123")
    private Long detectionId;

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
     * 流ID（视频流标识）
     */
    @TableField("stream_id")
    @Schema(description = "流ID", example = "1698745325654325123")
    private Long streamId;

    /**
     * 检测时间
     */
    @TableField("detection_time")
    @Schema(description = "检测时间", example = "2025-12-16T10:30:00")
    private LocalDateTime detectionTime;

    /**
     * 检测到的人脸数量
     */
    @TableField("face_count")
    @Min(value = 1, message = "人脸数量不能小于1")
    @Max(value = 50, message = "人脸数量不能大于50")
    @Schema(description = "检测到的人脸数量", example = "3")
    private Integer faceCount;

    /**
     * 最大人脸ID（如果识别出具体人员）
     */
    @TableField("matched_face_id")
    @Schema(description = "最大人脸ID", example = "1698745325654325123")
    private Long matchedFaceId;

    /**
     * 匹配的人员ID
     */
    @TableField("matched_person_id")
    @Schema(description = "匹配的人员ID", example = "1001")
    private Long matchedPersonId;

    /**
     * 匹配的人员姓名
     */
    @TableField("matched_person_name")
    @Size(max = 100, message = "匹配的人员姓名长度不能超过100个字符")
    @Schema(description = "匹配的人员姓名", example = "张三")
    private String matchedPersonName;

    /**
     * 最高相似度
     */
    @TableField("max_similarity")
    @Min(value = 0, message = "最高相似度不能小于0")
    @Max(value = 100, message = "最高相似度不能大于100")
    @Schema(description = "最高相似度", example = "95.5")
    private BigDecimal maxSimilarity;

    /**
     * 相似度阈值
     */
    @TableField("similarity_threshold")
    @Min(value = 0, message = "相似度阈值不能小于0")
    @Max(value = 100, message = "相似度阈值不能大于100")
    @Schema(description = "相似度阈值", example = "80.0")
    private BigDecimal similarityThreshold;

    /**
     * 人脸区域坐标（JSON数组：[{\"x\":100,\"y\":200,\"w\":150,\"h\":180}]）
     */
    @TableField("face_regions")
    @Schema(description = "人脸区域坐标", example = "[{\"x\":100,\"y\":200,\"w\":150,\"h\":180}]")
    private String faceRegions;

    /**
     * 检测到的人脸图片URL
     */
    @TableField("face_image_url")
    @Size(max = 500, message = "人脸图片URL长度不能超过500个字符")
    @Schema(description = "检测到的人脸图片URL", example = "/uploads/detection/1698745325654325123.jpg")
    private String faceImageUrl;

    /**
     * 场景图片URL（包含人脸标记的原始场景图）
     */
    @TableField("scene_image_url")
    @Size(max = 500, message = "场景图片URL长度不能超过500个字符")
    @Schema(description = "场景图片URL", example = "/uploads/scene/1698745325654325123.jpg")
    private String sceneImageUrl;

    /**
     * 检测算法类型
     * 1-商汤 2-旷视 3-依图 4-百度 5-腾讯优图 6-虹软 7-华为 8-阿里云
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
    @Size(max = 32, message = "检测模型版本长度不能超过32个字符")
    @Schema(description = "检测模型版本", example = "retinaface_r50_v1.0")
    private String detectionModel;

    /**
     * 人脸质量分数
     */
    @TableField("face_quality_score")
    @Min(value = 0, message = "人脸质量分数不能小于0")
    @Max(value = 100, message = "人脸质量分数不能大于100")
    @Schema(description = "人脸质量分数", example = "85.5")
    private BigDecimal faceQualityScore;

    /**
     * 活体检测结果
     * 0-未检测 1-真人 2-照片 3-视频 4-面具 5-其他攻击
     */
    @TableField("liveness_result")
    @Min(value = 0, message = "活体检测结果必须在0-5之间")
    @Max(value = 5, message = "活体检测结果必须在0-5之间")
    @Schema(description = "活体检测结果", example = "1")
    private Integer livenessResult;

    /**
     * 活体检测置信度
     */
    @TableField("liveness_confidence")
    @Min(value = 0, message = "活体检测置信度不能小于0")
    @Max(value = 100, message = "活体检测置信度不能大于100")
    @Schema(description = "活体检测置信度", example = "92.3")
    private BigDecimal livenessConfidence;

    /**
     * 人脸角度（俯仰角）
     */
    @TableField("face_pitch")
    @Min(value = -90, message = "人脸俯仰角不能小于-90度")
    @Max(value = 90, message = "人脸俯仰角不能大于90度")
    @Schema(description = "人脸俯仰角", example = "5")
    private Integer facePitch;

    /**
     * 人脸角度（偏航角）
     */
    @TableField("face_yaw")
    @Min(value = -90, message = "人脸偏航角不能小于-90度")
    @Max(value = 90, message = "人脸偏航角不能大于90度")
    @Schema(description = "人脸偏航角", example = "-10")
    private Integer faceYaw;

    /**
     * 人脸角度（翻滚角）
     */
    @TableField("face_roll")
    @Min(value = -180, message = "人脸翻滚角不能小于-180度")
    @Max(value = 180, message = "人脸翻滚角不能大于180度")
    @Schema(description = "人脸翻滚角", example = "2")
    private Integer faceRoll;

    /**
     * 年龄估计
     */
    @TableField("estimated_age")
    @Min(value = 0, message = "年龄估计不能小于0")
    @Max(value = 120, message = "年龄估计不能大于120")
    @Schema(description = "年龄估计", example = "25")
    private Integer estimatedAge;

    /**
     * 性别识别
     * 0-未知 1-男 2-女
     */
    @TableField("recognized_gender")
    @Min(value = 0, message = "识别性别必须在0-2之间")
    @Max(value = 2, message = "识别性别必须在0-2之间")
    @Schema(description = "识别性别", example = "1")
    private Integer recognizedGender;

    /**
     * 性别置信度
     */
    @TableField("gender_confidence")
    @Min(value = 0, message = "性别置信度不能小于0")
    @Max(value = 100, message = "性别置信度不能大于100")
    @Schema(description = "性别置信度", example = "88.7")
    private BigDecimal genderConfidence;

    /**
     * 情绪识别
     * 0-未知 1-高兴 2-悲伤 3-愤怒 4-惊讶 5-恐惧 6-厌恶 7-中性
     */
    @TableField("recognized_emotion")
    @Min(value = 0, message = "识别情绪必须在0-7之间")
    @Max(value = 7, message = "识别情绪必须在0-7之间")
    @Schema(description = "识别情绪", example = "1")
    private Integer recognizedEmotion;

    /**
     * 情绪置信度
     */
    @TableField("emotion_confidence")
    @Min(value = 0, message = "情绪置信度不能小于0")
    @Max(value = 100, message = "情绪置信度不能大于100")
    @Schema(description = "情绪置信度", example = "75.2")
    private BigDecimal emotionConfidence;

    /**
     * 人脸检测置信度
     */
    @TableField("detection_confidence")
    @Min(value = 0, message = "人脸检测置信度不能小于0")
    @Max(value = 100, message = "人脸检测置信度不能大于100")
    @Schema(description = "人脸检测置信度", example = "96.8")
    private BigDecimal detectionConfidence;

    /**
     * 是否触发告警
     * 0-未触发 1-已触发
     */
    @TableField("alarm_triggered")
    @Min(value = 0, message = "告警触发标志必须在0-1之间")
    @Max(value = 1, message = "告警触发标志必须在0-1之间")
    @Schema(description = "是否触发告警", example = "1")
    private Integer alarmTriggered;

    /**
     * 告警类型
     */
    @TableField("alarm_types")
    @Size(max = 200, message = "告警类型长度不能超过200个字符")
    @Schema(description = "告警类型", example = "黑名单,陌生人员")
    private String alarmTypes;

    /**
     * 处理状态
     * 0-未处理 1-已确认 2-已忽略 3-处理中
     */
    @TableField("process_status")
    @Min(value = 0, message = "处理状态必须在0-3之间")
    @Max(value = 3, message = "处理状态必须在0-3之间")
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
     * 处理备注
     */
    @TableField("process_remark")
    @Size(max = 500, message = "处理备注长度不能超过500个字符")
    @Schema(description = "处理备注", example = "确认为访客，已登记")
    private String processRemark;

    /**
     * 扩展属性（JSON格式）
     */
    @TableField("extended_attributes")
    @Schema(description = "扩展属性", example = "{\"weather\":\"晴天\",\"lighting\":\"良好\"}")
    private String extendedAttributes;

    /**
     * 删除标志
     */
    @TableLogic
    @TableField("deleted_flag")
    @Schema(description = "删除标志", example = "0")
    private Integer deletedFlag;
}
