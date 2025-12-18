package net.lab1024.sa.access.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 生物识别配置实体
 * 存储生物识别系统的配置参数
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_access_biometric_config")
@Schema(description = "生物识别配置实体")
public class BiometricConfigEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "配置ID", example = "1")
    private Long configId;

    @NotNull
    @Schema(description = "生物识别类型", example = "1", allowableValues = {"1", "2", "3"})
    private Integer biometricType;

    @NotBlank
    @Size(max = 100)
    @Schema(description = "配置名称", example = "人脸识别系统配置")
    private String configName;

    @NotNull
    @Schema(description = "配置状态", example = "1", allowableValues = {"1", "2", "3"})
    private Integer configStatus;

    @NotNull
    @Schema(description = "算法提供商", example = "1", allowableValues = {"1", "2", "3"})
    private Integer algorithmProvider;

    @Schema(description = "算法模型名称", example = "FaceNet-ResNet50")
    private String algorithmModel;

    @Schema(description = "算法模型路径", example = "/models/facenet_resnet50.onnx")
    private String modelPath;

    @Schema(description = "算法配置参数", example = "{\"input_size\":[224,224],\"batch_size\":1}")
    private String algorithmConfig;

    @NotNull
    @Schema(description = "匹配阈值", example = "0.85")
    private Double matchThreshold;

    @Schema(description = "活体检测启用", example = "true")
    private Boolean livenessEnabled;

    @Schema(description = "活体检测算法", example = "1", allowableValues = {"1", "2", "3"})
    private Integer livenessAlgorithm;

    @Schema(description = "活体检测阈值", example = "0.8")
    private Double livenessThreshold;

    @Schema(description = "活体检测配置", example = "{\"challenge\":true,\"blink\":true,\"smile\":true}")
    private String livenessConfig;

    @Schema(description = "多因子验证启用", example = "true")
    private Boolean multiFactorEnabled;

    @Schema(description = "备用验证方式", example = "2")
    private Integer backupAuthType;

    @Schema(description = "数据加密启用", example = "true")
    private Boolean dataEncryptionEnabled;

    @Schema(description = "加密密钥版本", example = "v1.0")
    private String encryptionKeyVersion;

    @Schema(description = "图片质量检查", example = "true")
    private Boolean imageQualityCheck;

    @Schema(description = "最小图片分辨率", example = "640")
    private Integer minImageResolution;

    @Schema(description = "图片质量阈值", example = "0.6")
    private Double imageQualityThreshold;

    @Schema(description = "最大验证时间（秒）", example = "5")
    private Integer maxAuthTime;

    @Schema(description = "重试次数限制", example = "3")
    private Integer retryLimit;

    @Schema(description = "锁定时间(分钟)", example = "30")
    private Integer lockoutDuration;

    @Schema(description = "日志记录级别", example = "2", allowableValues = {"1", "2", "3"})
    private Integer logLevel;

    @Schema(description = "审计记录启用", example = "true")
    private Boolean auditEnabled;

    @Schema(description = "实时监控启用", example = "true")
    private Boolean realTimeMonitoring;

    @Schema(description = "性能监控启用", example = "true")
    private Boolean performanceMonitoring;

    @Schema(description = "自动更新启用", example = "true")
    private Boolean autoUpdateEnabled;

    @Schema(description = "更新检查间隔(小时)", example = "24")
    private Integer updateCheckInterval;

    @Schema(description = "配置版本", example = "1.0")
    private String configVersion;

    @Schema(description = "生效时间", example = "2025-01-30T00:00:00")
    private LocalDateTime effectiveTime;

    @Schema(description = "失效时间", example = "2025-12-31T23:59:59")
    private LocalDateTime expireTime;

    @Schema(description = "备注", example = "人脸识别系统主配置")
    private String remarks;

    // 枚举定义
    public enum AlgorithmProvider {
        OPENCV(1, "OpenCV"),
        FACE_SDK(2, "Face SDK"),
        COMMERCIAL(3, "商业算法"),
        DEEP_LEARNING(4, "深度学习"),
        CUSTOM(5, "自定义");

        private final int code;
        private final String description;

        AlgorithmProvider(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public int getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static AlgorithmProvider fromCode(int code) {
            for (AlgorithmProvider provider : values()) {
                if (provider.code == code) {
                    return provider;
                }
            }
            throw new IllegalArgumentException("Invalid algorithm provider code: " + code);
        }
    }

    public enum ConfigStatus {
        DRAFT(1, "草稿"),
        ACTIVE(2, "激活"),
        INACTIVE(3, "未激活"),
        DEPRECATED(4, "已弃用");

        private final int code;
        private final String description;

        ConfigStatus(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public int getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static ConfigStatus fromCode(int code) {
            for (ConfigStatus status : values()) {
                if (status.code == code) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Invalid config status code: " + code);
        }
    }
}