package net.lab1024.sa.video.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.common.domain.PageParam;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 人脸添加表单
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
@Data
@Schema(description = "人脸添加表单")
public class VideoFaceAddForm extends PageParam {

    /**
     * 人员ID
     */
    @Schema(description = "人员ID", example = "1001")
    @NotNull(message = "人员ID不能为空")
    private Long personId;

    /**
     * 人员编号
     */
    @Schema(description = "人员编号", example = "EMP001")
    @NotBlank(message = "人员编号不能为空")
    @Size(max = 64, message = "人员编号长度不能超过64个字符")
    private String personCode;

    /**
     * 人员姓名
     */
    @Schema(description = "人员姓名", example = "张三")
    @NotBlank(message = "人员姓名不能为空")
    @Size(max = 100, message = "人员姓名长度不能超过100个字符")
    private String personName;

    /**
     * 证件类型
     */
    @Schema(description = "证件类型", example = "1")
    @Min(value = 1, message = "证件类型必须在1-5之间")
    @Max(value = 5, message = "证件类型必须在1-5之间")
    private Integer idCardType;

    /**
     * 证件号码
     */
    @Schema(description = "证件号码", example = "110101199001011234")
    @Size(max = 64, message = "证件号码长度不能超过64个字符")
    private String idCardNumber;

    /**
     * 手机号码
     */
    @Schema(description = "手机号码", example = "13800138000")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号码格式不正确")
    private String phoneNumber;

    /**
     * 部门ID
     */
    @Schema(description = "部门ID", example = "1001")
    private Long departmentId;

    /**
     * 部门名称
     */
    @Schema(description = "部门名称", example = "技术部")
    @Size(max = 200, message = "部门名称长度不能超过200个字符")
    private String departmentName;

    /**
     * 人员类型
     */
    @Schema(description = "人员类型", example = "1")
    @Min(value = 1, message = "人员类型必须在1-5之间")
    @Max(value = 5, message = "人员类型必须在1-5之间")
    private Integer personType;

    /**
     * 性别
     */
    @Schema(description = "性别", example = "1")
    @Min(value = 0, message = "性别必须在0-2之间")
    @Max(value = 2, message = "性别必须在0-2之间")
    private Integer gender;

    /**
     * 人脸图片URL
     */
    @Schema(description = "人脸图片URL", example = "/uploads/face/1698745325654325123.jpg")
    @NotBlank(message = "人脸图片URL不能为空")
    @Size(max = 500, message = "人脸图片URL长度不能超过500个字符")
    private String faceImageUrl;

    /**
     * 人脸特征向量
     */
    @Schema(description = "人脸特征向量", example = "eyJmZWF0dXJlIjogWzAuMTIzLCAwLjQ1NiwgLi4uXX0=")
    private String faceFeature;

    /**
     * 特征维度
     */
    @Schema(description = "特征维度", example = "512")
    @Min(value = 128, message = "特征维度不能小于128")
    @Max(value = 2048, message = "特征维度不能大于2048")
    private Integer featureDimension;

    /**
     * 算法类型
     */
    @Schema(description = "算法类型", example = "1")
    @Min(value = 1, message = "算法类型必须在1-8之间")
    @Max(value = 8, message = "算法类型必须在1-8之间")
    private Integer algorithmType;

    /**
     * 算法版本
     */
    @Schema(description = "算法版本", example = "v3.2.1")
    @Size(max = 32, message = "算法版本长度不能超过32个字符")
    private String algorithmVersion;

    /**
     * 人脸质量分数
     */
    @Schema(description = "人脸质量分数", example = "95.5")
    @DecimalMin(value = "0.0", message = "人脸质量分数不能小于0")
    @DecimalMax(value = "100.0", message = "人脸质量分数不能大于100")
    private BigDecimal faceQualityScore;

    /**
     * 活体检测通过标志
     */
    @Schema(description = "活体检测通过标志", example = "1")
    @Min(value = 0, message = "活体检测标志必须在0-2之间")
    @Max(value = 2, message = "活体检测标志必须在0-2之间")
    private Integer livenessCheck;

    /**
     * 人脸角度
     */
    @Schema(description = "人脸角度", example = "0")
    @Min(value = -180, message = "人脸角度不能小于-180度")
    @Max(value = 180, message = "人脸角度不能大于180度")
    private Integer faceAngle;

    /**
     * 人脸置信度
     */
    @Schema(description = "人脸置信度", example = "98.5")
    @DecimalMin(value = "0.0", message = "人脸置信度不能小于0")
    @DecimalMax(value = "100.0", message = "人脸置信度不能大于100")
    private BigDecimal faceConfidence;

    /**
     * 人脸区域坐标
     */
    @Schema(description = "人脸区域坐标", example = "{\"x\":100,\"y\":200,\"w\":150,\"h\":180}")
    @Size(max = 200, message = "人脸区域坐标长度不能超过200个字符")
    private String faceRegion;

    /**
     * 人脸采集设备ID
     */
    @Schema(description = "人脸采集设备ID", example = "1001")
    private Long captureDeviceId;

    /**
     * 人脸采集时间
     */
    @Schema(description = "人脸采集时间", example = "2025-12-16T10:30:00")
    private LocalDateTime captureTime;

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
     * 备注信息
     */
    @Schema(description = "备注信息", example = "VIP客户，需要重点关注")
    @Size(max = 500, message = "备注信息长度不能超过500个字符")
    private String remark;

    /**
     * 扩展属性
     */
    @Schema(description = "扩展属性", example = "{\"vipLevel\":\"gold\",\"accessLevel\":\"high\"}")
    @Size(max = 1000, message = "扩展属性长度不能超过1000个字符")
    private String extendedAttributes;
}