package net.lab1024.sa.access.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;

import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 生物识别记录实体类
 *
 * 记录每次生物识别认证的详细信息，包括成功和失败的记录 用于统计分析、性能监控和安全审计
 *
 * @author SmartAdmin Team
 * @date 2025-01-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class BiometricRecordEntity extends BaseEntity {

    /**
     * 记录ID
     */
    private Long recordId;

    /**
     * 员工ID 可能为空，当识别失败时无法确定员工身份
     */
    private Long employeeId;

    /**
     * 员工姓名（冗余字段，便于查询） 识别成功时记录，失败时为空
     */
    private String employeeName;

    /**
     * 员工编号（冗余字段，便于查询） 识别成功时记录，失败时为空
     */
    private String employeeCode;

    /**
     * 设备ID 执行生物识别的设备ID
     */
    private Long deviceId;

    /**
     * 设备名称（冗余字段，便于查询）
     */
    private String deviceName;

    /**
     * 设备编号（冗余字段，便于查询）
     */
    private String deviceCode;

    /**
     * 生物识别类型 FACE-人脸, FINGERPRINT-指纹, PALMPRINT-掌纹, IRIS-虹膜
     */
    private String biometricType;

    /**
     * 验证结果 success-成功, failure-失败, timeout-超时, liveness_failed-活体检测失败, error-系统错误
     */
    private String verificationResult;

    /**
     * 置信度分数 0.0000-1.0000，表示识别的置信程度
     */
    private BigDecimal confidenceScore;

    /**
     * 处理时间（毫秒） 从接收生物识别请求到返回结果的总时间
     */
    private Integer processingTime;

    /**
     * 特征向量（加密存储） 本次识别提取的特征向量，用于分析和调试
     */
    private String featureVectors;

    /**
     * 验证元数据（JSON格式） { "algorithm": "FaceNet-v2", "algorithmVersion": "2.1.0",
     * "modelVersion":
     * "v20241201", "threshold": 0.85, "similarityScore": 0.92, "distance": 0.08,
     * "qualityScore":
     * 0.88 }
     */
    private String verificationMetadata;

    /**
     * 失败原因详细描述 当验证失败时，记录具体的失败原因
     */
    private String failureReason;

    /**
     * 记录时间 生物识别发生的精确时间
     */
    private LocalDateTime recordTime;

    /**
     * 会话ID 用于关联同一次认证过程中的多次识别尝试
     */
    private String sessionId;

    /**
     * 识别模式 ENROLL-注册, VERIFY-验证, SEARCH-搜索, UPDATE-更新
     */
    private String recognitionMode;

    /**
     * 环境光照条件 DARK-暗, NORMAL-正常, BRIGHT-亮
     */
    private String lightingCondition;

    /**
     * 环境温度（摄氏度） 影响识别准确率的环境因素
     */
    private BigDecimal ambientTemperature;

    /**
     * 环境湿度（百分比） 影响识别准确率的环境因素
     */
    private BigDecimal ambientHumidity;

    /**
     * 图像数据（存储路径） 识别过程中采集的原始图像或处理后的图像
     */
    private String imageDataPath;

    /**
     * 数据质量评分 0.0-1.0，评估输入数据的质量
     */
    private BigDecimal dataQualityScore;

    /**
     * 活体检测结果 PASS-通过, FAIL-失败, SKIPPED-跳过
     */
    private String livenessResult;

    /**
     * 活体检测详情（JSON格式） { "blink": true, "headTurn": true, "mouthMovement": false,
     * "livenessScore":
     * 0.95, "antiSpoofingScore": 0.98 }
     */
    private String livenessDetails;

    /**
     * 风险评估分数 0.0-1.0，评估本次识别的安全风险
     */
    private BigDecimal riskAssessmentScore;

    /**
     * 异常检测标记 NORMAL-正常, SUSPICIOUS-可疑, ATTACK-攻击, ERROR-错误
     */
    private String anomalyDetection;

    /**
     * 异常详情（JSON格式） { "type": "PHOTO_ATTACK", "confidence": 0.92, "description":
     * "检测到照片攻击",
     * "evidence": ["no_blinking", "static_texture"] }
     */
    private String anomalyDetails;

    /**
     * 网络延迟（毫秒） 客户端到服务器的网络传输时间
     */
    private Integer networkLatency;

    /**
     * 服务器处理时间（毫秒） 服务器端算法处理时间
     */
    private Integer serverProcessingTime;

    /**
     * 客户端IP地址 发起识别请求的客户端IP
     */
    private String clientIpAddress;

    /**
     * 用户代理（User-Agent） 客户端应用信息
     */
    private String userAgent;

    /**
     * 操作系统信息 客户端设备操作系统
     */
    private String operatingSystem;

    /**
     * 设备指纹 客户端设备的唯一标识
     */
    private String deviceFingerprint;

    /**
     * 地理位置信息（JSON格式） { "latitude": 39.9042, "longitude": 116.4074, "accuracy":
     * 10.0, "address":
     * "北京市朝阳区" }
     */
    private String locationInfo;

    /**
     * 处理器使用率（百分比） 识别过程中的系统资源使用情况
     */
    private BigDecimal cpuUsage;

    /**
     * 内存使用率（百分比） 识别过程中的系统资源使用情况
     */
    private BigDecimal memoryUsage;

    /**
     * 缓存命中标记 0-未命中, 1-命中
     */
    private Integer cacheHit;

    /**
     * 缓存命中时间（毫秒） 缓存命中时的响应时间
     */
    private Integer cacheResponseTime;

    /**
     * 重试次数 本次识别的重试次数
     */
    private Integer retryCount;

    /**
     * 最大重试次数 允许的最大重试次数
     */
    private Integer maxRetryCount;

    /**
     * 业务流水号 用于追踪业务流程的标识
     */
    private String businessFlowNo;

    /**
     * 业务类型 ACCESS-门禁, ATTENDANCE-考勤, PAYMENT-支付, AUTHENTICATION-身份认证
     */
    private String businessType;

    /**
     * 扩展字段1（JSON格式）
     */
    private String extendField1;

    /**
     * 扩展字段2（JSON格式）
     */
    private String extendField2;

    // 枚举定义
    public enum VerificationResult {
        SUCCESS("success", "成功"), FAILURE("failure", "失败"), TIMEOUT("timeout",
                "超时"),
        LIVENESS_FAILED("liveness_failed", "活体检测失败"), ERROR("error", "系统错误");

        private final String value;
        private final String description;

        VerificationResult(String value, String description) {
            this.value = value;
            this.description = description;
        }

        public String getValue() {
            return value;
        }

        public String getDescription() {
            return description;
        }

        public static VerificationResult fromValue(String value) {
            for (VerificationResult result : values()) {
                if (result.value.equals(value)) {
                    return result;
                }
            }
            throw new IllegalArgumentException("Unknown verification result: " + value);
        }
    }

    public enum RecognitionMode {
        ENROLL("ENROLL", "注册"), VERIFY("VERIFY", "验证"), SEARCH("SEARCH", "搜索"), UPDATE("UPDATE",
                "更新");

        private final String value;
        private final String description;

        RecognitionMode(String value, String description) {
            this.value = value;
            this.description = description;
        }

        public String getValue() {
            return value;
        }

        public String getDescription() {
            return description;
        }

        public static RecognitionMode fromValue(String value) {
            for (RecognitionMode mode : values()) {
                if (mode.value.equals(value)) {
                    return mode;
                }
            }
            throw new IllegalArgumentException("Unknown recognition mode: " + value);
        }
    }

    public enum LightingCondition {
        DARK("DARK", "暗"), NORMAL("NORMAL", "正常"), BRIGHT("BRIGHT", "亮");

        private final String value;
        private final String description;

        LightingCondition(String value, String description) {
            this.value = value;
            this.description = description;
        }

        public String getValue() {
            return value;
        }

        public String getDescription() {
            return description;
        }

        public static LightingCondition fromValue(String value) {
            for (LightingCondition condition : values()) {
                if (condition.value.equals(value)) {
                    return condition;
                }
            }
            throw new IllegalArgumentException("Unknown lighting condition: " + value);
        }
    }

    public enum LivenessResult {
        PASS("PASS", "通过"), FAIL("FAIL", "失败"), SKIPPED("SKIPPED", "跳过");

        private final String value;
        private final String description;

        LivenessResult(String value, String description) {
            this.value = value;
            this.description = description;
        }

        public String getValue() {
            return value;
        }

        public String getDescription() {
            return description;
        }

        public static LivenessResult fromValue(String value) {
            for (LivenessResult result : values()) {
                if (result.value.equals(value)) {
                    return result;
                }
            }
            throw new IllegalArgumentException("Unknown liveness result: " + value);
        }
    }

    public enum AnomalyDetection {
        NORMAL("NORMAL", "正常"), SUSPICIOUS("SUSPICIOUS", "可疑"), ATTACK("ATTACK",
                "攻击"),
        ERROR("ERROR", "错误");

        private final String value;
        private final String description;

        AnomalyDetection(String value, String description) {
            this.value = value;
            this.description = description;
        }

        public String getValue() {
            return value;
        }

        public String getDescription() {
            return description;
        }

        public static AnomalyDetection fromValue(String value) {
            for (AnomalyDetection detection : values()) {
                if (detection.value.equals(value)) {
                    return detection;
                }
            }
            throw new IllegalArgumentException("Unknown anomaly detection: " + value);
        }
    }

    /**
     * 检查是否为成功记录
     */
    public boolean isSuccess() {
        return VerificationResult.SUCCESS.getValue().equals(verificationResult);
    }

    /**
     * 检查是否为失败记录
     */
    public boolean isFailure() {
        return VerificationResult.FAILURE.getValue().equals(verificationResult)
                || VerificationResult.LIVENESS_FAILED.getValue().equals(verificationResult);
    }

    /**
     * 检查是否超时
     */
    public boolean isTimeout() {
        return VerificationResult.TIMEOUT.getValue().equals(verificationResult);
    }

    /**
     * 检查是否为系统错误
     */
    public boolean isError() {
        return VerificationResult.ERROR.getValue().equals(verificationResult);
    }

    /**
     * 检查是否有异常
     */
    public boolean hasAnomaly() {
        return !AnomalyDetection.NORMAL.getValue().equals(anomalyDetection);
    }

    /**
     * 检查是否为高风险记录
     */
    public boolean isHighRisk() {
        return hasAnomaly()
                || (riskAssessmentScore != null
                        && riskAssessmentScore.compareTo(new BigDecimal("0.8")) >= 0)
                || (confidenceScore != null
                        && confidenceScore.compareTo(new BigDecimal("0.5")) < 0);
    }

    /**
     * 获取处理性能等级 FAST-快速(<500ms), NORMAL-正常(500-1000ms), SLOW-缓慢(>1000ms)
     */

    /**
     * 计算算法效率分数 基于处理时间和置信度计算综合效率
     */
    public BigDecimal calculateEfficiencyScore() {
        if (processingTime == null || confidenceScore == null) {
            return BigDecimal.ZERO;
        }

        // 置信度权重70%，处理时间权重30%
        BigDecimal timeScore = new BigDecimal("1000.0").max(new BigDecimal(processingTime))
                .divide(new BigDecimal("1000.0"), 4, java.math.RoundingMode.HALF_UP);

        BigDecimal confidenceWeighted = confidenceScore.multiply(new BigDecimal("0.7"));
        BigDecimal timeWeighted = timeScore.multiply(new BigDecimal("0.3"));

        return confidenceWeighted.add(timeWeighted);
    }
}
