package net.lab1024.sa.monitor.manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 邮件配置管理器
 *
 * 负责邮件相关配置管理
 *
 * @author IOE-DREAM Team
 */
@Slf4j
@Component
public class EmailConfigManager {

    @Value("${monitor.notification.email.smtp.host:smtp.gmail.com}")
    private String smtpHost;

    @Value("${monitor.notification.email.smtp.port:587}")
    private String smtpPort;

    @Value("${monitor.notification.email.smtp.auth:true}")
    private boolean smtpAuth;

    @Value("${monitor.notification.email.starttls.enable:true}")
    private boolean starttlsEnable;

    @Value("${monitor.notification.email.ssl.enable:false}")
    private boolean sslEnable;

    @Value("${monitor.notification.email.debug:false}")
    private boolean debug;

    @Value("${monitor.notification.email.from:monitor@ioe-dream.com}")
    private String fromAddress;

    @Value("${monitor.notification.email.username:}")
    private String username;

    @Value("${monitor.notification.email.password:}")
    private String password;

    @Value("${monitor.notification.email.test.recipient:admin@ioe-dream.com}")
    private String testRecipient;

    // Getter方法
    public String getSmtpHost() { return smtpHost; }
    public String getSmtpPort() { return smtpPort; }
    public boolean isSmtpAuth() { return smtpAuth; }
    public boolean isStarttlsEnable() { return starttlsEnable; }
    public boolean isSslEnable() { return sslEnable; }
    public boolean isDebug() { return debug; }
    public String getFromAddress() { return fromAddress; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getTestRecipient() { return testRecipient; }
}