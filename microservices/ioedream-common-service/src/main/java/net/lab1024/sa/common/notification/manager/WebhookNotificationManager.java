package net.lab1024.sa.common.notification.manager;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;


import org.springframework.data.redis.core.RedisTemplate;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.monitor.domain.entity.NotificationEntity;
import net.lab1024.sa.common.notification.constant.NotificationConfigKey;

/**
 * Webhook通知管理器
 * <p>
 * 负责通用Webhook通知的发送和管理
 * 严格遵循CLAUDE.md规范:
 * - 使用@Component注解标识
 * - 使用@Resource依赖注入（禁止@Autowired）
 * - 完整的异常处理和日志记录
 * </p>
 * <p>
 * 企业级特性：
 * - 通用HTTP Webhook支持
 * - 自定义请求头（支持Authorization、X-API-Key等）
 * - 支持多种HTTP方法（POST/PUT/PATCH）
 * - 签名验证（HMAC-SHA256）
 * - 重试机制（最多3次，指数退避）
 * - 超时控制
 * - 配置热更新支持
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-01-30
 */
@Slf4j
@Component
@SuppressWarnings("null")
public class WebhookNotificationManager {

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private NotificationConfigManager notificationConfigManager;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private NotificationRateLimiter notificationRateLimiter;

    @Resource
    private NotificationRetryManager notificationRetryManager;

    @Resource
    private NotificationMetricsCollector notificationMetricsCollector;

    /**
     * 发送Webhook通知
     * <p>
     * 发送HTTP请求到指定的Webhook URL
     * 支持自定义请求头、请求体、HTTP方法、签名验证等
     * </p>
     *
     * @param notification 通知实体
     * @return 发送是否成功
     */
    @Async("notificationExecutor")
    public boolean sendWebhook(NotificationEntity notification) {
        log.info("[Webhook通知] 开始发送，通知ID：{}，标题：{}",
                notification.getNotificationId(), notification.getTitle());

        long startTime = System.currentTimeMillis();
        String channel = "WEBHOOK";

        try {
            // 1. 限流检查（使用统一限流管理器）
            if (!notificationRateLimiter.tryAcquire(channel)) {
                log.warn("[Webhook通知] 超过限流阈值，通知ID：{}", notification.getNotificationId());
                notificationMetricsCollector.recordRateLimit(channel);
                return false;
            }
            // 1. 获取Webhook URL（优先从配置获取，其次从通知内容提取）
            String webhookUrl = getWebhookUrl(notification);
            if (webhookUrl == null || webhookUrl.isEmpty()) {
                log.warn("[Webhook通知] URL为空，通知ID：{}", notification.getNotificationId());
                return false;
            }

            // 2. 获取HTTP方法（默认POST）
            String httpMethod = notificationConfigManager.getConfigValue(
                    NotificationConfigKey.Webhook.METHOD, "POST");

            // 3. 构建请求体
            Map<String, Object> requestBody = buildWebhookRequestBody(notification);

            // 4. 构建请求头（支持自定义请求头）
            HttpHeaders headers = buildWebhookHeaders(notification);

            // 5. 如果启用了签名，添加签名到请求头
            String signatureKey = notificationConfigManager.getConfigValue(
                    NotificationConfigKey.Webhook.SIGNATURE_KEY);
            if (signatureKey != null && !signatureKey.isEmpty()) {
                String signature = generateSignature(requestBody, signatureKey);
                headers.set("X-Signature", signature);
            }

            // 6. 发送HTTP请求（使用统一重试管理器）
            boolean success = sendHttpRequest(webhookUrl, httpMethod, requestBody, headers, notification);

            long duration = System.currentTimeMillis() - startTime;

            if (success) {
                // 更新限流计数
                notificationRateLimiter.incrementCount(channel);
                log.info("[Webhook通知] 发送成功，通知ID：{}，URL：{}，耗时：{}ms",
                        notification.getNotificationId(), webhookUrl, duration);
                // 记录成功指标
                notificationMetricsCollector.recordSuccess(channel, duration);
            } else {
                log.warn("[Webhook通知] 发送失败，通知ID：{}，URL：{}，耗时：{}ms",
                        notification.getNotificationId(), webhookUrl, duration);
                // 记录失败指标
                notificationMetricsCollector.recordFailure(channel, duration, "send_failed");
            }

            return success;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[Webhook通知] 发送异常，通知ID：{}，耗时：{}ms", notification.getNotificationId(), duration, e);
            // 记录异常指标
            notificationMetricsCollector.recordFailure(channel, duration, e.getClass().getSimpleName());
            return false;
        }
    }

    /**
     * 获取Webhook URL
     * <p>
     * 优先从配置获取，其次从通知内容提取
     * </p>
     *
     * @param notification 通知实体
     * @return Webhook URL
     */
    private String getWebhookUrl(NotificationEntity notification) {
        // 1. 优先从配置获取
        String webhookUrl = notificationConfigManager.getConfigValue(NotificationConfigKey.Webhook.URL);
        if (webhookUrl != null && !webhookUrl.isEmpty()) {
            return webhookUrl;
        }

        // 2. 从通知内容提取（兼容旧方式）
        return extractWebhookUrl(notification);
    }

    /**
     * 提取Webhook URL
     * <p>
     * 从通知内容或配置中提取Webhook URL
     * 提取优先级：
     * 1. 从receiverIds字段提取（Webhook通知类型通常将URL存储在receiverIds中）
     * 2. 从通知内容的JSON格式中提取（如果内容是JSON且包含webhookUrl字段）
     * 3. 从通知内容的文本格式中提取（兼容旧格式 "webhook_url:https://..."）
     * </p>
     *
     * @param notification 通知实体
     * @return Webhook URL，如果无法提取则返回null
     */
    private String extractWebhookUrl(NotificationEntity notification) {
        // 方式1：优先从receiverIds字段提取（Webhook通知类型通常将URL存储在receiverIds中）
        String receiverIds = notification.getReceiverIds();
        if (receiverIds != null && !receiverIds.trim().isEmpty()) {
            String trimmedReceiverIds = receiverIds.trim();
            // 检查是否为有效的URL格式
            if (isValidUrl(trimmedReceiverIds)) {
                log.debug("从receiverIds字段提取Webhook URL，通知ID：{}，URL：{}", 
                        notification.getNotificationId(), trimmedReceiverIds);
                return trimmedReceiverIds;
            }
            // 如果receiverIds是逗号分隔的多个值，取第一个并验证
            if (trimmedReceiverIds.contains(",")) {
                String firstReceiver = trimmedReceiverIds.split(",")[0].trim();
                if (isValidUrl(firstReceiver)) {
                    log.debug("从receiverIds字段提取Webhook URL（取第一个），通知ID：{}，URL：{}", 
                            notification.getNotificationId(), firstReceiver);
                    return firstReceiver;
                }
            }
        }

        // 方式2：从通知内容的JSON格式中提取
        String content = notification.getContent();
        if (content != null && !content.trim().isEmpty()) {
            // 尝试解析为JSON格式
            try {
                Map<String, Object> contentMap = objectMapper.readValue(content, 
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                if (contentMap != null) {
                    // 尝试多种可能的字段名
                    String webhookUrl = extractUrlFromMap(contentMap, 
                            "webhookUrl", "webhook_url", "url", "targetUrl", "target_url");
                    if (webhookUrl != null) {
                        log.debug("从通知内容JSON中提取Webhook URL，通知ID：{}，URL：{}", 
                                notification.getNotificationId(), webhookUrl);
                        return webhookUrl;
                    }
                }
            } catch (Exception e) {
                // JSON解析失败，继续尝试文本格式提取
                log.debug("通知内容不是JSON格式，尝试文本格式提取，通知ID：{}", 
                        notification.getNotificationId());
            }

            // 方式3：从通知内容的文本格式中提取（兼容旧格式）
            if (content.contains("webhook_url:")) {
                String[] parts = content.split("webhook_url:");
                if (parts.length > 1) {
                    String urlCandidate = parts[1].trim().split("\\s")[0]; // 提取URL部分
                    if (isValidUrl(urlCandidate)) {
                        log.debug("从通知内容文本中提取Webhook URL，通知ID：{}，URL：{}", 
                                notification.getNotificationId(), urlCandidate);
                        return urlCandidate;
                    }
                }
            }
        }

        log.warn("无法从通知内容或receiverIds中提取Webhook URL，通知ID：{}", 
                notification.getNotificationId());
        return null;
    }

    /**
     * 从Map中提取URL字段
     * <p>
     * 尝试多种可能的字段名，返回第一个有效的URL
     * </p>
     *
     * @param contentMap 内容Map
     * @param fieldNames 可能的字段名列表
     * @return URL字符串，如果未找到则返回null
     */
    private String extractUrlFromMap(Map<String, Object> contentMap, String... fieldNames) {
        for (String fieldName : fieldNames) {
            Object value = contentMap.get(fieldName);
            if (value != null) {
                String urlCandidate = value.toString().trim();
                if (isValidUrl(urlCandidate)) {
                    return urlCandidate;
                }
            }
        }
        return null;
    }

    /**
     * 验证字符串是否为有效的URL
     * <p>
     * 使用Java标准库验证URL格式
     * </p>
     *
     * @param url URL字符串
     * @return 是否为有效URL
     */
    private boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        try {
            java.net.URL urlObj = new java.net.URL(url.trim());
            String protocol = urlObj.getProtocol();
            // 只支持 http、https、ftp 协议
            return ("http".equals(protocol) || "https".equals(protocol) || "ftp".equals(protocol))
                    && urlObj.getHost() != null && !urlObj.getHost().isEmpty();
        } catch (java.net.MalformedURLException e) {
            return false;
        }
    }

    /**
     * 构建Webhook请求体
     * <p>
     * 根据通知内容构建HTTP请求体
     * </p>
     *
     * @param notification 通知实体
     * @return 请求体Map
     */
    private Map<String, Object> buildWebhookRequestBody(NotificationEntity notification) {
        Map<String, Object> body = new HashMap<>();
        body.put("title", notification.getTitle());
        body.put("content", notification.getContent());
        body.put("notificationId", notification.getNotificationId());
        body.put("notificationType", notification.getNotificationType());
        body.put("timestamp", System.currentTimeMillis());

        return body;
    }

    /**
     * 构建Webhook请求头
     * <p>
     * 构建HTTP请求头，支持自定义头信息
     * 从配置中获取自定义请求头（如Authorization、X-API-Key等）
     * </p>
     *
     * @param notification 通知实体
     * @return HTTP请求头
     */
    private HttpHeaders buildWebhookHeaders(NotificationEntity notification) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("User-Agent", "IOE-DREAM-Notification-System/2.0");

        // 从配置获取自定义请求头（JSON格式）
        String headersConfig = notificationConfigManager.getConfigValue(NotificationConfigKey.Webhook.HEADERS);
        if (headersConfig != null && !headersConfig.isEmpty()) {
            try {
                Map<String, String> customHeaders = objectMapper.readValue(headersConfig,
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {});
                customHeaders.forEach(headers::set);
            } catch (Exception e) {
                log.warn("[Webhook通知] 自定义请求头解析失败，通知ID：{}", notification.getNotificationId(), e);
            }
        }

        return headers;
    }

    /**
     * 生成签名
     * <p>
     * 使用HMAC-SHA256算法生成请求签名
     * 签名算法：HMAC-SHA256(requestBody JSON字符串, signatureKey)
     * </p>
     *
     * @param requestBody   请求体
     * @param signatureKey 签名密钥
     * @return 签名（Base64编码）
     */
    private String generateSignature(Map<String, Object> requestBody, String signatureKey) {
        try {
            String requestBodyJson = objectMapper.writeValueAsString(requestBody);

            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec spec = new javax.crypto.spec.SecretKeySpec(
                    signatureKey.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(spec);
            byte[] signData = mac.doFinal(requestBodyJson.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            return java.util.Base64.getEncoder().encodeToString(signData);

        } catch (Exception e) {
            log.error("[Webhook通知] 生成签名失败", e);
            return null;
        }
    }

    /**
     * 发送HTTP请求（使用统一重试管理器）
     * <p>
     * 支持多种HTTP方法（POST/PUT/PATCH）
     * 使用统一重试管理器进行重试
     * </p>
     *
     * @param url           Webhook URL
     * @param httpMethod    HTTP方法
     * @param requestBody   请求体
     * @param headers       请求头
     * @param notification  通知实体
     * @return 是否发送成功
     */
    private boolean sendHttpRequest(String url, String httpMethod, Map<String, Object> requestBody,
                                    HttpHeaders headers, NotificationEntity notification) {
        // 参数验证
        if (url == null || url.isEmpty()) {
            log.error("[Webhook通知] URL为空，通知ID：{}", notification.getNotificationId());
            return false;
        }
        // 使用final变量，确保lambda表达式可以访问
        final String finalHttpMethod = (httpMethod == null || httpMethod.isEmpty()) ? "POST" : httpMethod;
        if (httpMethod == null || httpMethod.isEmpty()) {
            log.warn("[Webhook通知] HTTP方法为空，使用默认POST，通知ID：{}", notification.getNotificationId());
        }
        
        try {
            // 根据HTTP方法确定HttpMethod常量（在lambda外部确定，避免作用域冲突）
            // HttpMethod常量不会为null，添加SuppressWarnings抑制警告
            @SuppressWarnings("null")
            org.springframework.http.HttpMethod springHttpMethod;
            switch (finalHttpMethod.toUpperCase()) {
                case "POST":
                    springHttpMethod = org.springframework.http.HttpMethod.POST;
                    break;
                case "PUT":
                    springHttpMethod = org.springframework.http.HttpMethod.PUT;
                    break;
                case "PATCH":
                    // RestTemplate不支持PATCH，使用exchange方法
                    springHttpMethod = org.springframework.http.HttpMethod.PATCH;
                    break;
                default:
                    log.warn("[Webhook通知] 不支持的HTTP方法：{}，使用POST", finalHttpMethod);
                    springHttpMethod = org.springframework.http.HttpMethod.POST;
                    break;
            }
            
            // 使用统一重试管理器执行发送操作
            final org.springframework.http.HttpMethod finalHttpMethodValue = springHttpMethod;
            Boolean result = notificationRetryManager.executeWithRetry(
                    () -> {
                        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

                        // 根据HTTP方法发送请求
                        org.springframework.core.ParameterizedTypeReference<Map<String, Object>> responseType = 
                                new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {};
                        
                        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                                url, finalHttpMethodValue, request, responseType);

                        // 检查响应状态
                        if (response.getStatusCode().is2xxSuccessful()) {
                            return true;
                        } else {
                            throw new RuntimeException("HTTP状态码错误: " + response.getStatusCode());
                        }
                    },
                    "Webhook通知发送"
            );

            // 记录重试次数（如果有重试）
            if (result != null && result) {
                return true;
            }

            return false;

        } catch (Exception e) {
            log.error("[Webhook通知] 重试失败，通知ID：{}", notification.getNotificationId(), e);
            // 记录重试失败指标
            notificationMetricsCollector.recordRetry("WEBHOOK", notificationRetryManager.getMaxRetries());
            return false;
        }
    }
}
