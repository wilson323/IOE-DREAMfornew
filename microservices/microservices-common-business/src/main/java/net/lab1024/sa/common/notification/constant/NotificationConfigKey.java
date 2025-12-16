package net.lab1024.sa.common.notification.constant;

/**
 * 通知配置键常量
 * <p>
 * 定义各种通知渠道的配置键名称
 * 格式：{CONFIG_TYPE}.{KEY_NAME}
 * 严格遵循CLAUDE.md规范:
 * - 使用常量类统一管理配置键
 * - 避免硬编码配置键名称
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public class NotificationConfigKey {

    /**
     * 邮件配置键
     */
    public static class Email {
        public static final String SMTP_HOST = "EMAIL.SMTP_HOST";
        public static final String SMTP_PORT = "EMAIL.SMTP_PORT";
        public static final String SMTP_USERNAME = "EMAIL.SMTP_USERNAME";
        public static final String SMTP_PASSWORD = "EMAIL.SMTP_PASSWORD";
        public static final String FROM_ADDRESS = "EMAIL.FROM_ADDRESS";
        public static final String FROM_NAME = "EMAIL.FROM_NAME";
        public static final String SSL_ENABLED = "EMAIL.SSL_ENABLED";
        public static final String TLS_ENABLED = "EMAIL.TLS_ENABLED";
    }

    /**
     * 短信配置键
     */
    public static class Sms {
        public static final String ACCESS_KEY_ID = "SMS.ACCESS_KEY_ID";
        public static final String ACCESS_KEY_SECRET = "SMS.ACCESS_KEY_SECRET";
        public static final String SIGN_NAME = "SMS.SIGN_NAME";
        public static final String TEMPLATE_CODE = "SMS.TEMPLATE_CODE";
        public static final String REGION = "SMS.REGION";
    }

    /**
     * 钉钉配置键
     */
    public static class DingTalk {
        public static final String WEBHOOK_URL = "DINGTALK.WEBHOOK_URL";
        public static final String SECRET = "DINGTALK.SECRET";
        public static final String AGENT_ID = "DINGTALK.AGENT_ID";
        public static final String APP_KEY = "DINGTALK.APP_KEY";
        public static final String APP_SECRET = "DINGTALK.APP_SECRET";
    }

    /**
     * 企业微信配置键
     */
    public static class Wechat {
        public static final String CORP_ID = "WECHAT.CORP_ID";
        public static final String CORP_SECRET = "WECHAT.CORP_SECRET";
        public static final String AGENT_ID = "WECHAT.AGENT_ID";
        public static final String TOKEN = "WECHAT.TOKEN";
        public static final String ENCODING_AES_KEY = "WECHAT.ENCODING_AES_KEY";
    }

    /**
     * Webhook配置键
     */
    public static class Webhook {
        public static final String URL = "WEBHOOK.URL";
        public static final String METHOD = "WEBHOOK.METHOD";
        public static final String HEADERS = "WEBHOOK.HEADERS";
        public static final String SIGNATURE_KEY = "WEBHOOK.SIGNATURE_KEY";
        public static final String TIMEOUT = "WEBHOOK.TIMEOUT";
    }

    /**
     * 私有构造函数，防止实例化
     */
    private NotificationConfigKey() {
        throw new UnsupportedOperationException("常量类不允许实例化");
    }
}
