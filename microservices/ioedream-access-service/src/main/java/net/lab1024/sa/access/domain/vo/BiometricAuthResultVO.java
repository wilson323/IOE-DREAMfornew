package net.lab1024.sa.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 生物识别认证结果视图对象
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "生物识别认证结果视图对象")
public class BiometricAuthResultVO {

    @Schema(description = "验证是否成功", example = "true")
    private Boolean success;

    @Schema(description = "模板ID", example = "1001")
    private Long templateId;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "模板名称", example = "用户人脸特征模板")
    private String templateName;

    @Schema(description = "生物识别类型描述", example = "人脸识别")
    private String biometricTypeDesc;

    @Schema(description = "匹配得分", example = "0.95")
    private Double matchScore;

    @Schema(description = "活体检测是否通过", example = "true")
    private Boolean livenessPassed;

    @Schema(description = "活体检测得分", example = "0.92")
    private Double livenessScore;

    @Schema(description = "验证耗时(毫秒)", example = "1250")
    private Long duration;

    @Schema(description = "验证结果消息", example = "验证成功")
    private String message;

    @Schema(description = "验证时间", example = "2025-01-30T14:30:00")
    private String authTime;

    @Schema(description = "设备位置", example = "A栋1楼大厅")
    private String deviceLocation;

    @Schema(description = "可疑操作标识", example = "false")
    private Boolean suspiciousOperation;

    @Schema(description = "可疑原因", example = "")
    private String suspiciousReason;

    @Schema(description = "安全级别", example = "2")
    private Integer securityLevel;
}