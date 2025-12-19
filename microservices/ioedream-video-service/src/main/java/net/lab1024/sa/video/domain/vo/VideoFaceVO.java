package net.lab1024.sa.video.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 人脸信息视图对象
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
@Data
@Accessors(chain = true)
@Schema(description = "人脸信息视图对象")
public class VideoFaceVO {

    /**
     * 人脸ID
     */
    @Schema(description = "人脸ID", example = "1698745325654325123")
    private Long faceId;

    /**
     * 人员ID
     */
    @Schema(description = "人员ID", example = "1001")
    private Long personId;

    /**
     * 人员编号
     */
    @Schema(description = "人员编号", example = "EMP001")
    private String personCode;

    /**
     * 人员姓名
     */
    @Schema(description = "人员姓名", example = "张三")
    private String personName;

    /**
     * 证件类型
     */
    @Schema(description = "证件类型", example = "1")
    private Integer idCardType;

    /**
     * 证件类型描述
     */
    @Schema(description = "证件类型描述", example = "身份证")
    private String idCardTypeDesc;

    /**
     * 证件号码（脱敏显示）
     */
    @Schema(description = "证件号码", example = "110***********1234")
    private String idCardNumberMasked;

    /**
     * 手机号码（脱敏显示）
     */
    @Schema(description = "手机号码", example = "138****8000")
    private String phoneNumberMasked;

    /**
     * 部门ID
     */
    @Schema(description = "部门ID", example = "1001")
    private Long departmentId;

    /**
     * 部门名称
     */
    @Schema(description = "部门名称", example = "技术部")
    private String departmentName;

    /**
     * 人员类型
     */
    @Schema(description = "人员类型", example = "1")
    private Integer personType;

    /**
     * 人员类型描述
     */
    @Schema(description = "人员类型描述", example = "员工")
    private String personTypeDesc;

    /**
     * 性别
     */
    @Schema(description = "性别", example = "1")
    private Integer gender;

    /**
     * 性别描述
     */
    @Schema(description = "性别描述", example = "男")
    private String genderDesc;

    /**
     * 人脸图片URL
     */
    @Schema(description = "人脸图片URL", example = "/uploads/face/1698745325654325123.jpg")
    private String faceImageUrl;

    /**
     * 人脸缩略图URL
     */
    @Schema(description = "人脸缩略图URL", example = "/uploads/face/thumb_1698745325654325123.jpg")
    private String faceThumbnailUrl;

    /**
     * 特征维度
     */
    @Schema(description = "特征维度", example = "512")
    private Integer featureDimension;

    /**
     * 算法类型
     */
    @Schema(description = "算法类型", example = "1")
    private Integer algorithmType;

    /**
     * 算法类型描述
     */
    @Schema(description = "算法类型描述", example = "商汤")
    private String algorithmTypeDesc;

    /**
     * 算法版本
     */
    @Schema(description = "算法版本", example = "v3.2.1")
    private String algorithmVersion;

    /**
     * 人脸质量分数
     */
    @Schema(description = "人脸质量分数", example = "95.5")
    private BigDecimal faceQualityScore;

    /**
     * 人脸质量等级
     */
    @Schema(description = "人脸质量等级", example = "优秀")
    private String faceQualityGrade;

    /**
     * 活体检测通过标志
     */
    @Schema(description = "活体检测通过标志", example = "1")
    private Integer livenessCheck;

    /**
     * 活体检测描述
     */
    @Schema(description = "活体检测描述", example = "通过")
    private String livenessCheckDesc;

    /**
     * 人脸角度
     */
    @Schema(description = "人脸角度", example = "0")
    private Integer faceAngle;

    /**
     * 人脸置信度
     */
    @Schema(description = "人脸置信度", example = "98.5")
    private BigDecimal faceConfidence;

    /**
     * 人脸区域坐标
     */
    @Schema(description = "人脸区域坐标", example = "{\"x\":100,\"y\":200,\"w\":150,\"h\":180}")
    private String faceRegion;

    /**
     * 人脸采集设备ID
     */
    @Schema(description = "人脸采集设备ID", example = "1001")
    private Long captureDeviceId;

    /**
     * 人脸采集设备名称
     */
    @Schema(description = "人脸采集设备名称", example = "主入口摄像头")
    private String captureDeviceName;

    /**
     * 人脸采集时间
     */
    @Schema(description = "人脸采集时间", example = "2025-12-16T10:30:00")
    private LocalDateTime captureTime;

    /**
     * 采集时间描述
     */
    @Schema(description = "采集时间描述", example = "2025-12-16 10:30:00")
    private String captureTimeDesc;

    /**
     * 有效期开始时间
     */
    @Schema(description = "有效期开始时间", example = "2025-01-01T00:00:00")
    private LocalDateTime validStartTime;

    /**
     * 有效期结束时间
     */
    @Schema(description = "有效期结束时间", example = "2026-12-31T23:59:59")
    private LocalDateTime validEndTime;

    /**
     * 剩余有效期天数
     */
    @Schema(description = "剩余有效期天数", example = "376")
    private Integer remainingValidDays;

    /**
     * 是否已过期
     */
    @Schema(description = "是否已过期", example = "false")
    private Boolean isExpired;

    /**
     * 备注信息
     */
    @Schema(description = "备注信息", example = "VIP客户，需要重点关注")
    private String remark;

    /**
     * 同步标志
     */
    @Schema(description = "同步标志", example = "1")
    private Integer syncFlag;

    /**
     * 同步状态描述
     */
    @Schema(description = "同步状态描述", example = "已同步")
    private String syncFlagDesc;

    /**
     * 最后同步时间
     */
    @Schema(description = "最后同步时间", example = "2025-12-16T10:30:00")
    private LocalDateTime lastSyncTime;

    /**
     * 人脸状态
     */
    @Schema(description = "人脸状态", example = "1")
    private Integer faceStatus;

    /**
     * 人脸状态描述
     */
    @Schema(description = "人脸状态描述", example = "正常")
    private String faceStatusDesc;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2025-12-16T10:30:00")
    private LocalDateTime createTime;

    /**
     * 创建时间描述
     */
    @Schema(description = "创建时间描述", example = "2025-12-16 10:30:00")
    private String createTimeDesc;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2025-12-16T10:30:00")
    private LocalDateTime updateTime;

    /**
     * 更新人ID
     */
    @Schema(description = "更新人ID", example = "1001")
    private Long updateUserId;

    /**
     * 更新人姓名
     */
    @Schema(description = "更新人姓名", example = "管理员")
    private String updateUserName;

    /**
     * 扩展属性
     */
    @Schema(description = "扩展属性", example = "{\"vipLevel\":\"gold\",\"accessLevel\":\"high\"}")
    private String extendedAttributes;

    /**
     * VIP等级
     */
    @Schema(description = "VIP等级", example = "gold")
    private String vipLevel;

    /**
     * 访问级别
     */
    @Schema(description = "访问级别", example = "high")
    private String accessLevel;

    /**
     * 最后检测时间
     */
    @Schema(description = "最后检测时间", example = "2025-12-16T15:30:00")
    private LocalDateTime lastDetectionTime;

    /**
     * 检测次数统计
     */
    @Schema(description = "检测次数统计", example = "156")
    private Integer detectionCount;

    /**
     * 近期活跃度
     */
    @Schema(description = "近期活跃度", example = "高")
    private String recentActivity;
}