package net.lab1024.sa.common.audit.model;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 配置变更通知
 * <p>
 * 用于封装配置变更的通知信息，支持多渠道通知：
 * - 邮件通知（EMAIL）
 * - 短信通知（SMS）
 * - Webhook通知（WEBHOOK）
 * - 应用内通知（IN_APP）
 * - 企业微信/钉钉通知（ENTERPRISE_IM）
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigChangeNotification {

    /**
     * 审计ID
     */
    private Long auditId;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知消息内容
     */
    private String message;

    /**
     * 通知详细内容（HTML格式）
     */
    private String detailContent;

    /**
     * 通知优先级
     * LOW-低, MEDIUM-中, HIGH-高, URGENT-紧急
     */
    private String priority;

    /**
     * 通知渠道列表
     */
    private List<String> channels;

    /**
     * 通知接收人列表（邮箱地址、手机号等）
     */
    private List<String> recipients;

    /**
     * 抄送接收人列表
     */
    private List<String> ccRecipients;

    /**
     * 通知类型
     * INFO-信息, WARNING-警告, ERROR-错误, SUCCESS-成功, APPROVAL-审批, RISK-风险
     */
    private String notificationType;

    /**
     * 通知数据（JSON格式，用于模板渲染）
     */
    private Map<String, Object> data;

    /**
     * 通知模板ID
     */
    private String templateId;

    /**
     * 通知模板参数
     */
    private Map<String, Object> templateParams;

    /**
     * 是否需要确认
     */
    private Boolean requireConfirmation;

    /**
     * 确认超时时间（小时）
     */
    private Integer confirmationTimeoutHours;

    /**
     * 通知创建时间
     */
    private LocalDateTime createTime;

    /**
     * 计划发送时间
     */
    private LocalDateTime scheduledTime;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 最大重试次数
     */
    private Integer maxRetryCount;

    /**
     * 发送状态
     * PENDING-待发送, SENDING-发送中, SUCCESS-成功, FAILED-失败, CANCELLED-已取消
     */
    private String sendStatus;

    /**
     * 发送时间
     */
    private LocalDateTime sendTime;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 通知追踪ID
     */
    private String trackingId;

    /**
     * 相关链接（查看详情、审批等）
     */
    private String relatedUrl;

    /**
     * 附件列表
     */
    private List<NotificationAttachment> attachments;

    /**
     * 通知标签
     */
    private List<String> tags;

    /**
     * 扩展属性
     */
    private Map<String, Object> extendedAttributes;

    // ==================== 通知附件内部类 ====================

    /**
     * 通知附件
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationAttachment {

        /**
         * 附件名称
         */
        private String name;

        /**
         * 附件URL
         */
        private String url;

        /**
         * 附件大小（字节）
         */
        private Long size;

        /**
         * 附件类型
         */
        private String contentType;

        /**
         * 附件描述
         */
        private String description;
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 创建高风险变更通知
     */
    public static ConfigChangeNotification createHighRiskNotification(Long auditId, String configName,
                                                                     String operatorName, String riskLevel) {
        return ConfigChangeNotification.builder()
                .auditId(auditId)
                .title("高风险配置变更告警")
                .message(String.format("检测到高风险配置变更：%s，操作人：%s，风险等级：%s",
                        configName, operatorName, riskLevel))
                .priority("HIGH")
                .notificationType("RISK")
                .channels(List.of("EMAIL", "WEBHOOK", "IN_APP"))
                .requireConfirmation(true)
                .confirmationTimeoutHours(24)
                .createTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建变更成功通知
     */
    public static ConfigChangeNotification createSuccessNotification(Long auditId, String configName,
                                                                  String operatorName) {
        return ConfigChangeNotification.builder()
                .auditId(auditId)
                .title("配置变更成功")
                .message(String.format("配置变更成功：%s，操作人：%s", configName, operatorName))
                .priority("MEDIUM")
                .notificationType("SUCCESS")
                .channels(List.of("IN_APP"))
                .createTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建变更失败通知
     */
    public static ConfigChangeNotification createFailureNotification(Long auditId, String configName,
                                                                  String operatorName, String errorMessage) {
        return ConfigChangeNotification.builder()
                .auditId(auditId)
                .title("配置变更失败")
                .message(String.format("配置变更失败：%s，操作人：%s，错误：%s", configName, operatorName, errorMessage))
                .priority("HIGH")
                .notificationType("ERROR")
                .channels(List.of("EMAIL", "IN_APP"))
                .createTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建审批请求通知
     */
    public static ConfigChangeNotification createApprovalNotification(Long auditId, String configName,
                                                                    String operatorName, String changeReason) {
        return ConfigChangeNotification.builder()
                .auditId(auditId)
                .title("配置变更审批请求")
                .message(String.format("有待审批的配置变更：%s，申请人：%s，原因：%s",
                        configName, operatorName, changeReason))
                .priority("HIGH")
                .notificationType("APPROVAL")
                .channels(List.of("EMAIL", "WEBHOOK", "IN_APP"))
                .requireConfirmation(true)
                .confirmationTimeoutHours(48)
                .createTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建批量操作通知
     */
    public static ConfigChangeNotification createBatchNotification(String batchId, String operationType,
                                                                 int totalCount, int successCount, int failureCount) {
        return ConfigChangeNotification.builder()
                .title("批量配置变更结果")
                .message(String.format("批量%s完成：总数：%d，成功：%d，失败：%d", operationType, totalCount, successCount, failureCount))
                .priority("MEDIUM")
                .notificationType(successCount == totalCount ? "SUCCESS" : "WARNING")
                .channels(List.of("EMAIL", "IN_APP"))
                .createTime(LocalDateTime.now())
                .build();
    }

    // ==================== 业务方法 ====================

    /**
     * 判断是否为高优先级通知
     */
    public boolean isHighPriority() {
        return "HIGH".equals(priority) || "URGENT".equals(priority);
    }

    /**
     * 判断是否需要确认
     */
    public boolean needsConfirmation() {
        return Boolean.TRUE.equals(requireConfirmation);
    }

    /**
     * 判断是否为紧急通知
     */
    public boolean isUrgent() {
        return "URGENT".equals(priority);
    }

    /**
     * 判断是否发送成功
     */
    public boolean isSendSuccessful() {
        return "SUCCESS".equals(sendStatus);
    }

    /**
     * 判断是否发送失败
     */
    public boolean isSendFailed() {
        return "FAILED".equals(sendStatus);
    }

    /**
     * 判断是否可以重试
     */
    public boolean canRetry() {
        return isSendFailed() && (retryCount == null || retryCount < (maxRetryCount != null ? maxRetryCount : 3));
    }

    /**
     * 获取优先级显示名称
     */
    public String getPriorityDisplayName() {
        return switch (priority) {
            case "LOW" -> "低";
            case "MEDIUM" -> "中";
            case "HIGH" -> "高";
            case "URGENT" -> "紧急";
            default -> "未知";
        };
    }

    /**
     * 获取通知类型显示名称
     */
    public String getNotificationTypeDisplayName() {
        return switch (notificationType) {
            case "INFO" -> "信息";
            case "WARNING" -> "警告";
            case "ERROR" -> "错误";
            case "SUCCESS" -> "成功";
            case "APPROVAL" -> "审批";
            case "RISK" -> "风险";
            default -> notificationType;
        };
    }

    /**
     * 获取发送状态显示名称
     */
    public String getSendStatusDisplayName() {
        return switch (sendStatus) {
            case "PENDING" -> "待发送";
            case "SENDING" -> "发送中";
            case "SUCCESS" -> "发送成功";
            case "FAILED" -> "发送失败";
            case "CANCELLED" -> "已取消";
            default -> "未知状态";
        };
    }

    /**
     * 增加重试次数
     */
    public void incrementRetryCount() {
        this.retryCount = (this.retryCount == null ? 0 : this.retryCount) + 1;
    }

    /**
     * 标记为发送成功
     */
    public void markAsSent() {
        this.sendStatus = "SUCCESS";
        this.sendTime = LocalDateTime.now();
        this.errorMessage = null;
    }

    /**
     * 标记为发送失败
     */
    public void markAsFailed(String errorMessage) {
        this.sendStatus = "FAILED";
        this.errorMessage = errorMessage;
        incrementRetryCount();
    }

    /**
     * 标记为发送中
     */
    public void markAsSending() {
        this.sendStatus = "SENDING";
    }

    /**
     * 取消通知
     */
    public void cancel(String reason) {
        this.sendStatus = "CANCELLED";
        this.errorMessage = reason;
    }

    /**
     * 设置通知数据
     */
    public void setNotificationData(String key, Object value) {
        if (data == null) {
            data = new java.util.HashMap<>();
        }
        data.put(key, value);
    }

    /**
     * 获取通知数据
     */
    @SuppressWarnings("unchecked")
    public <T> T getNotificationData(String key, Class<T> type) {
        if (data == null || !data.containsKey(key)) {
            return null;
        }
        Object value = data.get(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    /**
     * 添加通知渠道
     */
    public void addChannel(String channel) {
        if (channels == null) {
            channels = new java.util.ArrayList<>();
        }
        if (!channels.contains(channel)) {
            channels.add(channel);
        }
    }

    /**
     * 移除通知渠道
     */
    public void removeChannel(String channel) {
        if (channels != null) {
            channels.remove(channel);
        }
    }

    /**
     * 添加通知接收人
     */
    public void addRecipient(String recipient) {
        if (recipients == null) {
            recipients = new java.util.ArrayList<>();
        }
        if (!recipients.contains(recipient)) {
            recipients.add(recipient);
        }
    }

    /**
     * 添加抄送接收人
     */
    public void addCcRecipient(String recipient) {
        if (ccRecipients == null) {
            ccRecipients = new java.util.ArrayList<>();
        }
        if (!ccRecipients.contains(recipient)) {
            ccRecipients.add(recipient);
        }
    }

    /**
     * 添加附件
     */
    public void addAttachment(String name, String url, String contentType) {
        if (attachments == null) {
            attachments = new java.util.ArrayList<>();
        }
        attachments.add(NotificationAttachment.builder()
                .name(name)
                .url(url)
                .contentType(contentType)
                .build());
    }

    /**
     * 添加通知标签
     */
    public void addTag(String tag) {
        if (tags == null) {
            tags = new java.util.ArrayList<>();
        }
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
    }

    /**
     * 生成通知追踪ID
     */
    public String generateTrackingId() {
        if (trackingId == null) {
            this.trackingId = "NOTIFY_" + System.currentTimeMillis() + "_" + (auditId != null ? auditId : "UNKNOWN");
        }
        return trackingId;
    }

    /**
     * 验证通知数据完整性
     */
    public boolean validateNotification() {
        if (title == null || title.trim().isEmpty()) {
            return false;
        }
        if (message == null || message.trim().isEmpty()) {
            return false;
        }
        if (channels == null || channels.isEmpty()) {
            return false;
        }
        if (recipients == null || recipients.isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * 获取邮件模板数据
     */
    public Map<String, Object> getEmailTemplateData() {
        Map<String, Object> templateData = new java.util.HashMap<>();
        templateData.put("title", title);
        templateData.put("message", message);
        templateData.put("priority", priority);
        templateData.put("createTime", createTime);
        templateData.put("trackingId", generateTrackingId());

        if (data != null) {
            templateData.putAll(data);
        }

        return templateData;
    }

    /**
     * 获取短信模板数据
     */
    public Map<String, Object> getSmsTemplateData() {
        Map<String, Object> templateData = new java.util.HashMap<>();
        templateData.put("message", message);
        templateData.put("priority", priority);

        if (data != null) {
            templateData.putAll(data);
        }

        return templateData;
    }

    /**
     * 获取Webhook数据
     */
    public Map<String, Object> getWebhookData() {
        Map<String, Object> webhookData = new java.util.HashMap<>();
        webhookData.put("auditId", auditId);
        webhookData.put("title", title);
        webhookData.put("message", message);
        webhookData.put("priority", priority);
        webhookData.put("notificationType", notificationType);
        webhookData.put("createTime", createTime);
        webhookData.put("trackingId", generateTrackingId());

        if (data != null) {
            webhookData.putAll(data);
        }

        return webhookData;
    }
}