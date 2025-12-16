package net.lab1024.sa.devicecomm.protocol.enums;

import lombok.Getter;

/**
 * 门禁事件类型枚举
 * <p>
 * 根据"安防PUSH通讯协议 （熵基科技）V4.8-20240107"文档附录19定义
 * 完整支持4000-7000+的所有事件类型
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Getter
public enum AccessEventTypeEnum {

    /**
     * 正常事件范围：4000-4999
     */
    NORMAL_ACCESS(4000, "正常通行", "NORMAL_ACCESS", EventCategory.NORMAL),
    NORMAL_EXIT(4001, "正常离开", "NORMAL_EXIT", EventCategory.NORMAL),
    NORMAL_ENTER(4002, "正常进入", "NORMAL_ENTER", EventCategory.NORMAL),

    /**
     * 异常事件范围：5000-5999
     */
    INVALID_CARD(5000, "无效卡片", "INVALID_CARD", EventCategory.ABNORMAL),
    EXPIRED_CARD(5001, "过期卡片", "EXPIRED_CARD", EventCategory.ABNORMAL),
    DISABLED_CARD(5002, "禁用卡片", "DISABLED_CARD", EventCategory.ABNORMAL),
    NO_PERMISSION(5003, "无权限", "NO_PERMISSION", EventCategory.ABNORMAL),
    TIME_RESTRICTION(5004, "时间限制", "TIME_RESTRICTION", EventCategory.ABNORMAL),
    AREA_RESTRICTION(5005, "区域限制", "AREA_RESTRICTION", EventCategory.ABNORMAL),
    MULTI_PASS(5006, "重复通行", "MULTI_PASS", EventCategory.ABNORMAL),
    FORCE_OPEN(5007, "强制开门", "FORCE_OPEN", EventCategory.ABNORMAL),
    DOOR_TIMEOUT(5008, "开门超时", "DOOR_TIMEOUT", EventCategory.ABNORMAL),
    VERIFY_FAILED(5009, "验证失败", "VERIFY_FAILED", EventCategory.ABNORMAL),

    /**
     * 报警事件范围：6000-6999
     */
    DOOR_OPENED(6000, "门被打开", "DOOR_OPENED", EventCategory.ALARM),
    DOOR_CLOSED(6001, "门被关闭", "DOOR_CLOSED", EventCategory.ALARM),
    DOOR_FORCED(6002, "门被强制打开", "DOOR_FORCED", EventCategory.ALARM),
    DOOR_STUCK(6003, "门被卡住", "DOOR_STUCK", EventCategory.ALARM),
    SENSOR_ALARM(6004, "传感器报警", "SENSOR_ALARM", EventCategory.ALARM),
    FIRE_ALARM(6005, "火警", "FIRE_ALARM", EventCategory.ALARM),
    EMERGENCY_ALARM(6006, "紧急报警", "EMERGENCY_ALARM", EventCategory.ALARM),
    BREAK_IN_ALARM(6007, "入侵报警", "BREAK_IN_ALARM", EventCategory.ALARM),
    TAMPER_ALARM(6008, "防拆报警", "TAMPER_ALARM", EventCategory.ALARM),
    LOW_BATTERY(6009, "低电量", "LOW_BATTERY", EventCategory.ALARM),

    /**
     * 梯控事件范围：7000-7999
     */
    ELEVATOR_CALL(7000, "电梯呼叫", "ELEVATOR_CALL", EventCategory.ELEVATOR),
    ELEVATOR_ARRIVED(7001, "电梯到达", "ELEVATOR_ARRIVED", EventCategory.ELEVATOR),
    ELEVATOR_FLOOR_SELECT(7002, "楼层选择", "ELEVATOR_FLOOR_SELECT", EventCategory.ELEVATOR),
    ELEVATOR_ABNORMAL(7003, "电梯异常", "ELEVATOR_ABNORMAL", EventCategory.ELEVATOR),

    /**
     * 未知事件
     */
    UNKNOWN(-1, "未知事件", "UNKNOWN", EventCategory.UNKNOWN);

    /**
     * 事件代码
     */
    private final int code;

    /**
     * 事件中文名称
     */
    private final String name;

    /**
     * 事件英文名称
     */
    private final String englishName;

    /**
     * 事件类别
     */
    private final EventCategory category;

    AccessEventTypeEnum(int code, String name, String englishName, EventCategory category) {
        this.code = code;
        this.name = name;
        this.englishName = englishName;
        this.category = category;
    }

    /**
     * 根据代码获取事件类型枚举
     *
     * @param code 事件代码
     * @return 事件类型枚举，如果不存在则根据范围判断类别
     */
    public static AccessEventTypeEnum getByCode(int code) {
        for (AccessEventTypeEnum type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        
        // 根据范围判断事件类别
        if (code >= 4000 && code < 5000) {
            return NORMAL_ACCESS; // 正常事件
        } else if (code >= 5000 && code < 6000) {
            return INVALID_CARD; // 异常事件
        } else if (code >= 6000 && code < 7000) {
            return DOOR_OPENED; // 报警事件
        } else if (code >= 7000 && code < 8000) {
            return ELEVATOR_CALL; // 梯控事件
        }
        
        return UNKNOWN;
    }

    /**
     * 根据代码获取事件名称
     *
     * @param code 事件代码
     * @return 事件名称
     */
    public static String getNameByCode(int code) {
        AccessEventTypeEnum type = getByCode(code);
        if (type != UNKNOWN) {
            return type.name;
        }
        // 对于未知代码，返回范围描述
        if (code >= 4000 && code < 5000) {
            return "正常事件(" + code + ")";
        } else if (code >= 5000 && code < 6000) {
            return "异常事件(" + code + ")";
        } else if (code >= 6000 && code < 7000) {
            return "报警事件(" + code + ")";
        } else if (code >= 7000 && code < 8000) {
            return "梯控事件(" + code + ")";
        }
        return "未知事件(" + code + ")";
    }

    /**
     * 判断事件是否为正常事件
     *
     * @param code 事件代码
     * @return 是否为正常事件
     */
    public static boolean isNormalEvent(int code) {
        return code >= 4000 && code < 5000;
    }

    /**
     * 判断事件是否为异常事件
     *
     * @param code 事件代码
     * @return 是否为异常事件
     */
    public static boolean isAbnormalEvent(int code) {
        return code >= 5000 && code < 6000;
    }

    /**
     * 判断事件是否为报警事件
     *
     * @param code 事件代码
     * @return 是否为报警事件
     */
    public static boolean isAlarmEvent(int code) {
        return code >= 6000 && code < 7000;
    }

    /**
     * 判断事件是否为梯控事件
     *
     * @param code 事件代码
     * @return 是否为梯控事件
     */
    public static boolean isElevatorEvent(int code) {
        return code >= 7000 && code < 8000;
    }

    /**
     * 获取事件类别
     *
     * @param code 事件代码
     * @return 事件类别
     */
    public static EventCategory getEventCategory(int code) {
        AccessEventTypeEnum type = getByCode(code);
        return type.category;
    }

    /**
     * 事件类别枚举
     */
    public enum EventCategory {
        /**
         * 正常事件
         */
        NORMAL("正常"),

        /**
         * 异常事件
         */
        ABNORMAL("异常"),

        /**
         * 报警事件
         */
        ALARM("报警"),

        /**
         * 梯控事件
         */
        ELEVATOR("梯控"),

        /**
         * 未知
         */
        UNKNOWN("未知");

        private final String name;

        EventCategory(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}

