package net.lab1024.sa.common.notification.constant;

/**
 * 通知模板类型枚举
 * <p>
 * 定义通知模板的类型
 * 严格遵循CLAUDE.md规范:
 * - 使用枚举类型确保类型安全
 * - 提供完整的描述信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public enum NotificationTemplateType {

    /**
     * 邮件模板
     */
    EMAIL(1, "邮件模板"),

    /**
     * 短信模板
     */
    SMS(2, "短信模板"),

    /**
     * 微信模板
     */
    WECHAT(3, "微信模板"),

    /**
     * 站内信模板
     */
    INNER(4, "站内信模板"),

    /**
     * 推送模板
     */
    PUSH(5, "推送模板");

    private final Integer code;
    private final String description;

    NotificationTemplateType(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据代码获取枚举
     *
     * @param code 代码
     * @return 枚举值，如果不存在返回null
     */
    public static NotificationTemplateType fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (NotificationTemplateType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
