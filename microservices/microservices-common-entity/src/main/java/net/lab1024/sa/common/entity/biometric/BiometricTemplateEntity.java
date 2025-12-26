package net.lab1024.sa.common.entity.biometric;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import net.lab1024.sa.common.entity.BaseEntity;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 生物识别模板实体
 * <p>
 * 存储用户生物特征模板数据，用于设备下发和权限管理
 * 严格遵循CLAUDE.md规范:
 * - 继承BaseEntity获取公共字段
 * - 使用@TableName指定数据库表名
 * - 完整的字段验证和文档
 * </p>
 * <p>
 * 表名: t_biometric_template
 * 说明: 生物模板管理服务专用表，区别于access-service的t_access_biometric_template
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_biometric_template")
@Schema(description = "生物识别模板实体")
public class BiometricTemplateEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "模板ID", example = "1001")
    private Long templateId;

    @NotNull
    @Schema(description = "用户ID", example = "1001")
    @TableField("user_id")
    private Long userId;

    @NotNull
    @Schema(description = "生物识别类型", example = "1", allowableValues = {"1", "2", "3", "4", "5"})
    @TableField("biometric_type")
    private Integer biometricType;

    @NotBlank
    @Size(max = 100)
    @Schema(description = "模板名称", example = "用户人脸特征模板")
    @TableField("template_name")
    private String templateName;

    @NotNull
    @Schema(description = "模板状态", example = "1", allowableValues = {"1", "2", "3", "4"})
    @TableField("template_status")
    private Integer templateStatus;

    @Schema(description = "特征数据", example = "生物识别特征数据(Base64编码)")
    @TableField("feature_data")
    private String featureData;

    @Schema(description = "特征向量", example = "AI算法生成的特征向量")
    @TableField("feature_vector")
    private String featureVector;

    @Schema(description = "匹配阈值", example = "0.85")
    @TableField("match_threshold")
    private Double matchThreshold;

    @Schema(description = "活体检测配置", example = "{\"enabled\":true,\"liveness\":0.9}")
    @TableField("liveness_config")
    private String livenessConfig;

    @Schema(description = "算法版本", example = "v2.1.0")
    @TableField("algorithm_version")
    private String algorithmVersion;

    @Schema(description = "设备ID", example = "DEVICE_001")
    @TableField("device_id")
    private String deviceId;

    @Schema(description = "采集时间", example = "2025-01-30T14:30:00")
    @TableField("capture_time")
    private LocalDateTime captureTime;

    @Schema(description = "过期时间", example = "2026-01-30T14:30:00")
    @TableField("expire_time")
    private LocalDateTime expireTime;

    @Schema(description = "使用次数", example = "156")
    @TableField("use_count")
    private Integer useCount;

    @Schema(description = "验证成功次数", example = "152")
    @TableField("success_count")
    private Integer successCount;

    @Schema(description = "验证失败次数", example = "4")
    @TableField("fail_count")
    private Integer failCount;

    @Schema(description = "上次使用时间", example = "2025-01-30T09:15:00")
    @TableField("last_use_time")
    private LocalDateTime lastUseTime;

    @Schema(description = "图片路径", example = "/biometric/face/1001_20250130.jpg")
    @TableField("image_path")
    private String imagePath;

    @Schema(description = "扩展属性", example = "{\"quality\":0.95,\"lighting\":\"good\"}")
    @TableField("extended_attributes")
    private String extendedAttributes;

    @Schema(description = "质量分数", example = "0.95")
    @TableField("quality_score")
    private Double qualityScore;

    @Schema(description = "模板版本", example = "1.0")
    @TableField("template_version")
    private String templateVersion;

    /**
     * 模板状态枚举
     */
    public enum TemplateStatus {
        ACTIVE(1, "激活"),
        INACTIVE(2, "未激活"),
        EXPIRED(3, "已过期"),
        LOCKED(4, "已锁定");

        private final int code;
        private final String description;

        TemplateStatus(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public int getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static TemplateStatus fromCode(int code) {
            for (TemplateStatus status : values()) {
                if (status.code == code) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Invalid template status code: " + code);
        }
    }
}
