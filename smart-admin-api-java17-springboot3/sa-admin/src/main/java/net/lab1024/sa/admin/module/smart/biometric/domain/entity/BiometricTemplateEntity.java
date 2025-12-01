package net.lab1024.sa.admin.module.smart.biometric.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 生物特征模板实体类
 *
 * 存储员工的生物识别特征模板数据，包括人脸、指纹、掌纹、虹膜等
 * 所有敏感数据均采用SM4国密算法加密存储
 *
 * @author SmartAdmin Team
 * @date 2025-01-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_biometric_templates")
public class BiometricTemplateEntity extends BaseEntity {

    /**
     * 模板ID
     */
    @TableId(type = IdType.AUTO)
    private Long templateId;

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 员工姓名（冗余字段，便于查询）
     */
    private String employeeName;

    /**
     * 员工编号（冗余字段，便于查询）
     */
    private String employeeCode;

    /**
     * 生物识别类型
     * FACE-人脸, FINGERPRINT-指纹, PALMPRINT-掌纹, IRIS-虹膜
     */
    private String biometricType;

    /**
     * 模板版本号
     * 用于版本管理和升级
     */
    private String templateVersion;

    /**
     * 模板数据（SM4加密存储）
     * 存储加密后的生物特征向量或模板数据
     */
    private String templateData;

    /**
     * 质量指标（JSON格式）
     * {
     *   "imageQuality": 0.95,
     *   "featureCount": 128,
     *   "signalToNoiseRatio": 25.6,
     *   "sharpness": 0.88,
     *   "brightness": 0.72,
     *   "contrast": 0.65
     * }
     */
    private String qualityMetrics;

    /**
     * 注册日期
     */
    private LocalDate enrollDate;

    /**
     * 最后更新日期
     */
    private java.sql.Timestamp lastUpdateDate;

    /**
     * 模板状态
     * 0-禁用, 1-启用, 2-过期
     */
    private Integer templateStatus;

    /**
     * 安全元数据（JSON格式）
     * {
     *   "algorithm": "SM4",
     *   "keyVersion": "v1.0",
     *   "encryptionTime": "2025-01-15T10:30:00",
     *   "checksum": "abc123def456",
     *   "securityLevel": "HIGH"
     * }
     */
    private String securityMetadata;

    /**
     * 特征维度
     * 特征向量的维度大小
     */
    private Integer featureDimension;

    /**
     * 算法版本
     * 用于标识生成模板的算法版本
     */
    private String algorithmVersion;

    /**
     * 设备信息（JSON格式）
     * 记录注册时使用的设备信息
     * {
     *   "deviceId": "DEV001",
     *   "deviceType": "FACE_RECOGNITION",
     *   "deviceModel": "FR-2000",
     *   "captureResolution": "1920x1080",
     *   "captureDistance": "0.5m"
     * }
     */
    private String deviceInfo;

    /**
     * 注册时的置信度分数
     * 模板质量评估的置信度分数
     */
    private BigDecimal enrollConfidence;

    /**
     * 保质期（天数）
     * 生物特征模板的有效期限
     */
    private Integer validityDays;

    /**
     * 过期日期
     * 基于注册日期和保质期计算得出
     */
    private LocalDate expireDate;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 是否启用备份（0-禁用，1-启用）
     * 是否启用模板备份机制
     */
    private Integer enableBackup;

    /**
     * 备份位置
     * 模板备份的存储位置标识
     */
    private String backupLocation;

    /**
     * 最后使用时间
     * 记录模板最后一次被使用的时间
     */
    private java.sql.Timestamp lastUsedTime;

    /**
     * 使用次数
     * 记录模板被使用的总次数
     */
    private Integer usageCount;

    /**
     * 异常使用次数
     * 记录认证失败或异常的次数
     */
    private Integer abnormalUsageCount;

    /**
     * 冻结状态（0-正常，1-冻结）
     * 由于安全原因冻结的模板
     */
    private Integer frozenStatus;

    /**
     * 冻结原因
     * 模板被冻结的原因说明
     */
    private String freezeReason;

    /**
     * 冻结时间
     */
    private java.sql.Timestamp freezeTime;

    /**
     * 解冻时间
     */
    private java.sql.Timestamp unfreezeTime;

    // 枚举定义
    public enum BiometricType {
        FACE("FACE", "人脸识别"),
        FINGERPRINT("FINGERPRINT", "指纹识别"),
        PALMPRINT("PALMPRINT", "掌纹识别"),
        IRIS("IRIS", "虹膜识别");

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

    public enum TemplateStatus {
        DISABLED(0, "禁用"),
        ENABLED(1, "启用"),
        EXPIRED(2, "过期"),
        FROZEN(3, "冻结");

        private final Integer value;
        private final String description;

        TemplateStatus(Integer value, String description) {
            this.value = value;
            this.description = description;
        }

        public Integer getValue() {
            return value;
        }

        public String getDescription() {
            return description;
        }

        public static TemplateStatus fromValue(Integer value) {
            for (TemplateStatus status : values()) {
                if (status.value.equals(value)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Unknown template status: " + value);
        }
    }

    public enum FrozenStatus {
        NORMAL(0, "正常"),
        FROZEN(1, "冻结");

        private final Integer value;
        private final String description;

        FrozenStatus(Integer value, String description) {
            this.value = value;
            this.description = description;
        }

        public Integer getValue() {
            return value;
        }

        public String getDescription() {
            return description;
        }

        public static FrozenStatus fromValue(Integer value) {
            for (FrozenStatus status : values()) {
                if (status.value.equals(value)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Unknown frozen status: " + value);
        }
    }

    /**
     * 检查模板是否过期
     */
    public boolean isExpired() {
        if (expireDate == null) {
            return false;
        }
        return LocalDate.now().isAfter(expireDate);
    }

    /**
     * 检查模板是否被冻结
     */
    public boolean isFrozen() {
        return FrozenStatus.FROZEN.getValue().equals(frozenStatus);
    }

    /**
     * 检查模板是否可用
     */
    public boolean isAvailable() {
        return TemplateStatus.ENABLED.getValue().equals(templateStatus)
                && !isExpired()
                && !isFrozen();
    }

    /**
     * 获取模板剩余有效天数
     */
    public Integer getRemainingDays() {
        if (expireDate == null) {
            return null;
        }
        LocalDate today = LocalDate.now();
        if (today.isAfter(expireDate)) {
            return 0;
        }
        return (int) java.time.temporal.ChronoUnit.DAYS.between(today, expireDate);
    }

    /**
     * 增加使用次数
     */
    public void incrementUsageCount() {
        this.usageCount = (this.usageCount == null ? 0 : this.usageCount) + 1;
        this.lastUsedTime = new java.sql.Timestamp(System.currentTimeMillis());
    }

    /**
     * 增加异常使用次数
     */
    public void incrementAbnormalUsageCount() {
        this.abnormalUsageCount = (this.abnormalUsageCount == null ? 0 : this.abnormalUsageCount) + 1;

        // 如果异常次数过多，自动冻结模板
        if (this.abnormalUsageCount >= 5) {
            this.frozenStatus = FrozenStatus.FROZEN.getValue();
            this.freezeReason = "异常使用次数过多，自动冻结";
            this.freezeTime = new java.sql.Timestamp(System.currentTimeMillis());
        }
    }

    /**
     * 重置异常使用次数
     */
    public void resetAbnormalUsageCount() {
        this.abnormalUsageCount = 0;
        if (isFrozen()) {
            this.frozenStatus = FrozenStatus.NORMAL.getValue();
            this.freezeReason = null;
            this.freezeTime = null;
            this.unfreezeTime = new java.sql.Timestamp(System.currentTimeMillis());
        }
    }
}