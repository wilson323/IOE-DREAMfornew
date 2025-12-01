package net.lab1024.sa.admin.module.consume.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户偏好设置枚举
 *
 * <p>
 * 严格遵循repowiki规范：
 * - 使用枚举定义用户偏好，避免魔法数字
 * - 提供完整的业务信息和描述
 * - 支持根据代码获取枚举实例
 * - 包含友好的显示名称
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-27
 */
@Getter
@AllArgsConstructor
public enum UserPreferenceEnum {

    /**
     * 语言偏好
     */
    LANGUAGE("LANGUAGE", "语言偏好", "用户界面的语言设置"),

    /**
     * 主题偏好
     */
    THEME("THEME", "主题偏好", "界面主题颜色和样式偏好"),

    /**
     * 通知偏好
     */
    NOTIFICATION("NOTIFICATION", "通知偏好", "接收通知的类型和方式偏好"),

    /**
     * 支付偏好
     */
    PAYMENT("PAYMENT", "支付偏好", "常用支付方式偏好"),

    /**
     * 配送偏好
     */
    DELIVERY("DELIVERY", "配送偏好", "收货地址和配送方式偏好"),

    /**
     * 隐私偏好
     */
    PRIVACY("PRIVACY", "隐私偏好", "个人信息的隐私保护设置"),

    /**
     * 显示偏好
     */
    DISPLAY("DISPLAY", "显示偏好", "数据展示和布局偏好"),

    /**
     * 导出偏好
     */
    EXPORT("EXPORT", "导出偏好", "数据导出的格式和内容偏好");

    /**
     * 偏好类型代码
     */
    private final String code;

    /**
     * 偏好类型名称
     */
    private final String name;

    /**
     * 偏好类型描述
     */
    private final String description;

    /**
     * 根据代码获取用户偏好设置枚举
     *
     * @param code 用户偏好设置代码
     * @return 用户偏好设置枚举，如果未找到返回null
     */
    public static UserPreferenceEnum getByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        for (UserPreferenceEnum preference : values()) {
            if (preference.getCode().equals(code.trim())) {
                return preference;
            }
        }
        return null;
    }

    /**
     * 根据名称获取用户偏好设置枚举
     *
     * @param name 用户偏好设置名称
     * @return 用户偏好设置枚举，如果未找到返回null
     */
    public static UserPreferenceEnum getByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        for (UserPreferenceEnum preference : values()) {
            if (preference.getName().equals(name.trim())) {
                return preference;
            }
        }
        return null;
    }

    /**
     * 判断是否为界面偏好
     *
     * @return 是否为界面偏好
     */
    public boolean isInterfacePreference() {
        return this == LANGUAGE || this == THEME || this == DISPLAY;
    }

    /**
     * 判断是否为功能偏好
     *
     * @return 是否为功能偏好
     */
    public boolean isFunctionalPreference() {
        return this == NOTIFICATION || this == PAYMENT || this == DELIVERY || this == EXPORT;
    }

    /**
     * 判断是否为安全偏好
     *
     * @return 是否为安全偏好
     */
    public boolean isSecurityPreference() {
        return this == PRIVACY;
    }

    /**
     * 判断是否需要用户输入
     *
     * @return 是否需要用户输入
     */
    public boolean requiresUserInput() {
        return this != PRIVACY;
    }

    /**
     * 判断是否可以默认设置
     *
     * @return 是否可以默认设置
     */
    public boolean hasDefaultValue() {
        return true;
    }

    /**
     * 获取推荐的默认值
     *
     * @return 推荐的默认值
     */
    public String getRecommendedDefaultValue() {
        switch (this) {
            case LANGUAGE:
                return "zh-CN";
            case THEME:
                return "light";
            case NOTIFICATION:
                return "email,sms";
            case PAYMENT:
                return "wechat,alipay";
            case DELIVERY:
                return "home_address";
            case PRIVACY:
                return "standard";
            case DISPLAY:
                return "grid";
            case EXPORT:
                return "excel";
            default:
                return "";
        }
    }

    /**
     * 获取可选值列表
     *
     * @return 可选值列表
     */
    public String[] getAvailableOptions() {
        switch (this) {
            case LANGUAGE:
                return new String[]{"zh-CN", "en-US", "ja-JP", "ko-KR"};
            case THEME:
                return new String[]{"light", "dark", "auto"};
            case NOTIFICATION:
                return new String[]{"email", "sms", "app_push", "wechat"};
            case PAYMENT:
                return new String[]{"wechat", "alipay", "bank_card", "cash"};
            case DELIVERY:
                return new String[]{"home_address", "company_address", "pickup_point"};
            case PRIVACY:
                return new String[]{"public", "friends", "private"};
            case DISPLAY:
                return new String[]{"grid", "list", "card"};
            case EXPORT:
                return new String[]{"excel", "csv", "pdf", "json"};
            default:
                return new String[]{};
        }
    }

    /**
     * 获取优先级（数字越大优先级越高）
     *
     * @return 优先级数值
     */
    public int getPriority() {
        switch (this) {
            case LANGUAGE:
                return 8;
            case PAYMENT:
                return 7;
            case DELIVERY:
                return 6;
            case NOTIFICATION:
                return 5;
            case PRIVACY:
                return 4;
            case THEME:
                return 3;
            case DISPLAY:
                return 2;
            case EXPORT:
                return 1;
            default:
                return 0;
        }
    }

    @Override
    public String toString() {
        return String.format("UserPreferenceEnum{code='%s', name='%s', description='%s'}",
                code, name, description);
    }
}