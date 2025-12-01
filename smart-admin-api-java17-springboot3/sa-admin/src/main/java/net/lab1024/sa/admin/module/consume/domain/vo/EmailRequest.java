/*
 * 邮件请求
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-17
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.consume.domain.vo;

import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.lab1024.sa.admin.module.consume.domain.enums.EmailPriority;

/**
 * 邮件请求
 * 封装邮件发送的请求数据
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {

    /**
     * 收件人邮箱
     */
    @NotBlank(message = "收件人邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String to;

    /**
     * 收件人列表（群发时使用）
     */
    private List<String> toList;

    /**
     * 抄送邮箱列表
     */
    private List<String> ccList;

    /**
     * 密送邮箱列表
     */
    private List<String> bccList;

    /**
     * 邮件主题
     */
    @NotBlank(message = "邮件主题不能为空")
    private String subject;

    /**
     * 邮件内容
     */
    @NotBlank(message = "邮件内容不能为空")
    private String content;

    /**
     * 是否为HTML格式
     */
    private boolean isHtml;

    /**
     * 附件文件列表
     */
    private List<String> attachments;

    /**
     * 发件人邮箱（可选，默认使用系统配置）
     */
    private String from;

    /**
     * 发件人名称（可选）
     */
    private String fromName;

    /**
     * 回复邮箱（可选）
     */
    private String replyTo;

    /**
     * 邮件优先级
     */
    private EmailPriority priority;

    /**
     * 模板代码（使用模板时）
     */
    private String templateCode;

    /**
     * 模板参数（使用模板时）
     */
    private java.util.Map<String, Object> templateParams;

    /**
     * 业务类型（用于分类统计）
     */
    private String businessType;

    /**
     * 请求ID（用于追踪）
     */
    private String requestId;

    /**
     * 是否需要回执
     */
    private boolean requireReceipt;

    /**
     * 发送时间（定时发送，null表示立即发送）
     */
    private java.time.LocalDateTime sendTime;

    /**
     * 邮件标签（用于分类和过滤）
     */
    private List<String> tags;

    /**
     * 获取收件人列表（优先使用toList，其次使用to）
     */
    public List<String> getRecipientList() {
        if (toList != null && !toList.isEmpty()) {
            return toList;
        }
        return to != null ? java.util.Arrays.asList(to) : java.util.Collections.emptyList();
    }

    /**
     * 检查是否为群发邮件
     */
    public boolean isBulkEmail() {
        return getRecipientList().size() > 1;
    }

    /**
     * 检查是否有附件
     */
    public boolean hasAttachments() {
        return attachments != null && !attachments.isEmpty();
    }

    /**
     * 检查是否使用模板
     */
    public boolean isTemplateEmail() {
        return templateCode != null && !templateCode.trim().isEmpty();
    }

    /**
     * 检查是否为定时发送
     */
    public boolean isScheduledSend() {
        return sendTime != null && sendTime.isAfter(java.time.LocalDateTime.now());
    }

    /**
     * 获取邮件大小估算（字节）
     */
    public long getEstimatedSize() {
        long size = 0;

        // 主题和内容大小
        if (subject != null) {
            size += subject.getBytes().length;
        }
        if (content != null) {
            size += content.getBytes().length;
        }

        // 附件大小（粗略估算）
        if (hasAttachments()) {
            size += attachments.size() * 1024 * 100; // 假设每个附件100KB
        }

        return size;
    }

    /**
     * 验证邮件请求参数
     */
    public boolean isValid() {
        // 检查收件人
        if (getRecipientList().isEmpty()) {
            return false;
        }

        // 验证邮箱格式
        for (String recipient : getRecipientList()) {
            if (!recipient.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                return false;
            }
        }

        // 检查主题和内容
        if (subject == null || subject.trim().isEmpty()) {
            return false;
        }

        if (content == null || content.trim().isEmpty()) {
            return false;
        }

        // 如果使用模板，检查模板参数
        if (isTemplateEmail() && templateParams == null) {
            templateParams = new java.util.HashMap<>();
        }

        return true;
    }

    /**
     * 创建简单文本邮件请求
     */
    public static EmailRequest createSimpleText(String to, String subject, String content) {
        return EmailRequest.builder()
                .to(to)
                .subject(subject)
                .content(content)
                .isHtml(false)
                .priority(EmailPriority.NORMAL)
                .build();
    }

    /**
     * 创建HTML邮件请求
     */
    public static EmailRequest createHtmlEmail(String to, String subject, String htmlContent) {
        return EmailRequest.builder()
                .to(to)
                .subject(subject)
                .content(htmlContent)
                .isHtml(true)
                .priority(EmailPriority.NORMAL)
                .build();
    }

    /**
     * 创建安全通知邮件请求
     */
    public static EmailRequest createSecurityNotification(String to, String subject, String content) {
        return EmailRequest.builder()
                .to(to)
                .subject("【安全通知】" + subject)
                .content(content)
                .isHtml(true)
                .priority(EmailPriority.HIGH)
                .businessType("SECURITY_NOTIFICATION")
                .requireReceipt(true)
                .tags(java.util.Arrays.asList("security", "notification"))
                .build();
    }
}
