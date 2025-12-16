package net.lab1024.sa.access.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 生物识别模板实体
 * 存储用户生物识别特征模板数据
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_access_biometric_template")
@Schema(description = "生物识别模板实体")
public class BiometricTemplateEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "模板ID", example = "1001")
    private Long templateId;

    @NotNull
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @NotNull
    @Schema(description = "生物识别类型", example = "1", allowableValues = {"1", "2", "3"})
    private Integer biometricType;

    @NotBlank
    @Size(max = 100)
    @Schema(description = "模板名称", example = "用户人脸特征模板")
    private String templateName;

    @NotNull
    @Schema(description = "模板状态", example = "1", allowableValues = {"1", "2", "3"})
    private Integer templateStatus;

    @Schema(description = "特征数据", example = "生物识别特征数据(Base64编码)")
    private String featureData;

    @Schema(description = "特征向量", example = "AI算法生成的特征向量")
    private String featureVector;

    @Schema(description = "匹配阈值", example = "0.85")
    private Double matchThreshold;

    @Schema(description = "活体检测配置", example = "{\"enabled\":true,\"liveness\":0.9}")
    private String livenessConfig;

    @Schema(description = "算法版本", example = "v2.1.0")
    private String algorithmVersion;

    @Schema(description = "设备ID", example = "DEVICE_001")
    private String deviceId;

    @Schema(description = "采集时间", example = "2025-01-30T14:30:00")
    private LocalDateTime captureTime;

    @Schema(description = "过期时间", example = "2026-01-30T14:30:00")
    private LocalDateTime expireTime;

    @Schema(description = "使用次数", example = "156")
    private Integer useCount;

    @Schema(description = "验证成功次数", example = "152")
    private Integer successCount;

    @Schema(description = "验证失败次数", example = "4")
    private Integer failCount;

    @Schema(description = "上次使用时间", example = "2025-01-30T09:15:00")
    private LocalDateTime lastUseTime;

    @Schema(description = "图片路径", example = "/biometric/face/1001_20250130.jpg")
    private String imagePath;

    @Schema(description = "扩展属性", example = "{\"quality\":0.95,\"lighting\":\"good\"}")
    private String extendedAttributes;

    // 枚举定义
    public enum BiometricType {
        FACE(1, "人脸识别"),
        FINGERPRINT(2, "指纹识别"),
        IRIS(3, "虹膜识别"),
        VOICE(4, "声纹识别"),
        PALM(5, "掌纹识别");

        private final int code;
        private final String description;

        BiometricType(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public int getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static BiometricType fromCode(int code) {
            for (BiometricType type : values()) {
                if (type.code == code) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Invalid biometric type code: " + code);
        }
    }

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