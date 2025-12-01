/*
 * 微信通知请求
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
import java.util.Map;

/**
 * 微信通知请求
 * 封装微信消息发送的请求数据
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WechatNotificationRequest {

    /**
     * 用户OpenID
     */
    @NotBlank(message = "OpenID不能为空")
    private String openId;

    /**
     * 消息类型（text/news/template/miniprogram）
     */
    @NotBlank(message = "消息类型不能为空")
    private String messageType;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 模板ID（模板消息时使用）
     */
    private String templateId;

    /**
     * 模板数据（模板消息时使用）
     */
    private Map<String, Object> templateData;

    /**
     * 跳转链接
     */
    private String url;

    /**
     * 小程序页面路径（小程序消息时使用）
     */
    private String miniProgramPage;

    /**
     * 标题（图文消息时使用）
     */
    private String title;

    /**
     * 描述（图文消息时使用）
     */
    private String description;

    /**
     * 图片链接（图文消息时使用）
     */
    private String picUrl;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 请求ID（用于追踪）
     */
    private String requestId;

    /**
     * 发送时间（定时发送，null表示立即发送）
     */
    private java.time.LocalDateTime sendTime;

    /**
     * 客服账号（客服消息时使用）
     */
    private String kfAccount;

    /**
     * 自定义参数（透传参数）
     */
    private String customParams;

    /**
     * 验证微信通知请求参数
     */
    public boolean isValid() {
        // 检查OpenID格式
        if (openId == null || !openId.matches("^[a-zA-Z0-9_-]{28}$")) {
            return false;
        }

        // 检查消息类型
        if (messageType == null) {
            return false;
        }

        // 根据消息类型检查必需参数
        switch (messageType.toLowerCase()) {
            case "text":
                return content != null && !content.trim().isEmpty();
            case "news":
                return title != null && !title.trim().isEmpty() && url != null && !url.trim().isEmpty();
            case "template":
                return templateId != null && !templateId.trim().isEmpty() && templateData != null;
            case "miniprogram":
                return templateId != null && !templateId.trim().isEmpty() && templateData != null;
            default:
                return false;
        }
    }

    /**
     * 检查是否为模板消息
     */
    public boolean isTemplateMessage() {
        return "template".equalsIgnoreCase(messageType);
    }

    /**
     * 检查是否为小程序消息
     */
    public boolean isMiniProgramMessage() {
        return "miniprogram".equalsIgnoreCase(messageType);
    }

    /**
     * 检查是否为图文消息
     */
    public boolean isNewsMessage() {
        return "news".equalsIgnoreCase(messageType);
    }

    /**
     * 检查是否为文本消息
     */
    public boolean isTextMessage() {
        return "text".equalsIgnoreCase(messageType);
    }

    /**
     * 检查是否为定时发送
     */
    public boolean isScheduledSend() {
        return sendTime != null && sendTime.isAfter(java.time.LocalDateTime.now());
    }

    /**
     * 检查内容长度限制
     */
    public boolean isContentWithinLimit() {
        if (isTextMessage() && content != null) {
            // 文本消息限制2048字节
            return content.getBytes().length <= 2048;
        }
        return true;
    }

    /**
     * 创建简单文本消息请求
     */
    public static WechatNotificationRequest createTextMessage(String openId, String content) {
        return WechatNotificationRequest.builder()
                .openId(openId)
                .messageType("text")
                .content(content)
                .build();
    }

    /**
     * 创建模板消息请求
     */
    public static WechatNotificationRequest createTemplateMessage(String openId, String templateId,
                                                                  Map<String, Object> templateData, String url) {
        return WechatNotificationRequest.builder()
                .openId(openId)
                .messageType("template")
                .templateId(templateId)
                .templateData(templateData != null ? templateData : new java.util.HashMap<>())
                .url(url)
                .build();
    }

    /**
     * 创建小程序消息请求
     */
    public static WechatNotificationRequest createMiniProgramMessage(String openId, String templateId,
                                                                    String page, Map<String, Object> templateData) {
        return WechatNotificationRequest.builder()
                .openId(openId)
                .messageType("miniprogram")
                .templateId(templateId)
                .miniProgramPage(page)
                .templateData(templateData != null ? templateData : new java.util.HashMap<>())
                .build();
    }

    /**
     * 创建图文消息请求
     */
    public static WechatNotificationRequest createNewsMessage(String openId, String title,
                                                            String description, String url, String picUrl) {
        return WechatNotificationRequest.builder()
                .openId(openId)
                .messageType("news")
                .title(title)
                .description(description)
                .url(url)
                .picUrl(picUrl)
                .build();
    }

    /**
     * 创建安全通知模板消息
     */
    public static WechatNotificationRequest createSecurityTemplateMessage(String openId, String securityType,
                                                                         String message, String actionUrl) {
        Map<String, Object> templateData = new java.util.HashMap<>();
        templateData.put("first", Map.of("value", "安全通知"));
        templateData.put("keyword1", Map.of("value", securityType));
        templateData.put("keyword2", Map.of("value", message));
        templateData.put("remark", Map.of("value", "请及时处理相关安全事项"));

        return WechatNotificationRequest.builder()
                .openId(openId)
                .messageType("template")
                .templateId("security_template_id") // 需要配置实际的模板ID
                .templateData(templateData)
                .url(actionUrl)
                .businessType("SECURITY_NOTIFICATION")
                .build();
    }

    /**
     * 获取消息类型描述
     */
    public String getMessageTypeDescription() {
        switch (messageType.toLowerCase()) {
            case "text":
                return "文本消息";
            case "news":
                return "图文消息";
            case "template":
                return "模板消息";
            case "miniprogram":
                return "小程序消息";
            case "image":
                return "图片消息";
            case "voice":
                return "语音消息";
            case "video":
                return "视频消息";
            default:
                return messageType;
        }
    }

    /**
     * 获取请求摘要
     */
    public String getRequestSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("微信通知请求摘要:\n");
        summary.append(String.format("OpenID: %s\n", openId.substring(0, 8) + "..."));
        summary.append(String.format("消息类型: %s\n", getMessageTypeDescription()));

        switch (messageType.toLowerCase()) {
            case "text":
                summary.append(String.format("内容: %s\n",
                        content.length() > 50 ? content.substring(0, 50) + "..." : content));
                break;
            case "template":
                summary.append(String.format("模板ID: %s\n", templateId));
                if (templateData != null) {
                    summary.append(String.format("模板参数: %d项\n", templateData.size()));
                }
                break;
            case "news":
                summary.append(String.format("标题: %s\n", title));
                summary.append(String.format("链接: %s\n", url));
                break;
            case "miniprogram":
                summary.append(String.format("模板ID: %s\n", templateId));
                summary.append(String.format("小程序页面: %s\n", miniProgramPage));
                break;
        }

        if (url != null) {
            summary.append(String.format("跳转链接: %s\n", url));
        }

        if (businessType != null) {
            summary.append(String.format("业务类型: %s\n", businessType));
        }

        return summary.toString();
    }
}