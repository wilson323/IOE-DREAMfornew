/*
 * 短信请求
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

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.Map;

/**
 * 短信请求
 * 封装短信发送的请求数据
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsRequest {

    /**
     * 手机号码
     */
    @NotBlank(message = "手机号码不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号码格式不正确")
    private String phoneNumber;

    /**
     * 短信内容
     */
    @NotBlank(message = "短信内容不能为空")
    private String content;

    /**
     * 模板代码（使用模板时）
     */
    private String templateCode;

    /**
     * 模板参数（使用模板时）
     */
    private Map<String, Object> templateParams;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 签名（可选，默认使用系统配置）
     */
    private String signature;

    /**
     * 是否为国际短信
     */
    private boolean international;

    /**
     * 国家代码（国际短信时使用）
     */
    private String countryCode;

    /**
     * 优先级
     */
    private SmsPriority priority;

    /**
     * 请求ID（用于追踪）
     */
    private String requestId;

    /**
     * 发送时间（定时发送，null表示立即发送）
     */
    private java.time.LocalDateTime sendTime;

    /**
     * 有效期（分钟）
     */
    private Integer expireMinutes;

    /**
     * 是否需要状态报告
     */
    private boolean requireStatusReport;

    /**
     * 回调URL（状态报告回调）
     */
    private String callbackUrl;

    /**
     * 自定义参数（透传参数）
     */
    private String customParams;

    /**
     * 验证短信请求参数
     */
    public boolean isValid() {
        // 检查手机号码格式
        if (phoneNumber == null || !phoneNumber.matches("^1[3-9]\\d{9}$")) {
            return false;
        }

        // 检查短信内容
        if (content == null || content.trim().isEmpty()) {
            return false;
        }

        // 如果使用模板，检查模板参数
        if (isTemplateSms() && templateParams == null) {
            templateParams = new java.util.HashMap<>();
        }

        // 检查内容长度（普通短信限制70个字符，超长短信限制500个字符）
        if (content.length() > 500) {
            return false;
        }

        return true;
    }

    /**
     * 检查是否使用模板
     */
    public boolean isTemplateSms() {
        return templateCode != null && !templateCode.trim().isEmpty();
    }

    /**
     * 检查是否为定时发送
     */
    public boolean isScheduledSend() {
        return sendTime != null && sendTime.isAfter(java.time.LocalDateTime.now());
    }

    /**
     * 检查是否为长短信
     */
    public boolean isLongSms() {
        return content != null && content.length() > 70;
    }

    /**
     * 获取短信条数（长短信按67字符/条计算）
     */
    public int getSMSSliceCount() {
        if (content == null) {
            return 1;
        }

        int length = content.length();
        if (length <= 70) {
            return 1;
        } else {
            // 长短信按67字符计算
            return (int) Math.ceil((double) length / 67);
        }
    }

    /**
     * 获取完整短信内容（含签名）
     */
    public String getFullContent() {
        StringBuilder fullContent = new StringBuilder();

        if (signature != null && !signature.trim().isEmpty()) {
            fullContent.append("【").append(signature).append("】");
        }

        fullContent.append(content != null ? content : "");

        return fullContent.toString();
    }

    /**
     * 创建简单短信请求
     */
    public static SmsRequest createSimple(String phoneNumber, String content) {
        return SmsRequest.builder()
                .phoneNumber(phoneNumber)
                .content(content)
                .priority(SmsPriority.NORMAL)
                .requireStatusReport(true)
                .build();
    }

    /**
     * 创建安全通知短信请求
     */
    public static SmsRequest createSecurityNotification(String phoneNumber, String content) {
        return SmsRequest.builder()
                .phoneNumber(phoneNumber)
                .content(content)
                .businessType("SECURITY_NOTIFICATION")
                .priority(SmsPriority.HIGH)
                .requireStatusReport(true)
                .expireMinutes(10) // 安全通知10分钟内必须送达
                .build();
    }

    /**
     * 创建验证码短信请求
     */
    public static SmsRequest createVerificationCode(String phoneNumber, String code, Integer expireMinutes) {
        String content = String.format("您的验证码是：%s，请在%d分钟内使用。如非本人操作请忽略。",
                code, expireMinutes != null ? expireMinutes : 5);

        return SmsRequest.builder()
                .phoneNumber(phoneNumber)
                .content(content)
                .businessType("VERIFICATION_CODE")
                .priority(SmsPriority.HIGH)
                .requireStatusReport(true)
                .expireMinutes(expireMinutes != null ? expireMinutes : 5)
                .build();
    }

    /**
     * 创建模板短信请求
     */
    public static SmsRequest createTemplate(String phoneNumber, String templateCode,
                                           Map<String, Object> templateParams, String businessType) {
        return SmsRequest.builder()
                .phoneNumber(phoneNumber)
                .templateCode(templateCode)
                .templateParams(templateParams != null ? templateParams : new java.util.HashMap<>())
                .businessType(businessType)
                .priority(SmsPriority.NORMAL)
                .requireStatusReport(true)
                .build();
    }
}

/**
 * 短信优先级枚举
 */
enum SmsPriority {
    LOW(1),      // 低优先级
    NORMAL(3),   // 普通优先级
    HIGH(5),     // 高优先级
    URGENT(9);   // 紧急

    private final int value;

    SmsPriority(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}