package net.lab1024.sa.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 生物识别认证结果视图对象
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用VO后缀命名
 * - 用于Controller返回认证结果
 * - 包含完整的生物识别验证信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "生物识别认证结果视图对象")
public class BiometricAuthResultVO {

    /**
     * 认证是否成功
     */
    @Schema(description = "认证是否成功", example = "true")
    private Boolean success;

    /**
     * 模板ID
     */
    @Schema(description = "模板ID", example = "1001")
    private Long templateId;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    /**
     * 模板名称
     */
    @Schema(description = "模板名称", example = "用户人脸特征模板")
    private String templateName;

    /**
     * 生物识别类型描述
     */
    @Schema(description = "生物识别类型描述", example = "人脸识别")
    private String biometricTypeDesc;

    /**
     * 匹配得分
     */
    @Schema(description = "匹配得分", example = "0.95")
    private Double matchScore;

    /**
     * 活体检测是否通过
     */
    @Schema(description = "活体检测是否通过", example = "true")
    private Boolean livenessPassed;

    /**
     * 活体检测得分
     */
    @Schema(description = "活体检测得分", example = "0.92")
    private Double livenessScore;

    /**
     * 认证耗时(毫秒)
     */
    @Schema(description = "认证耗时(毫秒)", example = "1250")
    private Long duration;

    /**
     * 认证结果消息
     */
    @Schema(description = "认证结果消息", example = "认证成功")
    private String message;

    /**
     * 认证时间
     */
    @Schema(description = "认证时间", example = "2025-01-30T14:30:00")
    private String authTime;

    /**
     * 设备位置
     */
    @Schema(description = "设备位置", example = "A栋一楼大厅")
    private String deviceLocation;

    /**
     * 可疑操作标识
     */
    @Schema(description = "可疑操作标识", example = "false")
    private Boolean suspiciousOperation;

    /**
     * 可疑原因
     */
    @Schema(description = "可疑原因", example = "")
    private String suspiciousReason;

    /**
     * 安全级别
     */
    @Schema(description = "安全级别", example = "2")
    private Integer securityLevel;
}