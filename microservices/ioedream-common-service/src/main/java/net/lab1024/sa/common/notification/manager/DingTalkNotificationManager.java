package net.lab1024.sa.common.notification.manager;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.monitor.domain.entity.NotificationEntity;
import net.lab1024.sa.common.notification.constant.NotificationConfigKey;

/**
 * 钉钉通知管理器
 * <p>
 * 负责钉钉通知的发送和管理
 * 严格遵循CLAUDE.md规范:
 * - 使用@Component注解标识
 * - 使用@Resource依赖注入（禁止@Autowired）
 * - 完整的异常处理和日志记录
 * </p>
 * <p>
 * 企业级特性：
 * - 钉钉机器人Webhook集成
 * - 加签模式确保安全性
 * - 支持Markdown、ActionCard、FeedCard等多种消息格式
 * - 支持@用户、@所有人功能
 * - 限流保护（20条/分钟）
 * - 重试机制（最多3次，指数退避）
 * - 异步发送，不阻塞主业务流程
 * - 配置热更新支持
 * </p>
 * <p>
 * 竞品对比（钉钉官方特性）：
 * - ✅ 机器人Webhook + 加签安全
 * - ✅ Markdown、ActionCard、FeedCard消息格式
 * - ✅ @用户、@所有人功能
 * - ✅ 限流保护（20条/分钟）
 * - ✅ 消息模板管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-01-30
 */
@Slf4j
@Component
@SuppressWarnings("null")
public class DingTalkNotificationManager {

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private NotificationConfigManager notificationConfigManager;

    @Resource
    private NotificationRateLimiter notificationRateLimiter;

    @Resource
    private NotificationRetryManager notificationRetryManager;

    @Resource
    private NotificationMetricsCollector notificationMetricsCollector;


    /**
     * 发送钉钉通知
     * <p>
     * 基于钉钉机器人Webhook实现消息发送
     * 支持加签模式确保安全性
     * 包含限流保护、重试机制等企业级特性
     * </p>
     *
     * @param notification 通知实体
     * @return 发送是否成功
     */
    @Async("notificationExecutor")
    public boolean sendDingTalk(NotificationEntity notification) {
        log.info("[钉钉通知] 开始发送，通知ID：{}，接收人：{}，标题：{}",
                notification.getNotificationId(), notification.getReceiverIds(), notification.getTitle());

        long startTime = System.currentTimeMillis();
        String channel = "DINGTALK";

        try {
            // 1. 限流检查（使用统一限流管理器）
            if (!notificationRateLimiter.tryAcquire(channel)) {
                log.warn("[钉钉通知] 触发限流保护，通知ID：{}，当前窗口已发送{}条消息",
                        notification.getNotificationId(), notificationRateLimiter.getCurrentCount(channel));
                notificationMetricsCollector.recordRateLimit(channel);
                return false;
            }

            // 2. 从配置管理器获取钉钉配置
            String webhookUrl = notificationConfigManager.getConfigValue(
                    NotificationConfigKey.DingTalk.WEBHOOK_URL);
            String secret = notificationConfigManager.getConfigValue(
                    NotificationConfigKey.DingTalk.SECRET);

            if (webhookUrl == null || webhookUrl.isEmpty()) {
                log.error("[钉钉通知] Webhook URL未配置，通知ID：{}", notification.getNotificationId());
                return false;
            }

            // 3. 构建消息内容
            Map<String, Object> message = buildDingTalkMessage(notification);

            // 4. 如果启用了加签，需要生成签名
            if (secret != null && !secret.isEmpty()) {
                webhookUrl = addSignatureToUrl(webhookUrl, secret);
            }

            // 5. 发送HTTP POST请求（使用统一重试管理器）
            boolean success = sendWithRetry(webhookUrl, message, notification);

            long duration = System.currentTimeMillis() - startTime;

            if (success) {
                // 更新限流计数
                notificationRateLimiter.incrementCount(channel);
                log.info("[钉钉通知] 发送成功，通知ID：{}，耗时：{}ms", notification.getNotificationId(), duration);
                // 记录成功指标
                notificationMetricsCollector.recordSuccess(channel, duration);
            } else {
                log.warn("[钉钉通知] 发送失败，通知ID：{}，耗时：{}ms", notification.getNotificationId(), duration);
                // 记录失败指标
                notificationMetricsCollector.recordFailure(channel, duration, "send_failed");
            }

            return success;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[钉钉通知] 发送异常，通知ID：{}，耗时：{}ms", notification.getNotificationId(), duration, e);
            // 记录异常指标
            notificationMetricsCollector.recordFailure(channel, duration, e.getClass().getSimpleName());
            return false;
        }
    }

    /**
     * 带重试机制的发送
     * <p>
     * 使用统一重试管理器进行重试
     * 支持最多3次重试，使用指数退避策略
     * </p>
     *
     * @param webhookUrl    Webhook URL
     * @param message       消息内容
     * @param notification  通知实体
     * @return 是否发送成功
     */
    private boolean sendWithRetry(String webhookUrl, Map<String, Object> message,
                                  NotificationEntity notification) {
        // 参数验证
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            log.error("[钉钉通知] Webhook URL为空，通知ID：{}", notification.getNotificationId());
            return false;
        }
        
        try {
            // 使用统一重试管理器执行发送操作
            Boolean result = notificationRetryManager.executeWithRetry(
                    () -> {
                        // 构建请求
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        HttpEntity<Map<String, Object>> request = new HttpEntity<>(message, headers);

                        // 发送HTTP POST请求（HttpMethod.POST是常量，不会为null）
                        @SuppressWarnings("null")
                        HttpMethod postMethod = HttpMethod.POST;
                        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                                webhookUrl,
                                postMethod,
                                request,
                                new ParameterizedTypeReference<Map<String, Object>>() {}
                        );

                        Map<String, Object> responseBody = response.getBody();
                        if (responseBody != null) {
                            Object errcodeObj = responseBody.get("errcode");
                            Integer errcode = errcodeObj instanceof Integer ? (Integer) errcodeObj : null;
                            if (errcode != null && errcode == 0) {
                                return true; // 发送成功
                            } else {
                                Object errmsgObj = responseBody.get("errmsg");
                                String errmsg = errmsgObj instanceof String ? (String) errmsgObj : null;
                                log.warn("[钉钉通知] 发送失败，错误码：{}，错误信息：{}",
                                        errcode, errmsg);

                                // 某些错误码不应该重试（如参数错误、签名错误等）
                                if (errcode != null && (errcode == 310000 || errcode == 300001)) {
                                    log.error("[钉钉通知] 错误码不允许重试，通知ID：{}", notification.getNotificationId());
                                    throw new RuntimeException("错误码不允许重试: " + errcode);
                                }

                                throw new RuntimeException("发送失败: " + (errmsg != null ? errmsg : "未知错误"));
                            }
                        }

                        throw new RuntimeException("响应体为空");
                    },
                    "钉钉通知发送"
            );

            // 记录重试次数（如果有重试）
            if (result != null && result) {
                return true;
            }

            return false;

        } catch (Exception e) {
            log.error("[钉钉通知] 重试失败，通知ID：{}", notification.getNotificationId(), e);
            // 记录重试失败指标
            notificationMetricsCollector.recordRetry("DINGTALK", notificationRetryManager.getMaxRetries());
            return false;
        }
    }

    /**
     * 构建钉钉消息
     * <p>
     * 根据通知内容构建钉钉消息格式
     * 支持多种消息格式：
     * - Markdown：富文本格式（默认）
     * - ActionCard：交互式卡片
     * - FeedCard：多卡片消息
     * </p>
     * <p>
     * 消息格式判断逻辑：
     * - 如果通知内容包含"actionCard"关键字，构建ActionCard消息
     * - 如果通知内容包含"feedCard"关键字，构建FeedCard消息
     * - 否则构建Markdown消息
     * </p>
     *
     * @param notification 通知实体
     * @return 钉钉消息Map
     */
    private Map<String, Object> buildDingTalkMessage(NotificationEntity notification) {
        String content = notification.getContent();
        if (content == null) {
            content = "";
        }

        // 判断消息格式（从通知内容中解析消息格式类型）
        // 支持通过内容关键字判断：包含"actioncard"关键字使用actioncard格式，包含"feedcard"关键字使用feedcard格式
        String messageType = getMessageTypeFromNotification(notification);

        switch (messageType.toLowerCase()) {
            case "actioncard":
                return buildActionCardMessage(notification);
            case "feedcard":
                return buildFeedCardMessage(notification);
            case "markdown":
            default:
                return buildMarkdownMessage(notification);
        }
    }

    /**
     * 构建Markdown格式消息
     * <p>
     * 钉钉Markdown消息格式
     * </p>
     *
     * @param notification 通知实体
     * @return 钉钉消息Map
     */
    private Map<String, Object> buildMarkdownMessage(NotificationEntity notification) {
        Map<String, Object> message = new HashMap<>();
        message.put("msgtype", "markdown");

        // 构建Markdown格式消息
        Map<String, Object> markdown = new HashMap<>();
        markdown.put("title", notification.getTitle());

        // 构建Markdown文本内容
        StringBuilder textBuilder = new StringBuilder();
        textBuilder.append("## ").append(notification.getTitle()).append("\n\n");
        textBuilder.append(notification.getContent());

        // 支持@用户功能
        if (notification.getReceiverIds() != null && !notification.getReceiverIds().isEmpty()) {
            textBuilder.append("\n\n");
            String[] userIds = notification.getReceiverIds().split(",");
            for (String userId : userIds) {
                textBuilder.append("@").append(userId.trim()).append(" ");
            }
        }

        markdown.put("text", textBuilder.toString());
        message.put("markdown", markdown);

        // 支持@所有人（如果通知类型为系统告警）
        if (notification.getNotificationType() != null && notification.getNotificationType() == 1) {
            Map<String, Object> at = new HashMap<>();
            at.put("isAtAll", true);
            message.put("at", at);
        }

        return message;
    }

    /**
     * 构建ActionCard格式消息
     * <p>
     * 钉钉ActionCard交互式卡片消息格式
     * </p>
     *
     * @param notification 通知实体
     * @return 钉钉消息Map
     */
    private Map<String, Object> buildActionCardMessage(NotificationEntity notification) {
        Map<String, Object> message = new HashMap<>();
        message.put("msgtype", "actionCard");

        // 构建ActionCard消息
        Map<String, Object> actionCard = new HashMap<>();
        actionCard.put("title", notification.getTitle());
        actionCard.put("text", notification.getContent());

        // 默认单个按钮（可以通过通知扩展字段配置）
        Map<String, Object> singleBtn = new HashMap<>();
        singleBtn.put("title", "查看详情");
        singleBtn.put("actionURL", "https://ioedream.com/notification/" + notification.getNotificationId());
        actionCard.put("singleTitle", singleBtn.get("title"));
        actionCard.put("singleURL", singleBtn.get("actionURL"));

        message.put("actionCard", actionCard);

        // 支持@所有人
        if (notification.getNotificationType() != null && notification.getNotificationType() == 1) {
            Map<String, Object> at = new HashMap<>();
            at.put("isAtAll", true);
            message.put("at", at);
        }

        return message;
    }

    /**
     * 构建FeedCard格式消息
     * <p>
     * 钉钉FeedCard多卡片消息格式
     * </p>
     *
     * @param notification 通知实体
     * @return 钉钉消息Map
     */
    private Map<String, Object> buildFeedCardMessage(NotificationEntity notification) {
        Map<String, Object> message = new HashMap<>();
        message.put("msgtype", "feedCard");

        // 构建FeedCard消息
        Map<String, Object> feedCard = new HashMap<>();
        List<Map<String, Object>> links = new java.util.ArrayList<>();

        // 默认单个卡片（可以通过通知扩展字段配置多个卡片）
        Map<String, Object> link = new HashMap<>();
        link.put("title", notification.getTitle());
        link.put("messageURL", "https://ioedream.com/notification/" + notification.getNotificationId());
        link.put("picURL", "https://ioedream.com/images/notification-default.png");
        links.add(link);

        feedCard.put("links", links);
        message.put("feedCard", feedCard);

        return message;
    }

    /**
     * 为Webhook URL添加签名
     * <p>
     * 钉钉加签模式：使用HMAC-SHA256算法生成签名
     * </p>
     *
     * @param webhookUrl 原始Webhook URL
     * @param secret 钉钉机器人Secret
     * @return 带签名的Webhook URL
     */
    private String addSignatureToUrl(String webhookUrl, String secret) {
        try {
            Long timestamp = System.currentTimeMillis();
            String stringToSign = timestamp + "\n" + secret;
            
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec spec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(spec);
            byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            String sign = Base64.getEncoder().encodeToString(signData);

            // URL编码签名
            String encodedSign = java.net.URLEncoder.encode(sign, StandardCharsets.UTF_8);

            // 拼接URL参数
            String separator = webhookUrl.contains("?") ? "&" : "?";
            return webhookUrl + separator + "timestamp=" + timestamp + "&sign=" + encodedSign;

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("生成钉钉签名失败", e);
            return webhookUrl; // 签名失败时返回原始URL
        }
    }

    /**
     * 从通知内容中解析消息格式类型
     * <p>
     * 支持通过内容关键字判断消息格式：
     * - 包含"actioncard"关键字：使用actioncard格式（交互式卡片）
     * - 包含"feedcard"关键字：使用feedcard格式（多卡片消息）
     * - 其他：使用markdown格式（Markdown消息，默认）
     * </p>
     *
     * @param notification 通知实体
     * @return 消息格式类型（markdown/actioncard/feedcard）
     */
    private String getMessageTypeFromNotification(NotificationEntity notification) {
        if (notification == null || notification.getContent() == null) {
            return "markdown"; // 默认Markdown格式
        }

        String content = notification.getContent().toLowerCase();
        if (content.contains("feedcard") || content.contains("多卡片")) {
            return "feedcard";
        } else if (content.contains("actioncard") || content.contains("交互卡片")) {
            return "actioncard";
        } else {
            return "markdown"; // 默认Markdown格式
        }
    }

}
