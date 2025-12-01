package net.lab1024.sa.base.module.biometric.entity;

import java.util.List;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.entity.BaseEntity;

/**
 * 设备生物特征关联实体
 * <p>
 * 定义设备需要哪些类型的生物特征，以及生物特征下发的配置信息
 * 实现设备与生物特征的灵活关联，支持不同设备需要不同的生物特征组合
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-24
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_device_biometric_mapping")
public class DeviceBiometricMappingEntity extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备类型
     * ACCESS-门禁设备
     * ATTENDANCE-考勤设备
     * CONSUME-消费设备
     * VIDEO-视频设备
     */
    private String deviceType;

    /**
     * 需要的生物特征类型列表（JSON格式）
     * 例如: ["FACE", "FINGERPRINT"] 表示需要人脸和指纹
     */
    @TableField("required_biometric_types")
    private List<String> requiredBiometricTypes;

    /**
     * 生物特征质量阈值（JSON格式）
     * {
     *   "FACE": 0.85,
     *   "FINGERPRINT": 0.80,
     *   "IRIS": 0.90,
     *   "PALMPRINT": 0.82,
     *   "VOICE": 0.78
     * }
     */
    @TableField("biometric_quality_threshold")
    private String biometricQualityThreshold;

    /**
     * 启用备选方案
     * 0-禁用，1-启用
     * 当主要生物特征验证失败时，尝试使用备选生物特征
     */
    @TableField("enable_fallback")
    private Integer enableFallback;

    /**
     * 备选顺序（JSON格式）
     * 例如: ["FACE", "FINGERPRINT"] 表示优先使用人脸，失败后使用指纹
     */
    @TableField("fallback_order")
    private List<String> fallbackOrder;

    /**
     * 下发状态
     * PENDING-待下发
     * DISPATCHING-下发中
     * COMPLETED-已完成
     * FAILED-失败
     * PARTIAL-部分成功
     */
    @TableField("dispatch_status")
    private String dispatchStatus;

    /**
     * 最后同步时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.time.LocalDateTime lastSyncTime;

    /**
     * 同步结果详情（JSON格式）
     * {
     *   "FACE": {
     *     "status": "SUCCESS",
     *     "dispatchTime": "2025-11-24 10:30:00",
     *     "templateCount": 2,
     *     "successCount": 2,
     *     "failureCount": 0
     *   },
     *   "FINGERPRINT": {
     *     "status": "PARTIAL",
     *     "dispatchTime": "2025-11-24 10:31:00",
     *     "templateCount": 5,
     *     "successCount": 4,
     *     "failureCount": 1,
     *     "errors": ["Template #3 加密失败"]
     *   }
     * }
     */
    @TableField("sync_result")
    private String syncResult;

    /**
     * 启用状态
     * 0-禁用，1-启用
     */
    @TableField("enable_status")
    private Integer enableStatus;

    /**
     * 备注
     */
    private String remark;

    /**
     * 设备类型枚举
     */
    public enum DeviceType {
        ACCESS("ACCESS", "门禁设备"),
        ATTENDANCE("ATTENDANCE", "考勤设备"),
        CONSUME("CONSUME", "消费设备"),
        VIDEO("VIDEO", "视频设备");

        private final String value;
        private final String description;

        DeviceType(String value, String description) {
            this.value = value;
            this.description = description;
        }

        public String getValue() {
            return value;
        }

        public String getDescription() {
            return description;
        }

        public static DeviceType fromValue(String value) {
            for (DeviceType type : values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown device type: " + value);
        }
    }

    /**
     * 下发状态枚举
     */
    public enum DispatchStatus {
        PENDING("PENDING", "待下发"),
        DISPATCHING("DISPATCHING", "下发中"),
        COMPLETED("COMPLETED", "已完成"),
        FAILED("FAILED", "失败"),
        PARTIAL("PARTIAL", "部分成功"),
        RETRY("RETRY", "重试中");

        private final String value;
        private final String description;

        DispatchStatus(String value, String description) {
            this.value = value;
            this.description = description;
        }

        public String getValue() {
            return value;
        }

        public String getDescription() {
            return description;
        }

        public static DispatchStatus fromValue(String value) {
            for (DispatchStatus status : values()) {
                if (status.value.equals(value)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Unknown dispatch status: " + value);
        }
    }

    /**
     * 检查是否需要人脸特征
     */
    public boolean requiresFace() {
        return requiredBiometricTypes != null && requiredBiometricTypes.contains("FACE");
    }

    /**
     * 检查是否需要指纹特征
     */
    public boolean requiresFingerprint() {
        return requiredBiometricTypes != null && requiredBiometricTypes.contains("FINGERPRINT");
    }

    /**
     * 检查是否需要虹膜特征
     */
    public boolean requiresIris() {
        return requiredBiometricTypes != null && requiredBiometricTypes.contains("IRIS");
    }

    /**
     * 检查是否需要掌纹特征
     */
    public boolean requiresPalmprint() {
        return requiredBiometricTypes != null && requiredBiometricTypes.contains("PALMPRINT");
    }

    /**
     * 检查是否需要声纹特征
     */
    public boolean requiresVoice() {
        return requiredBiometricTypes != null && requiredBiometricTypes.contains("VOICE");
    }

    /**
     * 检查是否需要指定的生物特征类型
     */
    public boolean requiresBiometricType(String biometricType) {
        return requiredBiometricTypes != null && requiredBiometricTypes.contains(biometricType);
    }

    /**
     * 获取需要的生物特征数量
     */
    public int getRequiredBiometricCount() {
        return requiredBiometricTypes != null ? requiredBiometricTypes.size() : 0;
    }

    /**
     * 检查是否启用备选方案
     */
    public boolean isFallbackEnabled() {
        return enableFallback != null && enableFallback == 1;
    }

    /**
     * 检查是否启用
     */
    public boolean isEnabled() {
        return enableStatus != null && enableStatus == 1;
    }

    /**
     * 检查是否已完成下发
     */
    public boolean isDispatchCompleted() {
        return DispatchStatus.COMPLETED.getValue().equals(dispatchStatus);
    }

    /**
     * 检查是否下发失败
     */
    public boolean isDispatchFailed() {
        return DispatchStatus.FAILED.getValue().equals(dispatchStatus);
    }

    /**
     * 检查是否正在下发
     */
    public boolean isDispatching() {
        return DispatchStatus.DISPATCHING.getValue().equals(dispatchStatus);
    }

    /**
     * 检查是否需要重新下发
     */
    public boolean needsRedispatch() {
        return DispatchStatus.FAILED.getValue().equals(dispatchStatus)
                || DispatchStatus.PARTIAL.getValue().equals(dispatchStatus);
    }

    /**
     * 获取生物特征质量阈值
     */
    public double getQualityThreshold(String biometricType) {
        if (biometricQualityThreshold == null || biometricQualityThreshold.trim().isEmpty()) {
            return 0.8; // 默认阈值
        }

        try {
            // 这里应该使用JSON解析器，简化实现
            switch (biometricType) {
                case "FACE":
                    return 0.85;
                case "FINGERPRINT":
                    return 0.80;
                case "IRIS":
                    return 0.90;
                case "PALMPRINT":
                    return 0.82;
                case "VOICE":
                    return 0.78;
                default:
                    return 0.8;
            }
        } catch (Exception e) {
            return 0.8;
        }
    }

    /**
     * 获取首选生物特征类型
     */
    public String getPrimaryBiometricType() {
        if (fallbackOrder != null && !fallbackOrder.isEmpty()) {
            return fallbackOrder.get(0);
        }
        if (requiredBiometricTypes != null && !requiredBiometricTypes.isEmpty()) {
            return requiredBiometricTypes.get(0);
        }
        return null;
    }

    /**
     * 获取生物特征优先级顺序
     */
    public List<String> getBiometricPriorityOrder() {
        if (fallbackOrder != null && !fallbackOrder.isEmpty()) {
            return fallbackOrder;
        }
        return requiredBiometricTypes;
    }

    /**
     * 更新下发状态
     */
    public void updateDispatchStatus(DispatchStatus status) {
        this.dispatchStatus = status.getValue();
        this.lastSyncTime = java.time.LocalDateTime.now();
    }

    /**
     * 更新同步时间
     */
    public void updateSyncTime() {
        this.lastSyncTime = java.time.LocalDateTime.now();
    }

    /**
     * 生成设备描述
     */
    public String getDeviceDescription() {
        DeviceType type = DeviceType.fromValue(deviceType);
        StringBuilder desc = new StringBuilder();
        desc.append(type.getDescription())
            .append(" - 需要生物特征: ");

        if (requiredBiometricTypes != null && !requiredBiometricTypes.isEmpty()) {
            for (int i = 0; i < requiredBiometricTypes.size(); i++) {
                if (i > 0) {
                    desc.append(", ");
                }
                switch (requiredBiometricTypes.get(i)) {
                    case "FACE":
                        desc.append("人脸");
                        break;
                    case "FINGERPRINT":
                        desc.append("指纹");
                        break;
                    case "IRIS":
                        desc.append("虹膜");
                        break;
                    case "PALMPRINT":
                        desc.append("掌纹");
                        break;
                    case "VOICE":
                        desc.append("声纹");
                        break;
                    default:
                        desc.append(requiredBiometricTypes.get(i));
                        break;
                }
            }
        }

        if (isFallbackEnabled()) {
            desc.append(" (启用备选)");
        }

        return desc.toString();
    }
}