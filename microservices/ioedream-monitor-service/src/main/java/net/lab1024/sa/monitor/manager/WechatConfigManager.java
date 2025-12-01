package net.lab1024.sa.monitor.manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 微信配置管理器
 *
 * 负责微信企业号相关配置管理
 *
 * @author IOE-DREAM Team
 */
@Slf4j
@Component
public class WechatConfigManager {

    @Value("${monitor.notification.wechat.corp-id:}")
    private String corpId;

    @Value("${monitor.notification.wechat.corp-secret:}")
    private String corpSecret;

    @Value("${monitor.notification.wechat.agent-id:1000001}")
    private String agentId;

    @Value("${monitor.notification.wechat.token:}")
    private String token;

    @Value("${monitor.notification.wechat.encoding-aes-key:}")
    private String encodingAESKey;

    @Value("${monitor.notification.wechat.api-url:https://qyapi.weixin.qq.com}")
    private String apiUrl;

    @Value("${monitor.notification.wechat.enabled:false}")
    private boolean enabled;

    @Value("${monitor.notification.wechat.default-receiver:}")
    private String defaultReceiver;

    @Value("${monitor.notification.wechat.default-receiver-type:USER}")
    private String defaultReceiverType;

    // Getter方法
    public String getCorpId() { return corpId; }
    public String getCorpSecret() { return corpSecret; }
    public String getAgentId() { return agentId; }
    public String getToken() { return token; }
    public String getEncodingAESKey() { return encodingAESKey; }
    public String getApiUrl() { return apiUrl; }
    public boolean isEnabled() { return enabled; }
    public String getDefaultReceiver() { return defaultReceiver; }
    public String getDefaultReceiverType() { return defaultReceiverType; }
}