package net.lab1024.sa.base.module.biometric.entity;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.entity.BaseEntity;

/**
 * 生物特征模板实体
 * <p>
 * 存储人员的具体生物特征模板数据，支持多种生物特征类型
 * 所有敏感数据均采用国密SM4算法加密存储，确保安全性
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-24
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_biometric_template")
public class BiometricTemplateEntity extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 人员ID
     */
    private Long personId;

    /**
     * 生物特征类型
     * FACE-人脸识别
     * FINGERPRINT-指纹识别
     * IRIS-虹膜识别
     * PALMPRINT-掌纹识别
     * VOICE-声纹识别
     */
    private String biometricType;

    /**
     * 模板索引（同一类型的多个模板）
     * 同一人员可以有多个同类型的模板，提高识别成功率
     */
    private Integer templateIndex;

    /**
     * 特征模板数据（SM4加密存储）
     * 存储加密后的生物特征向量或模板数据
     */
    private String templateData;

    /**
     * 模板版本号
     * 用于版本管理和算法升级
     */
    private String templateVersion;

    /**
     * 算法信息（JSON格式）
     * {
     *   "algorithm": "FaceNet-v2",
     *   "version": "2.1.0",
     *   "modelVersion": "v20241201",
     *   "provider": "旷视",
     *   "confidence": 0.95,
     *   "featureDimension": 512,
     *   "compressionRatio": 0.75
     * }
     */
    private String algorithmInfo;

    /**
     * 质量分数
     * 0.00-1.00，评估模板质量
     */
    private Double qualityScore;

    /**
     * 注册设备ID
     * 记录注册时使用的设备
     */
    private Long enrollmentDeviceId;

    /**
     * 注册设备信息（JSON格式）
     * {
     *   "deviceId": "DEV001",
     *   "deviceType": "FACE_RECOGNITION",
     *   "deviceModel": "FR-2000",
     * "manufacturer": "海康威视",
     *   "captureResolution": "1920x1080",
     *   "captureDistance": "0.5m",
     *   "lightingCondition": "NORMAL",
     *   "environment": "INDOOR"
     * }
     */
    private String enrollmentDeviceInfo;

    /**
     * 采集时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime captureTime;

    /**
     * 安全元数据（JSON格式）
     * {
     *   "encryption": "SM4",
     *   "keyVersion": "v1.0",
     *   "encryptionTime": "2025-11-24T10:30:00",
     *   "checksum": "abc123def456",
     *   "securityLevel": "HIGH",
     *   "dataIntegrity": "VERIFIED"
     * }
     */
    private String securityMetadata;

    /**
     * 有效期开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime validFrom;

    /**
     * 有效期结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime validTo;

    /**
     * 模板状态
     * ACTIVE-活跃：正常使用
     * INACTIVE-非活跃：暂时禁用
     * EXPIRED-已过期：超过有效期
     * FROZEN-冻结：安全原因冻结
     * CORRUPTED-损坏：数据损坏
     */
    private String templateStatus;

    /**
     * 使用次数
     * 记录模板被使用的总次数
     */
    private Integer usageCount;

    /**
     * 最后使用时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastUsedTime;

    /**
     * 失败次数
     * 记录认证失败或异常的次数
     */
    private Integer failureCount;

    /**
     * 是否主模板
     * 0-否
     * 1-是（同一类型中的默认模板）
     */
    private Integer isPrimary;

    /**
     * 是否启用
     * 0-禁用
     * 1-启用
     */
    private Integer enableStatus;

    /**
     * 备注
     */
    private String remark;

    /**
     * 更新最后更新时间
     */
    public void updateLastUpdateTime() {
        this.setUpdateTime(LocalDateTime.now());
    }

    /**
     * 生物特征类型枚举
     */
    public enum BiometricType {
        FACE("FACE", "人脸识别"),
        FINGERPRINT("FINGERPRINT", "指纹识别"),
        IRIS("IRIS", "虹膜识别"),
        PALMPRINT("PALMPRINT", "掌纹识别"),
        VOICE("VOICE", "声纹识别");

        private final String value;
        private final String description;

        BiometricType(String value, String description) {
            this.value = value;
            this.description = description;
        }

        public String getValue() {
            return value;
        }

        public String getDescription() {
            return description;
        }

        public static BiometricType fromValue(String value) {
            for (BiometricType type : values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown biometric type: " + value);
        }
    }

    /**
     * 模板状态枚举
     */
    public enum TemplateStatus {
        ACTIVE("ACTIVE", "活跃"),
        INACTIVE("INACTIVE", "非活跃"),
        EXPIRED("EXPIRED", "已过期"),
        FROZEN("FROZEN", "冻结"),
        CORRUPTED("CORRUPTED", "损坏");

        private final String value;
        private final String description;

        TemplateStatus(String value, String description) {
            this.value = value;
            this.description = description;
        }

        public String getValue() {
            return value;
        }

        public String getDescription() {
            return description;
        }

        public static TemplateStatus fromValue(String value) {
            for (TemplateStatus status : values()) {
                if (status.value.equals(value)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Unknown template status: " + value);
        }
    }

    /**
     * 检查模板是否过期
     */
    public boolean isExpired() {
        if (validTo == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(validTo);
    }

    /**
     * 检查模板是否在有效期内
     */
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return (validFrom == null || !now.isBefore(validFrom))
                && (validTo == null || !now.isAfter(validTo));
    }

    /**
     * 检查模板是否活跃
     */
    public boolean isActive() {
        return TemplateStatus.ACTIVE.getValue().equals(templateStatus) && isValid();
    }

    /**
     * 检查是否为主模板
     */
    public boolean isPrimary() {
        return isPrimary != null && isPrimary == 1;
    }

    /**
     * 检查模板质量是否优秀
     */
    public boolean isHighQuality() {
        return qualityScore != null && qualityScore >= 0.9;
    }

    /**
     * 检查模板质量是否合格
     */
    public boolean isAcceptableQuality() {
        return qualityScore != null && qualityScore >= 0.8;
    }

    /**
     * 检查模板是否需要重新注册
     */
    public boolean needsReEnrollment() {
        return isExpired()
                || TemplateStatus.CORRUPTED.getValue().equals(templateStatus)
                || !isAcceptableQuality();
    }

    /**
     * 增加使用次数
     */
    public void incrementUsageCount() {
        this.usageCount = (this.usageCount == null ? 0 : this.usageCount) + 1;
        this.lastUsedTime = LocalDateTime.now();
    }

    /**
     * 增加失败次数
     */
    public void incrementFailureCount() {
        this.failureCount = (this.failureCount == null ? 0 : this.failureCount) + 1;

        // 如果失败次数过多，自动冻结模板
        if (this.failureCount >= 10) {
            this.templateStatus = TemplateStatus.FROZEN.getValue();
        }
    }

    /**
     * 重置失败次数
     */
    public void resetFailureCount() {
        this.failureCount = 0;
        if (TemplateStatus.FROZEN.getValue().equals(templateStatus)) {
            this.templateStatus = TemplateStatus.ACTIVE.getValue();
        }
    }

    /**
     * 解析算法信息
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> parseAlgorithmInfo() {
        if (algorithmInfo == null || algorithmInfo.trim().isEmpty()) {
            return new HashMap<>();
        }
        try {
            // 这里应该使用JSON解析器，简化实现
            return new HashMap<>();
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    /**
     * 解析设备信息
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> parseDeviceInfo() {
        if (enrollmentDeviceInfo == null || enrollmentDeviceInfo.trim().isEmpty()) {
            return new HashMap<>();
        }
        try {
            // 这里应该使用JSON解析器，简化实现
            return new HashMap<>();
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    /**
     * 解析安全元数据
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> parseSecurityMetadata() {
        if (securityMetadata == null || securityMetadata.trim().isEmpty()) {
            return new HashMap<>();
        }
        try {
            // 这里应该使用JSON解析器，简化实现
            return new HashMap<>();
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    /**
     * 获取算法名称
     */
    public String getAlgorithmName() {
        Map<String, Object> info = parseAlgorithmInfo();
        return (String) info.get("algorithm");
    }

    /**
     * 获取算法版本
     */
    public String getAlgorithmVersion() {
        Map<String, Object> info = parseAlgorithmInfo();
        return (String) info.get("version");
    }

    /**
     * 获取特征维度
     */
    public Integer getFeatureDimension() {
        Map<String, Object> info = parseAlgorithmInfo();
        Object dimension = info.get("featureDimension");
        return dimension instanceof Number ? ((Number) dimension).intValue() : null;
    }

    /**
     * 计算使用成功率
     */
    public double getSuccessRate() {
        int total = (usageCount != null ? usageCount : 0) + (failureCount != null ? failureCount : 0);
        if (total == 0) {
            return 0.0;
        }
        return (double) usageCount / total;
    }

    /**
     * 获取使用频率等级
     */
    public String getUsageFrequencyLevel() {
        double rate = getSuccessRate();
        if (rate >= 0.95) {
            return "FREQUENT";
        } else if (rate >= 0.85) {
            return "REGULAR";
        } else if (rate >= 0.75) {
            return "OCCASIONAL";
        } else {
            return "RARE";
        }
    }

    /**
     * 生成模板描述
     */
    public String getTemplateDescription() {
        BiometricType type = BiometricType.fromValue(biometricType);
        StringBuilder desc = new StringBuilder();
        desc.append(type.getDescription())
            .append("模板 #").append(templateIndex)
            .append(" (质量: ").append(String.format("%.2f", qualityScore))
            .append(")");

        if (isPrimary()) {
            desc.append(" [主模板]");
        }

        if (isExpired()) {
            desc.append(" [已过期]");
        }

        return desc.toString();
    }
}