package net.lab1024.sa.admin.module.consume.service;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 密码验证历史记录
 *
 * 严格遵循repowiki规范:
 * - 统一的实体类设计模式
 * - 完整的Swagger注解
 * - 标准化的Builder模式
 *
 * @author SmartAdmin Team
 * @since 2025-11-22
 */




@Schema(description = "密码验证历史记录")
public class PasswordVerificationHistory {

    @Schema(description = "记录ID")
    private Long recordId;

    @Schema(description = "人员ID", required = true)
    private Long personId;

    @Schema(description = "验证类型：PASSWORD-密码验证, BIOMETRIC-生物特征验证", required = true)
    private String verificationType;

    @Schema(description = "验证结果：SUCCESS-成功, FAILED-失败, LOCKED-已锁定", required = true)
    private String verificationResult;

    @Schema(description = "验证时间", required = true)
    private LocalDateTime verificationTime;

    @Schema(description = "设备ID")
    private String deviceId;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "客户端IP地址")
    private String clientIp;

    @Schema(description = "客户端地理位置")
    private String clientLocation;

    @Schema(description = "失败原因")
    private String failureReason;

    @Schema(description = "验证时长（毫秒）")
    private Long verificationDurationMs;

    @Schema(description = "是否为异常验证", required = true)
    private boolean isAbnormal;

    @Schema(description = "异常类型：MULTIPLE_FAILURES-多次失败, SUSPICIOUS_IP-可疑IP, UNUSUAL_TIME-异常时间")
    private String abnormalType;

    @Schema(description = "风险等级：LOW-低, MEDIUM-中, HIGH-高")
    private String riskLevel;

    @Schema(description = "生物特征类型（当验证类型为BIOMETRIC时）")
    private String biometricType;

    @Schema(description = "操作来源：WEB-网页, MOBILE-移动端, DEVICE-设备端")
    private String operationSource;

    @Schema(description = "用户代理信息")
    private String userAgent;

    @Schema(description = "会话ID")
    private String sessionId;

    // ========== 静态工厂方法 ==========

    /**
     * 创建成功的密码验证记录
     */
    public static PasswordVerificationHistory createSuccess(Long personId, String clientIp, String deviceId) {
        return PasswordVerificationHistory.builder()
                .personId(personId)
                .verificationType("PASSWORD")
                .verificationResult("SUCCESS")
                .verificationTime(LocalDateTime.now())
                .clientIp(clientIp)
                .deviceId(deviceId)
                .isAbnormal(false)
                .riskLevel("LOW")
                .operationSource("DEVICE")
                .build();
    }

    /**
     * 创建失败的密码验证记录
     */
    public static PasswordVerificationHistory createFailure(Long personId, String clientIp, String deviceId, String failureReason) {
        return PasswordVerificationHistory.builder()
                .personId(personId)
                .verificationType("PASSWORD")
                .verificationResult("FAILED")
                .verificationTime(LocalDateTime.now())
                .clientIp(clientIp)
                .deviceId(deviceId)
                .failureReason(failureReason)
                .isAbnormal(false)
                .riskLevel("MEDIUM")
                .operationSource("DEVICE")
                .build();
    }

    /**
     * 创建异常验证记录
     */
    public static PasswordVerificationHistory createAbnormal(Long personId, String clientIp, String deviceId, String abnormalType) {
        return PasswordVerificationHistory.builder()
                .personId(personId)
                .verificationType("PASSWORD")
                .verificationResult("FAILED")
                .verificationTime(LocalDateTime.now())
                .clientIp(clientIp)
                .deviceId(deviceId)
                .isAbnormal(true)
                .abnormalType(abnormalType)
                .riskLevel("HIGH")
                .operationSource("DEVICE")
                .build();
    }
}