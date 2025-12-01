package net.lab1024.sa.base.module.device.enums;

/**
 * 协议类型枚举
 * <p>
 * 定义智能设备支持的通信协议类型
 * 用于设备连接、通信配置和协议适配
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
public enum ProtocolTypeEnum {

    /**
     * TCP协议
     * 传输控制协议，提供可靠的面向连接的通信服务
     */
    TCP("TCP", "TCP协议"),

    /**
     * UDP协议
     * 用户数据报协议，提供无连接的通信服务
     */
    UDP("UDP", "UDP协议"),

    /**
     * HTTP协议
     * 超文本传输协议，用于Web服务通信
     */
    HTTP("HTTP", "HTTP协议"),

    /**
     * HTTPS协议
     * 安全的超文本传输协议，加密的HTTP通信
     */
    HTTPS("HTTPS", "HTTPS协议"),

    /**
     * MQTT协议
     * 消息队列遥测传输协议，用于物联网设备通信
     */
    MQTT("MQTT", "MQTT协议"),

    /**
     * WebSocket协议
     * 全双工通信协议，用于实时数据传输
     */
    WEBSOCKET("WEBSOCKET", "WebSocket协议");

    private final String value;
    private final String description;

    ProtocolTypeEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 获取枚举值
     *
     * @return 枚举值字符串
     */
    public String getValue() {
        return value;
    }

    /**
     * 获取描述信息
     *
     * @return 描述信息
     */
    public String getDescription() {
        return description;
    }

    /**
     * 根据值获取枚举
     *
     * @param value 枚举值
     * @return 对应的枚举
     * @throws IllegalArgumentException 当值不存在时抛出异常
     */
    public static ProtocolTypeEnum fromValue(String value) {
        for (ProtocolTypeEnum type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown protocol type: " + value);
    }

    /**
     * 检查值是否有效
     *
     * @param value 待检查的值
     * @return 是否有效
     */
    public static boolean isValid(String value) {
        try {
            fromValue(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 获取所有协议类型的值数组
     *
     * @return 所有值数组
     */
    public static String[] getAllValues() {
        ProtocolTypeEnum[] types = values();
        String[] values = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            values[i] = types[i].value;
        }
        return values;
    }

    /**
     * 获取所有协议类型的描述数组
     *
     * @return 所有描述数组
     */
    public static String[] getAllDescriptions() {
        ProtocolTypeEnum[] types = values();
        String[] descriptions = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            descriptions[i] = types[i].description;
        }
        return descriptions;
    }

    /**
     * 检查是否为Web协议
     * Web协议包括：HTTP、HTTPS、WebSocket
     *
     * @return 是否为Web协议
     */
    public boolean isWebProtocol() {
        return this == HTTP || this == HTTPS || this == WEBSOCKET;
    }

    /**
     * 检查是否为加密协议
     * 加密协议包括：HTTPS
     *
     * @return 是否为加密协议
     */
    public boolean isSecureProtocol() {
        return this == HTTPS;
    }

    /**
     * 检查是否为物联网协议
     * 物联网协议包括：MQTT
     *
     * @return 是否为物联网协议
     */
    public boolean isIoTProtocol() {
        return this == MQTT;
    }

    /**
     * 获取默认端口
     * 返回该协议的常用默认端口号
     *
     * @return 默认端口号
     */
    public int getDefaultPort() {
        switch (this) {
            case HTTP:
                return 80;
            case HTTPS:
                return 443;
            case MQTT:
                return 1883;
            case WEBSOCKET:
                return 8080;
            case TCP:
            case UDP:
            default:
                return -1; // 无固定默认端口
        }
    }

    /**
     * 检查是否需要加密
     *
     * @return 是否需要加密
     */
    public boolean requiresEncryption() {
        return this == HTTPS;
    }
}