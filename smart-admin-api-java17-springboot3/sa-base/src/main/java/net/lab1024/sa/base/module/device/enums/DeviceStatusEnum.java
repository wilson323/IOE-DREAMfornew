package net.lab1024.sa.base.module.device.enums;

/**
 * 设备状态枚举
 * <p>
 * 定义智能设备的各种运行状态
 * 用于设备监控、状态管理和告警处理
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
public enum DeviceStatusEnum {

    /**
     * 在线状态
     * 设备正常连接，可以正常通信和操作
     */
    ONLINE("ONLINE", "在线"),

    /**
     * 离线状态
     * 设备断开连接，无法通信
     */
    OFFLINE("OFFLINE", "离线"),

    /**
     * 故障状态
     * 设备出现硬件或软件故障，需要维修
     */
    FAULT("FAULT", "故障"),

    /**
     * 维护中状态
     * 设备正在进行维护或升级，暂时不可用
     */
    MAINTAIN("MAINTAIN", "维护中");

    private final String value;
    private final String description;

    DeviceStatusEnum(String value, String description) {
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
    public static DeviceStatusEnum fromValue(String value) {
        for (DeviceStatusEnum status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown device status: " + value);
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
     * 获取所有设备状态的值数组
     *
     * @return 所有值数组
     */
    public static String[] getAllValues() {
        DeviceStatusEnum[] statuses = values();
        String[] values = new String[statuses.length];
        for (int i = 0; i < statuses.length; i++) {
            values[i] = statuses[i].value;
        }
        return values;
    }

    /**
     * 获取所有设备状态的描述数组
     *
     * @return 所有描述数组
     */
    public static String[] getAllDescriptions() {
        DeviceStatusEnum[] statuses = values();
        String[] descriptions = new String[statuses.length];
        for (int i = 0; i < statuses.length; i++) {
            descriptions[i] = statuses[i].description;
        }
        return descriptions;
    }

    /**
     * 检查是否为正常状态
     * 正常状态包括：在线、离线
     *
     * @return 是否为正常状态
     */
    public boolean isNormalStatus() {
        return this == ONLINE || this == OFFLINE;
    }

    /**
     * 检查是否为异常状态
     * 异常状态包括：故障、维护中
     *
     * @return 是否为异常状态
     */
    public boolean isAbnormalStatus() {
        return this == FAULT || this == MAINTAIN;
    }

    /**
     * 检查是否可操作状态
     * 可操作状态：在线
     *
     * @return 是否可操作
     */
    public boolean isOperable() {
        return this == ONLINE;
    }
}