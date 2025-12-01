package net.lab1024.sa.admin.module.smart.biometric.constant;

import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 系统安全级别枚举
 *
 * @author SmartAdmin Team
 * @date 2025-01-15
 */
public enum SecurityLevelEnum implements BaseEnum {

    /**
     * 低安全级别
     */
    LOW("LOW", "低安全级别"),

    /**
     * 中安全级别
     */
    MEDIUM("MEDIUM", "中安全级别"),

    /**
     * 高安全级别
     */
    HIGH("HIGH", "高安全级别"),

    /**
     * 关键安全级别
     */
    CRITICAL("CRITICAL", "关键安全级别");

    private final String value;
    private final String description;

    SecurityLevelEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public String getValue() {
        return value;
    }

    
    @Override
    public String getDesc() {
        return description;
    }

    public static SecurityLevelEnum fromValue(String value) {
        for (SecurityLevelEnum level : values()) {
            if (level.getValue().equals(value)) {
                return level;
            }
        }
        return null;
    }

    /**
     * 获取级别数值
     */
    public int getLevel() {
        switch (this) {
            case LOW:
                return 1;
            case MEDIUM:
                return 2;
            case HIGH:
                return 3;
            case CRITICAL:
                return 4;
            default:
                return 1;
        }
    }

    /**
     * 获取所需生物识别类型数量
     */
    public int getRequiredBiometricTypes() {
        switch (this) {
            case LOW:
                return 1;
            case MEDIUM:
                return 2;
            case HIGH:
                return 3;
            case CRITICAL:
                return 4;
            default:
                return 1;
        }
    }
}