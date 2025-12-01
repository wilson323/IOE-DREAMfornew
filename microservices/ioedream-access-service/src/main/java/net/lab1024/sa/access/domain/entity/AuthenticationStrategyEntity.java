package net.lab1024.sa.access.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import net.lab1024.sa.common.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * 认证策略配置实体类
 *
 * 定义不同安全级别的生物识别认证策略，包括所需的生物识别类型、
 * 阈值设置、备用方案等配置参数
 *
 * @author SmartAdmin Team
 * @date 2025-01-15
 */

public class AuthenticationStrategyEntity extends BaseEntity {

    /**
     * 策略ID
     */
    private Long strategyId;

    /**
     * 策略名称
     * 策略的唯一标识名称
     */
    private String strategyName;

    /**
     * 策略显示名称
     * 用户友好的显示名称
     */
    private String strategyDisplayName;

    /**
     * 策略描述
     * 详细说明策略的用途和适用场景
     */
    private String strategyDescription;

    /**
     * 安全级别
     * LOW-低, MEDIUM-中, HIGH-高, CRITICAL-关键
     */
    private String securityLevel;

    /**
     * 策略配置（JSON格式）
     * {
     *   "requiredBiometricTypes": ["FACE", "FINGERPRINT"],
     *   "confidenceThreshold": 0.95,
     *   "livenessDetectionRequired": true,
     *   "maxRetryCount": 3,
     *   "timeoutSeconds": 30,
     *   "fallbackTypes": ["PALMPRINT"],
     *   "fusionStrategy": "WEIGHTED_AVERAGE",
     *   "riskAssessmentEnabled": true,
     *   "livenessConfig": {
     *     "actions": ["BLINK", "HEAD_TURN"],
     *     "difficultyLevel": "HIGH",
     *     "randomActions": true
     *   },
     *   "biometricThresholds": {
     *     "FACE": 0.85,
     *     "FINGERPRINT": 0.90,
     *     "PALMPRINT": 0.88,
     *     "IRIS": 0.95
     *   },
     *   "securityControls": {
     *     "requireLocation": false,
     *     "requireDeviceBinding": true,
     *     "maxConcurrentSessions": 3,
     *   "sessionTimeoutMinutes": 30
     *   }
     * }
     */
    private String strategyConfig;

    /**
     * 适用设备类型（逗号分隔）
     * DOOR-门禁, GATE-闸机, TURNSTILE-转闸
     */
    private String deviceTypes;

    /**
     * 适用区域类型（逗号分隔）
     * OFFICE-办公区, PRODUCTION-生产区, DATA_CENTER-数据中心, RESEARCH-研发区
     */
    private String areaTypes;

    /**
     * 适用用户类型（逗号分隔）
     * EMPLOYEE-员工, VISITOR-访客, CONTRACTOR-承包商, VIP-贵宾
     */
    private String userTypes;

    /**
     * 生效开始时间
     */
    private LocalTime effectiveStartTime;

    /**
     * 生效结束时间
     */
    private LocalTime effectiveEndTime;

    /**
     * 生效星期（逗号分隔，1-7对应周一到周日）
     */
    private String effectiveDays;

    /**
     * 是否启用
     * 0-禁用, 1-启用
     */
    private Integer isEnabled;

    /**
     * 策略优先级
     * 数值越小优先级越高，当多个策略匹配时使用最高优先级的策略
     */
    private Integer priority;

    /**
     * 是否为默认策略
     * 当没有匹配的具体策略时使用的默认策略
     */
    private Integer isDefault;

    /**
     * 是否支持备用认证
     * 当主要认证方式失败时是否允许使用备用方式
     */
    private Integer supportFallback;

    /**
     * 是否启用多因子认证
     * 要求使用多种生物识别方式进行认证
     */
    private Integer enableMultiFactor;

    /**
     * 最少需要的生物识别类型数量
     * 多因子认证时至少需要的类型数量
     */
    private Integer minRequiredTypes;

    /**
     * 最大允许的生物识别类型数量
     * 多因子认证时最多允许的类型数量
     */
    private Integer maxAllowedTypes;

    /**
     * 是否启用自适应阈值
     * 根据环境和设备情况动态调整识别阈值
     */
    private Integer enableAdaptiveThreshold;

    /**
     * 基础置信度阈值
     * 识别的基础置信度要求
     */
    private BigDecimal baseConfidenceThreshold;

    /**
     * 动态阈值调整范围
     * 自适应阈值的调整范围百分比
     */
    private BigDecimal thresholdAdjustmentRange;

    /**
     * 是否启用风险评估
     * 在认证过程中进行安全风险评估
     */
    private Integer enableRiskAssessment;

    /**
     * 风险评估阈值
     * 超过此阈值的风险评分需要额外验证
     */
    private BigDecimal riskAssessmentThreshold;

    /**
     * 是否启用地理位置验证
     * 验证用户的位置是否在允许范围内
     */
    private Integer enableLocationVerification;

    /**
     * 允许的地理位置（JSON格式）
     * {
     *   "type": "GEOFENCE",
     *   "coordinates": [
     *     {"latitude": 39.9042, "longitude": 116.4074, "radius": 100}
     *   ],
     *   "whitelist": ["OFFICE", "FACTORY"]
     * }
     */
    private String allowedLocations;

    /**
     * 是否启用设备绑定验证
     * 验证用户是否使用注册的设备进行认证
     */
    private Integer enableDeviceBinding;

    /**
     * 允许的设备类型（JSON格式）
     * {
     *   "mobile": true,
     *   "desktop": false,
     *   "kiosk": true,
     *   "security_level": "HIGH"
     * }
     */
    private String allowedDeviceTypes;

    /**
     * 会话超时时间（分钟）
     * 认证会话的最大有效时间
     */
    private Integer sessionTimeoutMinutes;

    /**
     * 最大并发会话数
     * 同一用户允许的最大并发认证会话数
     */
    private Integer maxConcurrentSessions;

    /**
     * 重试策略（JSON格式）
     * {
     *   "maxRetryCount": 3,
     *   "retryDelaySeconds": [5, 10, 30],
     *   "exponentialBackoff": true,
     *   "lockoutAfterMaxRetries": true,
     *   "lockoutDurationMinutes": 15
     * }
     */
    private String retryStrategy;

    /**
     * 是否启用行为分析
     * 分析用户的行为模式以提高安全性
     */
    private Integer enableBehaviorAnalysis;

    /**
     * 行为分析配置（JSON格式）
     * {
     *   "typingPattern": true,
     *   "mouseMovement": true,
     *   "deviceOrientation": true,
     *   "anomalyThreshold": 0.7
     * }
     */
    private String behaviorAnalysisConfig;

    /**
     * 是否启用实时监控
     * 实时监控认证过程并记录详细日志
     */
    private Integer enableRealTimeMonitoring;

    /**
     * 监控告警配置（JSON格式）
     * {
     *   "enableFailureAlerts": true,
     *   "failureThreshold": 5,
     *   "timeWindowMinutes": 10,
     *   "alertChannels": ["EMAIL", "SMS", "WEBHOOK"]
     * }
     */
    private String monitoringAlertConfig;

    /**
     * 策略版本号
     * 用于策略版本管理和升级
     */
    private String strategyVersion;

    /**
     * 上次更新时间
     */
    private java.sql.Timestamp lastUpdateTime;

    /**
     * 更新说明
     * 记录策略更新的原因和内容
     */
    private String updateRemark;

    /**
     * 是否为系统策略
     * 0-用户自定义, 1-系统预定义
     */
    private Integer isSystemStrategy;

    /**
     * 策略分类
     * ACCESS_CONTROL-访问控制, TIME_ATTENDANCE-考勤管理, PAYMENT-支付验证, IDENTITY-身份认证
     */
    private String strategyCategory;

    /**
     * 适用场景标签（逗号分隔）
     * INDOOR-室内, OUTDOOR-室外, HIGH_SECURITY-高安全, PUBLIC-公共场所
     */
    private String scenarioTags;

    /**
     * 性能要求（JSON格式）
     * {
     *   "maxResponseTimeMs": 1000,
     *   "minSuccessRate": 0.99,
     *   "maxFalsePositiveRate": 0.001,
     *   "maxFalseNegativeRate": 0.01
     * }
     */
    private String performanceRequirements;

    /**
     * 合规性要求（JSON格式）
     * {
     *   "gdpr": true,
     *   "ccpa": false,
     *   "iso27001": true,
     *   "localRegulations": ["CYBERSECURITY_LAW"]
     * }
     */
    private String complianceRequirements;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 扩展配置字段（JSON格式）
     * 用于存储未来扩展的配置参数
     */
    private String extendConfig;

    // 枚举定义
    public enum SecurityLevel {
        LOW("LOW", "低"),
        MEDIUM("MEDIUM", "中"),
        HIGH("HIGH", "高"),
        CRITICAL("CRITICAL", "关键");

        private final String value;
        private final String description;

        SecurityLevel(String value, String description) {
            this.value = value;
            this.description = description;
        }



        public static SecurityLevel fromValue(String value) {
            for (SecurityLevel level : values()) {
                if (level.value.equals(value)) {
                    return level;
                }
            }
            throw new IllegalArgumentException("Unknown security level: " + value);
        }
    }

    public enum StrategyCategory {
        ACCESS_CONTROL("ACCESS_CONTROL", "访问控制"),
        TIME_ATTENDANCE("TIME_ATTENDANCE", "考勤管理"),
        PAYMENT("PAYMENT", "支付验证"),
        IDENTITY("IDENTITY", "身份认证");

        private final String value;
        private final String description;

        StrategyCategory(String value, String description) {
            this.value = value;
            this.description = description;
        }



        public static StrategyCategory fromValue(String value) {
            for (StrategyCategory category : values()) {
                if (category.value.equals(value)) {
                    return category;
                }
            }
            throw new IllegalArgumentException("Unknown strategy category: " + value);
        }
    }

    /**
     * 检查策略是否在当前时间有效
     */
    public boolean isCurrentTimeValid() {
        LocalTime now = LocalTime.now();
        LocalTime start = effectiveStartTime != null ? effectiveStartTime : LocalTime.MIN;
        LocalTime end = effectiveEndTime != null ? effectiveEndTime : LocalTime.MAX;

        if (start.isBefore(end)) {
            return !now.isBefore(start) && !now.isAfter(end);
        } else {
            // 跨越午夜的情况
            return !now.isBefore(start) || !now.isAfter(end);
        }
    }

    /**
     * 检查策略是否在当前星期有效
     */
    public boolean isCurrentDayValid() {
        if (effectiveDays == null || effectiveDays.trim().isEmpty()) {
            return true; // 如果没有限制，默认都有效
        }

        int currentDay = java.time.LocalDateTime.now().getDayOfWeek().getValue();
        // 调整为1-7对应周一到周日
        if (currentDay == 7) currentDay = 0; // 将周日调整为0

        String[] days = effectiveDays.split(",");
        for (String day : days) {
            try {
                int dayValue = Integer.parseInt(day.trim());
                if (dayValue == currentDay) {
                    return true;
                }
            } catch (NumberFormatException e) {
                // 忽略无效的日期格式
            }
        }
        return false;
    }

    /**
     * 检查策略是否完全有效
     */
    public boolean isFullyValid() {
        return Integer.valueOf(1).equals(isEnabled)
                && isCurrentTimeValid()
                && isCurrentDayValid();
    }

    /**
     * 检查设备类型是否适用
     */
    public boolean isDeviceTypeApplicable(String deviceType) {
        if (deviceTypes == null || deviceTypes.trim().isEmpty()) {
            return true; // 如果没有限制，默认都适用
        }

        String[] types = deviceTypes.split(",");
        for (String type : types) {
            if (type.trim().equalsIgnoreCase(deviceType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查用户类型是否适用
     */
    public boolean isUserTypeApplicable(String userType) {
        if (userTypes == null || userTypes.trim().isEmpty()) {
            return true; // 如果没有限制，默认都适用
        }

        String[] types = userTypes.split(",");
        for (String type : types) {
            if (type.trim().equalsIgnoreCase(userType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取策略安全级别数值
     * 用于比较和排序
     */

    /**
     * 检查是否支持指定的生物识别类型
     */
    public boolean supportsBiometricType(String biometricType) {
        // 这里需要解析strategyConfig JSON字段中的requiredBiometricTypes
        // 简化实现，实际使用时需要JSON解析
        return true; // 简化返回，实际需要详细解析
    }

    /**
     * 检查是否需要活体检测
     */
    public boolean requiresLivenessDetection() {
        // 解析strategyConfig中的livenessDetectionRequired字段
        return true; // 简化返回，实际需要详细解析
    }

    /**
     * 获取最大重试次数
     */

    /**
     * 获取超时时间（秒）
     */
}
