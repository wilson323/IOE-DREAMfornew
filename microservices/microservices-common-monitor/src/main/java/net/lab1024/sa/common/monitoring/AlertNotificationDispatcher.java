package net.lab1024.sa.common.monitoring;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 告警通知分发器
 * <p>
 * 负责将告警事件分发到各通知渠道（钉钉/企业微信/邮件/短信），从 AlertManager 中抽离以满足 SRP。
 * </p>
 */
@Slf4j
public class AlertNotificationDispatcher {

    private final AlertManager.AlertConfig alertConfig;
    private final GatewayServiceClient gatewayServiceClient;
    private final RestTemplate restTemplate;
    private final String dingTalkWebhookUrl;
    private final String weChatWebhookUrl;
    private final String smsApiUrl;
    private final String smsApiKey;
    private final String smsPhoneNumbers;
    private final String smsProvider;

    public AlertNotificationDispatcher(
            AlertManager.AlertConfig alertConfig,
            GatewayServiceClient gatewayServiceClient,
            RestTemplate restTemplate,
            String dingTalkWebhookUrl,
            String weChatWebhookUrl,
            String smsApiUrl,
            String smsApiKey,
            String smsPhoneNumbers,
            String smsProvider) {
        this.alertConfig = alertConfig;
        this.gatewayServiceClient = gatewayServiceClient;
        this.restTemplate = restTemplate;
        this.dingTalkWebhookUrl = dingTalkWebhookUrl;
        this.weChatWebhookUrl = weChatWebhookUrl;
        this.smsApiUrl = smsApiUrl;
        this.smsApiKey = smsApiKey;
        this.smsPhoneNumbers = smsPhoneNumbers;
        this.smsProvider = smsProvider != null && !smsProvider.isEmpty() ? smsProvider : "webhook";
    }

    public void sendAlertNotification(AlertManager.AlertRule rule, AlertManager.AlertRecord record, boolean isRepeated) {
        for (Map.Entry<String, AlertManager.NotificationChannel> entry : alertConfig.getChannels().entrySet()) {
            AlertManager.NotificationChannel channel = entry.getValue();
            if (!channel.isEnabled()) {
                continue;
            }
            try {
                sendNotification(channel, rule, record, isRepeated);
            } catch (Exception e) {
                log.error("[告警通知] 发送通知失败, channel={}", channel.getType(), e);
            }
        }
    }

    public void sendRecoveryNotification(AlertManager.AlertRule rule, double currentValue) {
        for (Map.Entry<String, AlertManager.NotificationChannel> entry : alertConfig.getChannels().entrySet()) {
            AlertManager.NotificationChannel channel = entry.getValue();
            if (channel.isEnabled() && "dingtalk".equals(channel.getType())) {
                try {
                    sendDingTalkRecoveryNotification(rule, currentValue);
                } catch (Exception e) {
                    log.error("[告警通知] 发送恢复通知失败, channel={}", channel.getType(), e);
                }
            }
        }
    }

    private void sendNotification(
            AlertManager.NotificationChannel channel,
            AlertManager.AlertRule rule,
            AlertManager.AlertRecord record,
            boolean isRepeated) {
        switch (channel.getType()) {
            case "dingtalk":
                sendDingTalkNotification(rule, record, isRepeated);
                break;
            case "wechat":
                sendWeChatNotification(rule, record, isRepeated);
                break;
            case "email":
                sendEmailNotification(rule, record, isRepeated);
                break;
            case "sms":
                sendSMSNotification(rule, record, isRepeated);
                break;
            default:
                break;
        }
    }

    private void sendDingTalkNotification(AlertManager.AlertRule rule, AlertManager.AlertRecord record, boolean isRepeated) {
        String title = isRepeated ? "【持续告警】" : "【新告警】";
        String content = String.format(
                "%s %s\n\n" +
                        "告警级别: %s %s\n" +
                        "当前数值: %.2f\n" +
                        "阈值设定: %.2f\n" +
                        "告警描述: %s\n" +
                        "触发时间: %s\n" +
                        "系统: IOE-DREAM智能管理平台",
                title, rule.getName(),
                record.getSeverity().getIcon(), record.getSeverity().getName(),
                record.getValue(), record.getThreshold(),
                rule.getDescription(), record.getTriggerTime()
        );

        try {
            if (dingTalkWebhookUrl != null && !dingTalkWebhookUrl.isEmpty()) {
                sendDingTalkNotificationDirectly(content);
                log.info("[告警通知] 钉钉通知已发送（直接调用）: rule={}, value={}", rule.getName(), record.getValue());
                return;
            }

            if (gatewayServiceClient != null) {
                Map<String, Object> request = new HashMap<>();
                request.put("notificationType", "dingtalk");
                request.put("msgtype", "text");
                Map<String, Object> message = new HashMap<>();
                message.put("content", content);
                request.put("message", message);

                gatewayServiceClient.callCommonService(
                        "/api/v1/notification/dingtalk/send",
                        HttpMethod.POST,
                        request,
                        Object.class
                );
                log.info("[告警通知] 钉钉通知已发送（通过网关）: rule={}, value={}", rule.getName(), record.getValue());
            } else {
                log.warn("[告警通知] 钉钉通知渠道已启用但未配置（GatewayServiceClient和Webhook URL均未配置），无法发送通知: rule={}, value={}",
                        rule.getName(), record.getValue());
            }

            if (record.getSeverity() == AlertManager.AlertSeverity.CRITICAL) {
                try {
                    String smsContent = String.format(
                            "【IOE-DREAM告警】%s：当前值%.2f，阈值%.2f，触发时间%s",
                            rule.getName(), record.getValue(), record.getThreshold(), record.getTriggerTime()
                    );
                    if (smsApiUrl != null && !smsApiUrl.isEmpty() && smsPhoneNumbers != null && !smsPhoneNumbers.isEmpty()) {
                        sendSmsNotificationDirectly(smsContent);
                        log.info("[告警通知] 严重告警短信通知已发送（直接调用）: rule={}, value={}, threshold={}",
                                rule.getName(), record.getValue(), record.getThreshold());
                    } else if (gatewayServiceClient != null) {
                        Map<String, Object> smsRequest = new HashMap<>();
                        smsRequest.put("notificationType", "sms");
                        smsRequest.put("content", smsContent);
                        smsRequest.put("templateCode", "ALERT_SMS");

                        gatewayServiceClient.callCommonService(
                                "/api/v1/notification/sms/send",
                                HttpMethod.POST,
                                smsRequest,
                                Object.class
                        );
                        log.info("[告警通知] 严重告警短信通知已发送（通过网关）: rule={}, value={}, threshold={}",
                                rule.getName(), record.getValue(), record.getThreshold());
                    } else {
                        log.warn("[告警通知] 严重告警短信通知未配置（GatewayServiceClient和短信API URL均未配置）: rule={}, value={}, threshold={}",
                                rule.getName(), record.getValue(), record.getThreshold());
                    }
                } catch (Exception smsEx) {
                    log.error("[告警通知] 发送严重告警短信通知失败, rule={}, error={}", rule.getName(), smsEx.getMessage(), smsEx);
                }
            }
        } catch (Exception e) {
            log.error("[告警通知] 发送钉钉通知失败, rule={}, error={}", rule.getName(), e.getMessage(), e);
        }
    }

    private void sendDingTalkNotificationDirectly(String content) {
        try {
            if (dingTalkWebhookUrl == null || dingTalkWebhookUrl.isEmpty()) {
                log.warn("[告警通知] 钉钉Webhook URL未配置，跳过直接调用");
                return;
            }

            Map<String, Object> text = new HashMap<>();
            text.put("content", content);

            Map<String, Object> payload = new HashMap<>();
            payload.put("msgtype", "text");
            payload.put("text", text);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            if (restTemplate == null) {
                log.warn("[告警通知] RestTemplate未配置，无法直接调用钉钉API");
                return;
            }
            restTemplate.postForEntity(dingTalkWebhookUrl, entity, String.class);

            log.debug("[告警通知] 钉钉通知发送成功（直接调用）");
        } catch (Exception e) {
            log.error("[告警通知] 直接调用钉钉API失败: error={}", e.getMessage(), e);
        }
    }

    private void sendDingTalkRecoveryNotification(AlertManager.AlertRule rule, double currentValue) {
        String content = String.format(
                "【告警恢复】%s\n\n" +
                        "当前数值: %.2f\n" +
                        "阈值设定: %.2f\n" +
                        "恢复时间: %s\n" +
                        "系统: IOE-DREAM智能管理平台",
                rule.getName(), currentValue, rule.getThreshold(), LocalDateTime.now()
        );

        try {
            if (dingTalkWebhookUrl != null && !dingTalkWebhookUrl.isEmpty()) {
                sendDingTalkNotificationDirectly(content);
                log.info("[告警通知] 钉钉恢复通知已发送（直接调用）: rule={}, value={}", rule.getName(), currentValue);
                return;
            }

            if (gatewayServiceClient != null) {
                Map<String, Object> request = new HashMap<>();
                request.put("notificationType", "dingtalk");
                request.put("msgtype", "text");
                Map<String, Object> message = new HashMap<>();
                message.put("content", content);
                request.put("message", message);

                gatewayServiceClient.callCommonService(
                        "/api/v1/notification/dingtalk/send",
                        HttpMethod.POST,
                        request,
                        Object.class
                );
                log.info("[告警通知] 钉钉恢复通知已发送（通过网关）: rule={}, value={}", rule.getName(), currentValue);
            } else {
                log.warn("[告警通知] 钉钉恢复通知未配置（GatewayServiceClient和Webhook URL均未配置）: {}", content);
            }
        } catch (Exception e) {
            log.error("[告警通知] 发送钉钉恢复通知失败, rule={}, error={}", rule.getName(), e.getMessage(), e);
        }
    }

    private void sendWeChatNotification(AlertManager.AlertRule rule, AlertManager.AlertRecord record, boolean isRepeated) {
        String title = isRepeated ? "【持续告警】" : "【新告警】";
        String content = String.format(
                "%s %s\n\n" +
                        "告警级别: %s %s\n" +
                        "当前数值: %.2f\n" +
                        "阈值设定: %.2f\n" +
                        "告警描述: %s\n" +
                        "触发时间: %s\n" +
                        "系统: IOE-DREAM智能管理平台",
                title, rule.getName(),
                record.getSeverity().getIcon(), record.getSeverity().getName(),
                record.getValue(), record.getThreshold(),
                rule.getDescription(), record.getTriggerTime()
        );

        try {
            if (weChatWebhookUrl != null && !weChatWebhookUrl.isEmpty()) {
                sendWeChatNotificationDirectly(content);
                log.info("[告警通知] 企业微信通知已发送（直接调用）: rule={}, value={}", rule.getName(), record.getValue());
                return;
            }

            if (gatewayServiceClient != null) {
                Map<String, Object> request = new HashMap<>();
                request.put("notificationType", "wechat");
                request.put("msgtype", "text");
                Map<String, Object> message = new HashMap<>();
                message.put("content", content);
                request.put("message", message);

                gatewayServiceClient.callCommonService(
                        "/api/v1/notification/wechat/send",
                        HttpMethod.POST,
                        request,
                        Object.class
                );
                log.info("[告警通知] 企业微信通知已发送（通过网关）: rule={}, value={}", rule.getName(), record.getValue());
            } else {
                log.warn("[告警通知] 企业微信通知渠道已启用但未配置（GatewayServiceClient和Webhook URL均未配置），无法发送通知: rule={}, value={}",
                        rule.getName(), record.getValue());
            }
        } catch (Exception e) {
            log.error("[告警通知] 发送企业微信通知失败, rule={}, error={}", rule.getName(), e.getMessage(), e);
        }
    }

    private void sendWeChatNotificationDirectly(String content) {
        try {
            if (weChatWebhookUrl == null || weChatWebhookUrl.isEmpty()) {
                log.warn("[告警通知] 企业微信Webhook URL未配置，跳过直接调用");
                return;
            }
            if (restTemplate == null) {
                log.warn("[告警通知] RestTemplate未配置，无法直接调用企业微信API");
                return;
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("msgtype", "text");
            Map<String, Object> text = new HashMap<>();
            text.put("content", content);
            payload.put("text", text);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            restTemplate.postForEntity(weChatWebhookUrl, entity, String.class);
            log.debug("[告警通知] 企业微信通知发送成功（直接调用）");
        } catch (Exception e) {
            log.error("[告警通知] 直接调用企业微信API失败: error={}", e.getMessage(), e);
        }
    }

    private void sendEmailNotification(AlertManager.AlertRule rule, AlertManager.AlertRecord record, boolean isRepeated) {
        String title = isRepeated ? "【持续告警】" : "【新告警】";
        String subject = String.format("%s %s - IOE-DREAM智能管理平台", title, rule.getName());
        String content = String.format(
                "<h2>%s %s</h2>" +
                        "<p><strong>告警级别:</strong> %s %s</p>" +
                        "<p><strong>当前数值:</strong> %.2f</p>" +
                        "<p><strong>阈值设定:</strong> %.2f</p>" +
                        "<p><strong>告警描述:</strong> %s</p>" +
                        "<p><strong>触发时间:</strong> %s</p>" +
                        "<p><strong>系统:</strong> IOE-DREAM智能管理平台</p>",
                title, rule.getName(),
                record.getSeverity().getIcon(), record.getSeverity().getName(),
                record.getValue(), record.getThreshold(),
                rule.getDescription(), record.getTriggerTime()
        );

        try {
            if (gatewayServiceClient != null) {
                Map<String, Object> request = new HashMap<>();
                request.put("notificationType", "email");
                request.put("subject", subject);
                request.put("content", content);
                request.put("templateCode", "ALERT_NOTIFICATION");

                gatewayServiceClient.callCommonService(
                        "/api/v1/notification/email/send",
                        HttpMethod.POST,
                        request,
                        Object.class
                );
                log.info("[告警通知] 邮件通知已发送, rule={}, value={}", rule.getName(), record.getValue());
            } else {
                log.warn("[告警通知] 邮件通知渠道已启用但未配置（GatewayServiceClient未配置），无法发送通知: rule={}, value={}",
                        rule.getName(), record.getValue());
            }
        } catch (Exception e) {
            log.error("[告警通知] 发送邮件通知失败, rule={}, error={}", rule.getName(), e.getMessage(), e);
        }
    }

    private void sendSMSNotification(AlertManager.AlertRule rule, AlertManager.AlertRecord record, boolean isRepeated) {
        if (record.getSeverity() != AlertManager.AlertSeverity.CRITICAL) {
            return;
        }

        String content = String.format(
                "【IOE-DREAM告警】%s：当前值%.2f，阈值%.2f，触发时间%s",
                rule.getName(), record.getValue(), record.getThreshold(), record.getTriggerTime()
        );

        try {
            if (smsApiUrl != null && !smsApiUrl.isEmpty() && smsPhoneNumbers != null && !smsPhoneNumbers.isEmpty()) {
                sendSmsNotificationDirectly(content);
                log.info("[告警通知] 短信通知已发送（直接调用）: rule={}, value={}", rule.getName(), record.getValue());
                return;
            }

            if (gatewayServiceClient != null) {
                Map<String, Object> request = new HashMap<>();
                request.put("notificationType", "sms");
                request.put("content", content);
                request.put("templateCode", "ALERT_SMS");

                gatewayServiceClient.callCommonService(
                        "/api/v1/notification/sms/send",
                        HttpMethod.POST,
                        request,
                        Object.class
                );
                log.info("[告警通知] 短信通知已发送（通过网关）: rule={}, value={}", rule.getName(), record.getValue());
            } else {
                log.warn("[告警通知] 短信通知渠道已启用但未配置（GatewayServiceClient和短信API URL均未配置），无法发送通知: rule={}, value={}",
                        rule.getName(), record.getValue());
            }
        } catch (Exception e) {
            log.error("[告警通知] 发送短信通知失败, rule={}, error={}", rule.getName(), e.getMessage(), e);
        }
    }

    private void sendSmsNotificationDirectly(String content) {
        try {
            if (smsApiUrl == null || smsApiUrl.isEmpty()) {
                log.warn("[告警通知] 短信API URL未配置，跳过直接调用");
                return;
            }
            if (smsPhoneNumbers == null || smsPhoneNumbers.isEmpty()) {
                log.warn("[告警通知] 短信接收号码未配置，跳过直接调用");
                return;
            }
            if (restTemplate == null) {
                log.warn("[告警通知] RestTemplate未配置，无法直接调用短信API");
                return;
            }

            boolean success;
            String provider = smsProvider != null ? smsProvider.toLowerCase() : "webhook";
            switch (provider) {
                case "aliyun":
                    success = sendAliyunSms(content, smsPhoneNumbers);
                    break;
                case "tencent":
                    success = sendTencentSms(content, smsPhoneNumbers);
                    break;
                case "webhook":
                default:
                    success = sendSmsViaWebhook(content, smsPhoneNumbers);
                    break;
            }

            if (success) {
                log.debug("[告警通知] 短信通知发送成功（直接调用）: provider={}", provider);
            } else {
                log.error("[告警通知] 短信通知发送失败（直接调用）: provider={}", provider);
            }
        } catch (Exception e) {
            log.error("[告警通知] 直接调用短信API失败: error={}", e.getMessage(), e);
        }
    }

    private boolean sendSmsViaWebhook(String content, String phoneNumbers) {
        try {
            if (smsApiUrl == null || smsApiUrl.isEmpty()) {
                log.warn("[告警通知] 短信API URL未配置，跳过发送");
                return false;
            }
            if (restTemplate == null) {
                return false;
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("phones", phoneNumbers.split(","));
            payload.put("content", content);
            payload.put("timestamp", LocalDateTime.now().toString());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (smsApiKey != null && !smsApiKey.isEmpty()) {
                headers.set("Authorization", "Bearer " + smsApiKey);
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(smsApiUrl, entity, String.class);
            return true;
        } catch (Exception e) {
            log.error("[告警通知] Webhook短信发送失败: error={}", e.getMessage(), e);
            return false;
        }
    }

    private boolean sendAliyunSms(String content, String phoneNumbers) {
        try {
            if (smsApiUrl == null || smsApiUrl.isEmpty()) {
                log.warn("[告警通知] 短信API URL未配置，跳过发送");
                return false;
            }
            if (restTemplate == null) {
                return false;
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("PhoneNumbers", phoneNumbers);
            payload.put("SignName", "IOE-DREAM");
            payload.put("TemplateCode", "SMS_ALERT");
            Map<String, Object> templateParam = new HashMap<>();
            templateParam.put("content", content);
            payload.put("TemplateParam", templateParam);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (smsApiKey != null && !smsApiKey.isEmpty()) {
                headers.set("Authorization", "Bearer " + smsApiKey);
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(smsApiUrl, entity, String.class);
            return true;
        } catch (Exception e) {
            log.error("[告警通知] 阿里云短信发送失败: error={}", e.getMessage(), e);
            return false;
        }
    }

    private boolean sendTencentSms(String content, String phoneNumbers) {
        try {
            if (smsApiUrl == null || smsApiUrl.isEmpty()) {
                log.warn("[告警通知] 短信API URL未配置，跳过发送");
                return false;
            }
            if (restTemplate == null) {
                return false;
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("PhoneNumberSet", phoneNumbers.split(","));
            payload.put("TemplateID", "SMS_ALERT");
            payload.put("TemplateParamSet", Collections.singletonList(content));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (smsApiKey != null && !smsApiKey.isEmpty()) {
                headers.set("Authorization", "Bearer " + smsApiKey);
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(smsApiUrl, entity, String.class);
            return true;
        } catch (Exception e) {
            log.error("[告警通知] 腾讯云短信发送失败: error={}", e.getMessage(), e);
            return false;
        }
    }
}

