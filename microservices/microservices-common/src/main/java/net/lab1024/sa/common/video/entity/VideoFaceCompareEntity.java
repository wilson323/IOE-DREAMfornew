package net.lab1024.sa.common.video.entity;

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
 * 人脸比对记录实体
 * 记录1:1人脸验证的结果
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_video_face_compare")
@Schema(description = "人脸比对记录实体")
public class VideoFaceCompareEntity extends BaseEntity {

    /**
     * 比对ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "比对ID", example = "1698745325654325123")
    private Long compareId;

    /**
     * 请求ID（前端请求标识）
     */
    @TableField("request_id")
    @Size(max = 64, message = "请求ID长度不能超过64个字符")
    @Schema(description = "请求ID", example = "REQ202512161030001")
    private String requestId;

    /**
     * 比对类型
     * 1-身份验证 2-活体检测 3-人脸注册验证 4-门禁验证 5-考勤验证
     */
    @TableField("compare_type")
    @NotNull(message = "比对类型不能为空")
    @Min(value = 1, message = "比对类型必须在1-5之间")
    @Max(value = 5, message = "比对类型必须在1-5之间")
    @Schema(description = "比对类型", example = "1")
    private Integer compareType;

    /**
     * 源人脸ID
     */
    @TableField("source_face_id")
    @NotNull(message = "源人脸ID不能为空")
    @Schema(description = "源人脸ID", example = "1698745325654325123")
    private Long sourceFaceId;

    /**
     * 目标人脸ID
     */
    @TableField("target_face_id")
    @Schema(description = "目标人脸ID", example = "1698745325654325124")
    private Long targetFaceId;

    /**
     * 源人脸图片URL
     */
    @TableField("source_face_url")
    @NotNull(message = "源人脸图片URL不能为空")
    @Size(max = 500, message = "源人脸图片URL长度不能超过500个字符")
    @Schema(description = "源人脸图片URL", example = "/uploads/compare/source_1698745325654325123.jpg")
    private String sourceFaceUrl;

    /**
     * 目标人脸图片URL
     */
    @TableField("target_face_url")
    @Size(max = 500, message = "目标人脸图片URL长度不能超过500个字符")
    @Schema(description = "目标人脸图片URL", example = "/uploads/compare/target_1698745325654325124.jpg")
    private String targetFaceUrl;

    /**
     * 源人员ID
     */
    @TableField("source_person_id")
    @Schema(description = "源人员ID", example = "1001")
    private Long sourcePersonId;

    /**
     * 目标人员ID
     */
    @TableField("target_person_id")
    @Schema(description = "目标人员ID", example = "1002")
    private Long targetPersonId;

    /**
     * 源人员姓名
     */
    @TableField("source_person_name")
    @Size(max = 100, message = "源人员姓名长度不能超过100个字符")
    @Schema(description = "源人员姓名", example = "张三")
    private String sourcePersonName;

    /**
     * 目标人员姓名
     */
    @TableField("target_person_name")
    @Size(max = 100, message = "目标人员姓名长度不能超过100个字符")
    @Schema(description = "目标人员姓名", example = "李四")
    private String targetPersonName;

    /**
     * 比对相似度
     */
    @TableField("similarity_score")
    @NotNull(message = "比对相似度不能为空")
    @Min(value = 0, message = "比对相似度不能小于0")
    @Max(value = 100, message = "比对相似度不能大于100")
    @Schema(description = "比对相似度", example = "92.5")
    private BigDecimal similarityScore;

    /**
     * 相似度阈值
     */
    @TableField("similarity_threshold")
    @NotNull(message = "相似度阈值不能为空")
    @Min(value = 0, message = "相似度阈值不能小于0")
    @Max(value = 100, message = "相似度阈值不能大于100")
    @Schema(description = "相似度阈值", example = "80.0")
    private BigDecimal similarityThreshold;

    /**
     * 比对结果
     * 0-未知 1-相同 2-不同
     */
    @TableField("compare_result")
    @NotNull(message = "比对结果不能为空")
    @Min(value = 0, message = "比对结果必须在0-2之间")
    @Max(value = 2, message = "比对结果必须在0-2之间")
    @Schema(description = "比对结果", example = "1")
    private Integer compareResult;

    /**
     * 置信度
     */
    @TableField("confidence_score")
    @Min(value = 0, message = "置信度不能小于0")
    @Max(value = 100, message = "置信度不能大于100")
    @Schema(description = "置信度", example = "95.8")
    private BigDecimal confidenceScore;

    /**
     * 比对算法类型
     * 1-商汤 2-旷视 3-依图 4-百度 5-腾讯优图 6-虹软 7-华为 8-阿里云
     */
    @TableField("algorithm_type")
    @NotNull(message = "算法类型不能为空")
    @Min(value = 1, message = "算法类型必须在1-8之间")
    @Max(value = 8, message = "算法类型必须在1-8之间")
    @Schema(description = "比对算法类型", example = "1")
    private Integer algorithmType;

    /**
     * 算法版本
     */
    @TableField("algorithm_version")
    @Size(max = 32, message = "算法版本长度不能超过32个字符")
    @Schema(description = "算法版本", example = "FaceNet-v4")
    private String algorithmVersion;

    /**
     * 比对耗时（毫秒）
     */
    @TableField("compare_duration")
    @Min(value = 0, message = "比对耗时不能小于0")
    @Schema(description = "比对耗时（毫秒）", example = "150")
    private Long compareDuration;

    /**
     * 设备ID
     */
    @TableField("device_id")
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
     * 用户ID（发起比对的用户）
     */
    @TableField("user_id")
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    /**
     * 用户姓名
     */
    @TableField("user_name")
    @Size(max = 100, message = "用户姓名长度不能超过100个字符")
    @Schema(description = "用户姓名", example = "管理员")
    private String userName;

    /**
     * IP地址
     */
    @TableField("client_ip")
    @Size(max = 64, message = "IP地址长度不能超过64个字符")
    @Schema(description = "IP地址", example = "192.168.1.100")
    private String clientIp;

    /**
     * 用户代理
     */
    @TableField("user_agent")
    @Size(max = 500, message = "用户代理长度不能超过500个字符")
    @Schema(description = "用户代理", example = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
    private String userAgent;

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
    @Schema(description = "活体检测置信度", example = "88.5")
    private BigDecimal livenessConfidence;

    /**
     * 源人脸质量分数
     */
    @TableField("source_quality_score")
    @Min(value = 0, message = "源人脸质量分数不能小于0")
    @Max(value = 100, message = "源人脸质量分数不能大于100")
    @Schema(description = "源人脸质量分数", example = "92.3")
    private BigDecimal sourceQualityScore;

    /**
     * 目标人脸质量分数
     */
    @TableField("target_quality_score")
    @Min(value = 0, message = "目标人脸质量分数不能小于0")
    @Max(value = 100, message = "目标人脸质量分数不能大于100")
    @Schema(description = "目标人脸质量分数", example = "89.7")
    private BigDecimal targetQualityScore;

    /**
     * 特征距离
     */
    @TableField("feature_distance")
    @Min(value = 0, message = "特征距离不能小于0")
    @Max(value = 2, message = "特征距离不能大于2")
    @Schema(description = "特征距离", example = "0.25")
    private BigDecimal featureDistance;

    /**
     * 特征向量余弦相似度
     */
    @TableField("cosine_similarity")
    @Min(value = 0, message = "余弦相似度不能小于0")
    @Max(value = 1, message = "余弦相似度不能大于1")
    @Schema(description = "余弦相似度", example = "0.925")
    private BigDecimal cosineSimilarity;

    /**
     * 错误码
     */
    @TableField("error_code")
    @Size(max = 32, message = "错误码长度不能超过32个字符")
    @Schema(description = "错误码", example = "FACE_TOO_SMALL")
    private String errorCode;

    /**
     * 错误信息
     */
    @TableField("error_message")
    @Size(max = 500, message = "错误信息长度不能超过500个字符")
    @Schema(description = "错误信息", example = "人脸过小，无法进行比对")
    private String errorMessage;

    /**
     * 备注信息
     */
    @TableField("remark")
    @Size(max = 500, message = "备注信息长度不能超过500个字符")
    @Schema(description = "备注信息", example = "门禁授权验证")
    private String remark;

    /**
     * 扩展属性（JSON格式）
     */
    @TableField("extended_attributes")
    @Schema(description = "扩展属性", example = "{\"purpose\":\"access_control\",\"priority\":\"high\"}")
    private String extendedAttributes;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间", example = "2025-12-16T10:30:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间", example = "2025-12-16T10:30:00")
    private LocalDateTime updateTime;

    /**
     * 删除标志
     */
    @TableLogic
    @TableField("deleted_flag")
    @Schema(description = "删除标志", example = "0")
    private Integer deletedFlag;
}