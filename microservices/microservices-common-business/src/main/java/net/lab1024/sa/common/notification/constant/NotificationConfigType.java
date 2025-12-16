package net.lab1024.sa.common.notification.constant;

/**
 * 通知配置类型枚举
 * <p>
 * 定义各种通知渠道的配置类型
 * 严格遵循CLAUDE.md规范:
 * - 使用枚举类型确保类型安全
 * - 提供清晰的类型说明
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public enum NotificationConfigType {

    /**
     * 邮件配置
     * <p>
     * 包含SMTP服务器、端口、用户名、密码等配置
     * </p>
     */
    EMAIL("EMAIL", "邮件配置"),

    /**
     * 短信配置
     * <p>
     * 包含阿里云AccessKey、SecretKey、签名、模板等配置
     * </p>
     */
    SMS("SMS", "短信配置"),

    /**
     * 钉钉配置
     * <p>
     * 包含Webhook URL、Secret、AgentId等配置
     * </p>
     */
    DINGTALK("DINGTALK", "钉钉配置"),

    /**
     * 企业微信配置
     * <p>
     * 包含CorpId、CorpSecret、AgentId等配置
     * </p>
     */
    WECHAT("WECHAT", "企业微信配置"),

    /**
     * Webhook配置
     * <p>
     * 包含Webhook URL、请求头、签名密钥等配置
     * </p>
     */
    WEBHOOK("WEBHOOK", "Webhook配置");

    /**
     * 配置类型代码
     */
    private final String code;

    /**
     * 配置类型描述
     */
    private final String description;

    /**
     * 构造函数
     *
     * @param code        配置类型代码
     * @param description 配置类型描述
     */
    NotificationConfigType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 获取配置类型代码
     *
     * @return 配置类型代码
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取配置类型描述
     *
     * @return 配置类型描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 根据代码获取枚举
     *
     * @param code 配置类型代码
     * @return 通知配置类型枚举
     */
    public static NotificationConfigType fromCode(String code) {
        for (NotificationConfigType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的通知配置类型: " + code);
    }
}
