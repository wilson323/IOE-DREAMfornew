package net.lab1024.sa.monitor.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.monitor.domain.entity.NotificationEntity;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Webhook通知管理器
 *
 * 负责发送Webhook通知
 *
 * @author IOE-DREAM Team
 */
@Slf4j
@Component
public class WebhookNotificationManager {

    @Resource
    private WebhookConfigManager webhookConfigManager;

    private final HttpClient httpClient;

    public WebhookNotificationManager() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    /**
     * 发送Webhook通知
     *
     * @param notification 通知信息
     * @return 发送结果
     */
    public boolean sendWebhook(NotificationEntity notification) {
        log.debug("发送Webhook通知，URL：{}，标题：{}", notification.getRecipient(), notification.getNotificationTitle());

        try {
            String webhookUrl = notification.getRecipient();
            String payload = buildWebhookPayload(notification);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "IOE-DREAM-Monitor/1.0")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .timeout(Duration.ofSeconds(60))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            boolean success = response.statusCode() >= 200 && response.statusCode() < 300;

            if (success) {
                log.info("Webhook发送成功，URL：{}，状态码：{}", webhookUrl, response.statusCode());
            } else {
                log.warn("Webhook发送失败，URL：{}，状态码：{}，响应：{}", webhookUrl, response.statusCode(), response.body());
            }

            return success;

        } catch (IOException e) {
            log.error("Webhook发送IO异常，URL：{}", notification.getRecipient(), e);
            return false;
        } catch (InterruptedException e) {
            log.error("Webhook发送中断异常，URL：{}", notification.getRecipient(), e);
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.error("Webhook发送异常，URL：{}", notification.getRecipient(), e);
            return false;
        }
    }

    /**
     * 发送Slack Webhook通知
     *
     * @param notification 通知信息
     * @return 发送结果
     */
    public boolean sendSlackWebhook(NotificationEntity notification) {
        log.debug("发送Slack Webhook通知");

        try {
            String webhookUrl = notification.getRecipient();
            String slackPayload = buildSlackPayload(notification);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(slackPayload))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            boolean success = response.statusCode() == 200;

            if (success) {
                log.info("Slack Webhook发送成功");
            } else {
                log.warn("Slack Webhook发送失败，状态码：{}，响应：{}", response.statusCode(), response.body());
            }

            return success;

        } catch (Exception e) {
            log.error("Slack Webhook发送失败", e);
            return false;
        }
    }

    /**
     * 发送钉钉Webhook通知
     *
     * @param notification 通知信息
     * @return 发送结果
     */
    public boolean sendDingTalkWebhook(NotificationEntity notification) {
        log.debug("发送钉钉Webhook通知");

        try {
            String webhookUrl = notification.getRecipient();
            String dingTalkPayload = buildDingTalkPayload(notification);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(dingTalkPayload))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            boolean success = response.statusCode() == 200;

            if (success) {
                log.info("钉钉Webhook发送成功");
            } else {
                log.warn("钉钉Webhook发送失败，状态码：{}，响应：{}", response.statusCode(), response.body());
            }

            return success;

        } catch (Exception e) {
            log.error("钉钉Webhook发送失败", e);
            return false;
        }
    }

    /**
     * 发送Teams Webhook通知
     *
     * @param notification 通知信息
     * @return 发送结果
     */
    public boolean sendTeamsWebhook(NotificationEntity notification) {
        log.debug("发送Teams Webhook通知");

        try {
            String webhookUrl = notification.getRecipient();
            String teamsPayload = buildTeamsPayload(notification);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(teamsPayload))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            boolean success = response.statusCode() == 200;

            if (success) {
                log.info("Teams Webhook发送成功");
            } else {
                log.warn("Teams Webhook发送失败，状态码：{}，响应：{}", response.statusCode(), response.body());
            }

            return success;

        } catch (Exception e) {
            log.error("Teams Webhook发送失败", e);
            return false;
        }
    }

    /**
     * 测试Webhook配置
     *
     * @param webhookUrl Webhook URL
     * @return 测试结果
     */
    public boolean testWebhookConfig(String webhookUrl) {
        log.debug("测试Webhook配置，URL：{}", webhookUrl);

        try {
            Map<String, Object> testPayload = new HashMap<>();
            testPayload.put("type", "test");
            testPayload.put("title", "IOE-DREAM监控服务测试");
            testPayload.put("message", "这是一条测试消息，用于验证Webhook配置是否正确");
            testPayload.put("timestamp", LocalDateTime.now().toString());

            String jsonPayload = mapToJson(testPayload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            boolean success = response.statusCode() >= 200 && response.statusCode() < 300;

            if (success) {
                log.info("Webhook配置测试成功");
            } else {
                log.warn("Webhook配置测试失败，状态码：{}", response.statusCode());
            }

            return success;

        } catch (Exception e) {
            log.error("Webhook配置测试失败", e);
            return false;
        }
    }

    // 私有方法构建不同格式的payload

    private String buildWebhookPayload(NotificationEntity notification) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("alert_id", notification.getAlertId());
        payload.put("title", notification.getNotificationTitle());
        payload.put("content", notification.getNotificationContent());
        payload.put("priority", notification.getPriority());
        payload.put("timestamp", LocalDateTime.now().toString());
        payload.put("service", "ioedream-monitor");

        return mapToJson(payload);
    }

    private String buildSlackPayload(NotificationEntity notification) {
        Map<String, Object> payload = new HashMap<>();

        Map<String, Object> message = new HashMap<>();
        message.put("text", notification.getNotificationTitle());
        message.put("type", "mrkdwn");

        Map<String, Object> attachment = new HashMap<>();
        attachment.put("color", getColorByPriority(notification.getPriority()));
        attachment.put("text", notification.getNotificationContent());
        attachment.put("ts", System.currentTimeMillis() / 1000);

        payload.put("text", message.get("text"));
        payload.put("attachments", new Object[]{attachment});

        return mapToJson(payload);
    }

    private String buildDingTalkPayload(NotificationEntity notification) {
        Map<String, Object> payload = new HashMap<>();

        Map<String, Object> text = new HashMap<>();
        text.put("content", "## " + notification.getNotificationTitle() + "\n\n" + notification.getNotificationContent());

        payload.put("msgtype", "markdown");
        payload.put("markdown", text);

        return mapToJson(payload);
    }

    private String buildTeamsPayload(NotificationEntity notification) {
        Map<String, Object> payload = new HashMap<>();

        Map<String, Object> title = new HashMap<>();
        title.put("text", notification.getNotificationTitle());

        Map<String, Object> text = new HashMap<>();
        text.put("text", notification.getNotificationContent());

        Map<String, Object> themeColor = new HashMap<>();
        themeColor.put("themeColor", getColorHexByPriority(notification.getPriority()));

        payload.put("title", title);
        payload.put("text", text);
        payload.putAll(themeColor);

        return mapToJson(payload);
    }

    private String getColorByPriority(String priority) {
        switch (priority) {
            case "URGENT":
                return "danger";
            case "HIGH":
                return "warning";
            case "NORMAL":
                return "good";
            case "LOW":
                return "#36a64f";
            default:
                return "good";
        }
    }

    private String getColorHexByPriority(String priority) {
        switch (priority) {
            case "URGENT":
                return "FF0000";
            case "HIGH":
                return "FFA500";
            case "NORMAL":
                return "008000";
            case "LOW":
                return "36a64f";
            default:
                return "008000";
        }
    }

    private String mapToJson(Map<String, Object> map) {
        // 简单的JSON序列化，实际项目中应该使用Jackson或Gson
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                json.append(",");
            }
            json.append("\"").append(entry.getKey()).append("\":");
            if (entry.getValue() instanceof String) {
                json.append("\"").append(entry.getValue()).append("\"");
            } else {
                json.append(entry.getValue());
            }
            first = false;
        }
        json.append("}");
        return json.toString();
    }
}