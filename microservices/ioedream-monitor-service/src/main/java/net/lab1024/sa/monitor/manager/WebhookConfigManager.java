package net.lab1024.sa.monitor.manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Webhook配置管理器
 *
 * 负责Webhook相关配置管理
 *
 * @author IOE-DREAM Team
 */
@Slf4j
@Component
public class WebhookConfigManager {

    @Value("${monitor.notification.webhook.timeout:30000}")
    private int timeoutMs;

    @Value("${monitor.notification.webhook.max-retries:3}")
    private int maxRetries;

    @Value("${monitor.notification.webhook.retry-delay:5000}")
    private int retryDelayMs;

    @Value("${monitor.notification.webhook.user-agent:IOE-DREAM-Monitor/1.0}")
    private String userAgent;

    @Value("${monitor.notification.webhook.enable-ssl-verification:true}")
    private boolean enableSslVerification;

    @Value("${monitor.notification.webhook.default-headers:}")
    private String defaultHeaders;

    // Getter方法
    public int getTimeoutMs() { return timeoutMs; }
    public int getMaxRetries() { return maxRetries; }
    public int getRetryDelayMs() { return retryDelayMs; }
    public String getUserAgent() { return userAgent; }
    public boolean isEnableSslVerification() { return enableSslVerification; }
    public String getDefaultHeaders() { return defaultHeaders; }
}