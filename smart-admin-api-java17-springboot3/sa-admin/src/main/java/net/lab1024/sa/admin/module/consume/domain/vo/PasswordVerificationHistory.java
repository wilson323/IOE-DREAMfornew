/*
 * 密码验证历史记录
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-17
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.consume.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 密码验证历史记录
 * 记录支付密码验证行为的历史信息
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordVerificationHistory {

    /**
     * 验证时间
     */
    private LocalDateTime verifyTime;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 客户端IP地址
     */
    private String clientIp;

    /**
     * 验证结果（SUCCESS/FAILED）
     */
    private String result;

    /**
     * 结果消息
     */
    private String message;

    /**
     * 验证方式（PASSWORD/BIOMETRIC_FINGERPRINT/BIOMETRIC_FACE等）
     */
    private String verifyMethod;

    /**
     * 设备信息
     */
    private String deviceInfo;

    /**
     * 地理位置
     */
    private String location;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 风险等级（SAFE/LOW/MEDIUM/HIGH/CRITICAL）
     */
    private String riskLevel;

    /**
     * 是否为异常行为
     */
    private Boolean isAbnormal;

    /**
     * 失败原因（如果验证失败）
     */
    private String failureReason;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 交易订单号（如果有）
     */
    private String orderNo;

    /**
     * 验证耗时（毫秒）
     */
    private Long verifyDuration;

    /**
     * 创建成功的历史记录
     */
    public static PasswordVerificationHistory success(LocalDateTime verifyTime, String clientIp, String message) {
        return PasswordVerificationHistory.builder()
                .verifyTime(verifyTime)
                .clientIp(clientIp)
                .result("SUCCESS")
                .message(message)
                .riskLevel("SAFE")
                .isAbnormal(false)
                .build();
    }

    /**
     * 创建失败的历史记录
     */
    public static PasswordVerificationHistory failure(LocalDateTime verifyTime, String clientIp, String message, String failureReason) {
        return PasswordVerificationHistory.builder()
                .verifyTime(verifyTime)
                .clientIp(clientIp)
                .result("FAILED")
                .message(message)
                .failureReason(failureReason)
                .riskLevel("MEDIUM")
                .isAbnormal(false)
                .build();
    }

    /**
     * 是否为成功的验证
     */
    public boolean isSuccess() {
        return "SUCCESS".equals(result);
    }

    /**
     * 是否为失败的验证
     */
    public boolean isFailure() {
        return "FAILED".equals(result);
    }

    /**
     * 获取结果显示文本
     */
    public String getResultDisplay() {
        if (isSuccess()) {
            return "成功";
        } else if (isFailure()) {
            return "失败";
        } else {
            return result;
        }
    }

    /**
     * 获取风险等级描述
     */
    public String getRiskLevelDescription() {
        if (riskLevel == null) {
            return "未知";
        }
        switch (riskLevel) {
            case "SAFE":
                return "安全";
            case "LOW":
                return "低风险";
            case "MEDIUM":
                return "中风险";
            case "HIGH":
                return "高风险";
            case "CRITICAL":
                return "严重风险";
            default:
                return riskLevel;
        }
    }

    /**
     * 获取验证方式描述
     */
    public String getVerifyMethodDescription() {
        if (verifyMethod == null) {
            return "未知";
        }
        switch (verifyMethod) {
            case "PASSWORD":
                return "密码验证";
            case "BIOMETRIC_FINGERPRINT":
                return "指纹验证";
            case "BIOMETRIC_FACE":
                return "人脸验证";
            case "BIOMETRIC_IRIS":
                return "虹膜验证";
            default:
                if (verifyMethod.startsWith("BIOMETRIC_")) {
                    return "生物特征验证";
                }
                return verifyMethod;
        }
    }

    /**
     * 检查是否为最近发生的验证
     */
    public boolean isRecent() {
        return verifyTime != null && verifyTime.isAfter(LocalDateTime.now().minusMinutes(30));
    }

    /**
     * 检查是否为今天发生的验证
     */
    public boolean isToday() {
        return verifyTime != null && verifyTime.toLocalDate().equals(LocalDateTime.now().toLocalDate());
    }

    /**
     * 获取简化的显示信息
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(verifyTime != null ? verifyTime.toString() : "未知时间");
        summary.append(" | ");
        summary.append(getResultDisplay());
        summary.append(" | ");
        summary.append(clientIp != null ? clientIp : "未知IP");

        if (deviceInfo != null) {
            summary.append(" | ");
            summary.append(deviceInfo);
        }

        return summary.toString();
    }
}