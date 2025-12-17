package net.lab1024.sa.video.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * 人脸库实体
 * 存储人脸特征和基本信息，支持多算法接入
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_video_face")
@Schema(description = "人脸库实体")
public class VideoFaceEntity extends BaseEntity {

    /**
     * 人脸ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "人脸ID", example = "1698745325654325123")
    private Long faceId;

    /**
     * 人员ID（关联人员信息）
     */
    @TableField("person_id")
    @NotNull(message = "人员ID不能为空")
    @Schema(description = "人员ID", example = "1001")
    private Long personId;

    /**
     * 人员编号
     */
    @TableField("person_code")
    @NotBlank(message = "人员编号不能为空")
    @Size(max = 64, message = "人员编号长度不能超过64个字符")
    @Schema(description = "人员编号", example = "EMP001")
    private String personCode;

    /**
     * 人员姓名
     */
    @TableField("person_name")
    @NotBlank(message = "人员姓名不能为空")
    @Size(max = 100, message = "人员姓名长度不能超过100个字符")
    @Schema(description = "人员姓名", example = "张三")
    private String personName;

    /**
     * 证件类型
     * 1-身份证 2-护照 3-军官证 4-港澳通行证 5-台湾通行证
     */
    @TableField("id_card_type")
    @Min(value = 1, message = "证件类型必须在1-5之间")
    @Max(value = 5, message = "证件类型必须在1-5之间")
    @Schema(description = "证件类型", example = "1")
    private Integer idCardType;

    /**
     * 证件号码
     */
    @TableField("id_card_number")
    @Size(max = 64, message = "证件号码长度不能超过64个字符")
    @Schema(description = "证件号码", example = "110101199001011234")
    private String idCardNumber;

    /**
     * 手机号码
     */
    @TableField("phone_number")
    @Size(max = 32, message = "手机号码长度不能超过32个字符")
    @Schema(description = "手机号码", example = "13800138000")
    private String phoneNumber;

    /**
     * 部门ID
     */
    @TableField("department_id")
    @Schema(description = "部门ID", example = "1001")
    private Long departmentId;

    /**
     * 部门名称
     */
    @TableField("department_name")
    @Size(max = 200, message = "部门名称长度不能超过200个字符")
    @Schema(description = "部门名称", example = "技术部")
    private String departmentName;

    /**
     * 人员类型
     * 1-员工 2-访客 3-黑名单 4-白名单 5-重点关注
     */
    @TableField("person_type")
    @Min(value = 1, message = "人员类型必须在1-5之间")
    @Max(value = 5, message = "人员类型必须在1-5之间")
    @Schema(description = "人员类型", example = "1")
    private Integer personType;

    /**
     * 性别
     * 0-未知 1-男 2-女
     */
    @TableField("gender")
    @Min(value = 0, message = "性别必须在0-2之间")
    @Max(value = 2, message = "性别必须在0-2之间")
    @Schema(description = "性别", example = "1")
    private Integer gender;

    /**
     * 人脸图片URL
     */
    @TableField("face_image_url")
    @NotBlank(message = "人脸图片URL不能为空")
    @Size(max = 500, message = "人脸图片URL长度不能超过500个字符")
    @Schema(description = "人脸图片URL", example = "/uploads/face/1698745325654325123.jpg")
    private String faceImageUrl;

    /**
     * 人脸特征向量（Base64编码）
     */
    @TableField("face_feature")
    @Schema(description = "人脸特征向量", example = "eyJmZWF0dXJlIjogWzAuMTIzLCAwLjQ1NiwgLi4uXX0=")
    private String faceFeature;

    /**
     * 特征维度
     */
    @TableField("feature_dimension")
    @Min(value = 128, message = "特征维度不能小于128")
    @Max(value = 2048, message = "特征维度不能大于2048")
    @Schema(description = "特征维度", example = "512")
    private Integer featureDimension;

    /**
     * 算法类型
     * 1-商汤 2-旷视 3-依图 4-百度 5-腾讯优图 6-虹软 7-华为 8-阿里云
     */
    @TableField("algorithm_type")
    @Min(value = 1, message = "算法类型必须在1-8之间")
    @Max(value = 8, message = "算法类型必须在1-8之间")
    @Schema(description = "算法类型", example = "1")
    private Integer algorithmType;

    /**
     * 算法版本
     */
    @TableField("algorithm_version")
    @Size(max = 32, message = "算法版本长度不能超过32个字符")
    @Schema(description = "算法版本", example = "v3.2.1")
    private String algorithmVersion;

    /**
     * 人脸质量分数
     */
    @TableField("face_quality_score")
    @Min(value = 0, message = "人脸质量分数不能小于0")
    @Max(value = 100, message = "人脸质量分数不能大于100")
    @Schema(description = "人脸质量分数", example = "95.5")
    private BigDecimal faceQualityScore;

    /**
     * 活体检测通过标志
     * 0-未检测 1-通过 2-未通过
     */
    @TableField("liveness_check")
    @Min(value = 0, message = "活体检测标志必须在0-2之间")
    @Max(value = 2, message = "活体检测标志必须在0-2之间")
    @Schema(description = "活体检测通过标志", example = "1")
    private Integer livenessCheck;

    /**
     * 人脸角度（相对于摄像头的角度）
     */
    @TableField("face_angle")
    @Min(value = -180, message = "人脸角度不能小于-180度")
    @Max(value = 180, message = "人脸角度不能大于180度")
    @Schema(description = "人脸角度", example = "0")
    private Integer faceAngle;

    /**
     * 人脸置信度
     */
    @TableField("face_confidence")
    @Min(value = 0, message = "人脸置信度不能小于0")
    @Max(value = 100, message = "人脸置信度不能大于100")
    @Schema(description = "人脸置信度", example = "98.5")
    private BigDecimal faceConfidence;

    /**
     * 人脸区域坐标（JSON格式：x,y,w,h）
     */
    @TableField("face_region")
    @Schema(description = "人脸区域坐标", example = "{\"x\":100,\"y\":200,\"w\":150,\"h\":180}")
    private String faceRegion;

    /**
     * 人脸采集设备ID
     */
    @TableField("capture_device_id")
    @Schema(description = "人脸采集设备ID", example = "1001")
    private Long captureDeviceId;

    /**
     * 人脸采集时间
     */
    @TableField("capture_time")
    @Schema(description = "人脸采集时间", example = "2025-12-16T10:30:00")
    private LocalDateTime captureTime;

    /**
     * 有效期开始时间
     */
    @TableField("valid_start_time")
    @Schema(description = "有效期开始时间", example = "2025-01-01T00:00:00")
    private LocalDateTime validStartTime;

    /**
     * 有效期结束时间
     */
    @TableField("valid_end_time")
    @Schema(description = "有效期结束时间", example = "2026-12-31T23:59:59")
    private LocalDateTime validEndTime;

    /**
     * 备注信息
     */
    @TableField("remark")
    @Size(max = 500, message = "备注信息长度不能超过500个字符")
    @Schema(description = "备注信息", example = "VIP客户，需要重点关注")
    private String remark;

    /**
     * 扩展属性（JSON格式）
     */
    @TableField("extended_attributes")
    @Schema(description = "扩展属性", example = "{\"vipLevel\":\"gold\",\"accessLevel\":\"high\"}")
    private String extendedAttributes;

    /**
     * 同步标志
     * 0-未同步 1-已同步 2-同步失败
     */
    @TableField("sync_flag")
    @Min(value = 0, message = "同步标志必须在0-2之间")
    @Max(value = 2, message = "同步标志必须在0-2之间")
    @Schema(description = "同步标志", example = "1")
    private Integer syncFlag;

    /**
     * 最后同步时间
     */
    @TableField("last_sync_time")
    @Schema(description = "最后同步时间", example = "2025-12-16T10:30:00")
    private LocalDateTime lastSyncTime;

    /**
     * 人脸状态
     * 1-正常 2-禁用 3-过期 4-审核中 5-审核拒绝
     */
    @TableField("face_status")
    @Min(value = 1, message = "人脸状态必须在1-5之间")
    @Max(value = 5, message = "人脸状态必须在1-5之间")
    @Schema(description = "人脸状态", example = "1")
    private Integer faceStatus;

    /**
     * 创建人ID
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建人ID", example = "1001")
    private Long createUserId;

    /**
     * 更新人ID
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新人ID", example = "1001")
    private Long updateUserId;

    /**
     * 删除标志
     */
    @TableLogic
    @TableField("deleted_flag")
    @Schema(description = "删除标志", example = "0")
    private Integer deletedFlag;
}
