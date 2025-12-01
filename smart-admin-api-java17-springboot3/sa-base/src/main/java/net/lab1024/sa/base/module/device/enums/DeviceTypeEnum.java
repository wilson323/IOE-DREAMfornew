package net.lab1024.sa.base.module.device.enums;

/**
 * 设备类型枚举
 * <p>
 * 定义IOE-DREAM系统中支持的所有智能设备类型
 * 用于设备分类、权限控制和业务模块路由
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
public enum DeviceTypeEnum {

    /**
     * 门禁设备
     * 负责人员进出控制、身份验证等功能
     */
    ACCESS("ACCESS", "门禁设备"),

    /**
     * 考勤设备
     * 负责员工考勤打卡、时间记录等功能
     */
    ATTENDANCE("ATTENDANCE", "考勤设备"),

    /**
     * 消费设备
     * 负责收费支付、账户扣款等功能
     */
    CONSUME("CONSUME", "消费设备"),

    /**
     * 视频设备
     * 负责视频监控、录像存储等功能
     */
    VIDEO("VIDEO", "视频设备");

    private final String value;
    private final String description;

    DeviceTypeEnum(String value, String description) {
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
    public static DeviceTypeEnum fromValue(String value) {
        for (DeviceTypeEnum type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown device type: " + value);
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
     * 获取所有设备类型的值数组
     *
     * @return 所有值数组
     */
    public static String[] getAllValues() {
        DeviceTypeEnum[] types = values();
        String[] values = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            values[i] = types[i].value;
        }
        return values;
    }

    /**
     * 获取所有设备类型的描述数组
     *
     * @return 所有描述数组
     */
    public static String[] getAllDescriptions() {
        DeviceTypeEnum[] types = values();
        String[] descriptions = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            descriptions[i] = types[i].description;
        }
        return descriptions;
    }
}