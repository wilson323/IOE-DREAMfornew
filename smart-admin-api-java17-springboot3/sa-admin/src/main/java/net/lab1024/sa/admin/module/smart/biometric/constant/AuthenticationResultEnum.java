package net.lab1024.sa.admin.module.smart.biometric.constant;

import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 认证结果状态枚举
 *
 * @author SmartAdmin Team
 * @date 2025-01-15
 */
public enum AuthenticationResultEnum implements BaseEnum {

    /**
     * 认证成功
     */
    SUCCESS("SUCCESS", "认证成功"),

    /**
     * 认证失败
     */
    FAILURE("FAILURE", "认证失败"),

    /**
     * 超时
     */
    TIMEOUT("TIMEOUT", "认证超时"),

    /**
     * 设备错误
     */
    DEVICE_ERROR("DEVICE_ERROR", "设备错误"),

    /**
     * 数据无效
     */
    INVALID_DATA("INVALID_DATA", "数据无效"),

    /**
     * 权限不足
     */
    INSUFFICIENT_PERMISSION("INSUFFICIENT_PERMISSION", "权限不足"),

    /**
     * 活体检测失败
     */
    LIVENESS_FAILURE("LIVENESS_FAILURE", "活体检测失败");

    private final String value;
    private final String description;

    AuthenticationResultEnum(String value, String description) {
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

    public static AuthenticationResultEnum fromValue(String value) {
        for (AuthenticationResultEnum result : values()) {
            if (result.getValue().equals(value)) {
                return result;
            }
        }
        return null;
    }

    /**
     * 判断是否为成功状态
     */
    public boolean isSuccess() {
        return this == SUCCESS;
    }

    /**
     * 判断是否为失败状态
     */
    public boolean isFailure() {
        return this == FAILURE || this == TIMEOUT || this == DEVICE_ERROR || this == INVALID_DATA;
    }

    /**
     * 判断是否为错误状态
     */
    public boolean isError() {
        return this == INSUFFICIENT_PERMISSION || this == LIVENESS_FAILURE;
    }
}