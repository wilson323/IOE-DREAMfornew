/*
 * 短信发送结果
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
 * 短信发送结果
 * 封装短信发送的执行结果
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsResult {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 错误消息
     */
    private String errorMessage;

    /**
     * 发送时间
     */
    private LocalDateTime sendTime;

    /**
     * 预计送达时间
     */
    private LocalDateTime estimatedDeliveryTime;

    /**
     * 短信条数
     */
    private Integer smsCount;

    /**
     * 服务提供商
     */
    private String serviceProvider;

    /**
     * 运营商
     */
    private String carrier;

    /**
     * 发送耗时（毫秒）
     */
    private Long sendTimeMs;

    /**
     * 状态报告URL
     */
    private String statusReportUrl;

    /**
     * 额外信息
     */
    private Object extraInfo;

    /**
     * 创建成功结果
     */
    public static SmsResult success(String messageId) {
        return SmsResult.builder()
                .success(true)
                .messageId(messageId)
                .smsCount(1)
                .sendTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建成功结果（带完整信息）
     */
    public static SmsResult success(String messageId, String serviceProvider, String carrier, Integer smsCount) {
        return SmsResult.builder()
                .success(true)
                .messageId(messageId)
                .serviceProvider(serviceProvider)
                .carrier(carrier)
                .smsCount(smsCount != null ? smsCount : 1)
                .sendTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建失败结果
     */
    public static SmsResult failure(String errorCode, String errorMessage) {
        return SmsResult.builder()
                .success(false)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .sendTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建失败结果（带详细错误）
     */
    public static SmsResult failure(String errorCode, String errorMessage, String serviceProvider) {
        return SmsResult.builder()
                .success(false)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .serviceProvider(serviceProvider)
                .sendTime(LocalDateTime.now())
                .build();
    }

    /**
     * 获取结果描述
     */
    public String getResultDescription() {
        if (success) {
            return String.format("短信发送成功 - 消息ID: %s, 条数: %d",
                    messageId != null ? messageId : "未知",
                    smsCount != null ? smsCount : 1);
        } else {
            return String.format("短信发送失败 [%s] - %s",
                    errorCode != null ? errorCode : "UNKNOWN_ERROR",
                    errorMessage != null ? errorMessage : "未知错误");
        }
    }

    /**
     * 检查是否需要重试
     */
    public boolean shouldRetry() {
        if (success) {
            return false;
        }

        // 以下错误类型可以重试
        return "NETWORK_TIMEOUT".equals(errorCode) ||
               "SERVICE_UNAVAILABLE".equals(errorCode) ||
               "RATE_LIMIT_EXCEEDED".equals(errorCode) ||
               "TEMPORARY_FAILURE".equals(errorCode) ||
               "CARRIER_BUSY".equals(errorCode);
    }

    /**
     * 获取重试等待时间（秒）
     */
    public long getRetryWaitSeconds() {
        if (!shouldRetry()) {
            return 0;
        }

        // 根据错误类型确定重试时间
        switch (errorCode) {
            case "RATE_LIMIT_EXCEEDED":
                return 300; // 5分钟
            case "SERVICE_UNAVAILABLE":
                return 120; // 2分钟
            case "CARRIER_BUSY":
                return 60;  // 1分钟
            case "NETWORK_TIMEOUT":
                return 30;  // 30秒
            default:
                return 60;  // 默认1分钟
        }
    }

    /**
     * 获取运营商描述
     */
    public String getCarrierDescription() {
        if (carrier == null) {
            return "未知运营商";
        }

        switch (carrier.toUpperCase()) {
            case "CMCC":
                return "中国移动";
            case "CUCC":
                return "中国联通";
            case "CTCC":
                return "中国电信";
            default:
                return carrier;
        }
    }

    /**
     * 获取详细错误信息
     */
    public String getDetailedErrorInfo() {
        if (success) {
            return "短信发送成功";
        }

        StringBuilder errorInfo = new StringBuilder();
        errorInfo.append("短信发送失败:\n");
        errorInfo.append(String.format("错误代码: %s\n", errorCode != null ? errorCode : "未知"));
        errorInfo.append(String.format("错误消息: %s\n", errorMessage != null ? errorMessage : "未知"));

        if (serviceProvider != null) {
            errorInfo.append(String.format("服务提供商: %s\n", serviceProvider));
        }

        if (carrier != null) {
            errorInfo.append(String.format("运营商: %s\n", getCarrierDescription()));
        }

        if (shouldRetry()) {
            errorInfo.append(String.format("建议重试，等待时间: %d秒\n", getRetryWaitSeconds()));
        } else {
            errorInfo.append("不建议重试，请检查手机号码和配置\n");
        }

        return errorInfo.toString();
    }

    /**
     * 获取预计送达时间描述
     */
    public String getDeliveryTimeDescription() {
        if (estimatedDeliveryTime == null) {
            return "未知";
        }

        return java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .format(estimatedDeliveryTime);
    }

    /**
     * 检查是否为长短信
     */
    public boolean isLongSms() {
        return smsCount != null && smsCount > 1;
    }

    /**
     * 获取发送状态描述
     */
    public String getStatusDescription() {
        if (success) {
            return isLongSms() ? "长短信发送成功" : "短信发送成功";
        } else {
            return "短信发送失败";
        }
    }
}