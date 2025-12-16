package net.lab1024.sa.devicecomm.protocol.enums;

import lombok.Getter;

/**
 * 协议类型枚举
 * <p>
 * 定义所有支持的设备协议类型
 * 严格遵循CLAUDE.md规范：
 * - 使用枚举类型确保类型安全
 * - 包含完整的协议信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Getter
public enum ProtocolTypeEnum {

    /**
     * 考勤PUSH协议（熵基科技 V4.0）
     */
    ATTENDANCE_ENTROPY_V4_0("ATTENDANCE_ENTROPY_V4.0", "考勤PUSH协议", "熵基科技", "V4.0", "ATTENDANCE"),

    /**
     * 门禁PUSH协议（熵基科技 V4.8）
     */
    ACCESS_ENTROPY_V4_8("ACCESS_ENTROPY_V4.8", "安防PUSH协议", "熵基科技", "V4.8", "ACCESS"),

    /**
     * 消费PUSH协议（中控智慧 V1.0）
     */
    CONSUME_ZKTECO_V1_0("CONSUME_ZKTECO_V1.0", "消费PUSH协议", "中控智慧", "V1.0", "CONSUME");

    /**
     * 协议类型代码
     */
    private final String code;

    /**
     * 协议名称
     */
    private final String name;

    /**
     * 设备厂商
     */
    private final String manufacturer;

    /**
     * 协议版本
     */
    private final String version;

    /**
     * 设备类型
     */
    private final String deviceType;

    /**
     * 构造函数
     *
     * @param code 协议类型代码
     * @param name 协议名称
     * @param manufacturer 设备厂商
     * @param version 协议版本
     * @param deviceType 设备类型
     */
    ProtocolTypeEnum(String code, String name, String manufacturer, String version, String deviceType) {
        this.code = code;
        this.name = name;
        this.manufacturer = manufacturer;
        this.version = version;
        this.deviceType = deviceType;
    }

    /**
     * 根据协议代码获取枚举
     *
     * @param code 协议代码
     * @return 协议类型枚举，如果不存在返回null
     */
    public static ProtocolTypeEnum getByCode(String code) {
        for (ProtocolTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 根据设备类型和厂商获取协议类型
     *
     * @param deviceType 设备类型（ATTENDANCE、ACCESS、CONSUME）
     * @param manufacturer 厂商名称
     * @return 协议类型枚举，如果不存在返回null
     */
    public static ProtocolTypeEnum getByDeviceTypeAndManufacturer(String deviceType, String manufacturer) {
        for (ProtocolTypeEnum type : values()) {
            if (type.getDeviceType().equals(deviceType) && type.getManufacturer().contains(manufacturer)) {
                return type;
            }
        }
        return null;
    }
}

