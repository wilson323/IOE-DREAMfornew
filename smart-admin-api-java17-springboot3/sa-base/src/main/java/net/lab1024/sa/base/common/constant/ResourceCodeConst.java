package net.lab1024.sa.base.common.constant;

/**
 * 统一资源码常量定义
 * <p>
 * 严格遵循repowiki编码规范：使用jakarta包名、常量定义规范
 * 定义系统中所有的资源操作码，用于@RequireResource注解和权限验证
 * </p>
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-17
 */
public final class ResourceCodeConst {

    /**
     * 门禁设备管理资源码
     */
    public static final class AccessDevice {
        public static final String VIEW = "ACCESS_DEVICE_VIEW";
        public static final String ADD = "ACCESS_DEVICE_ADD";
        public static final String UPDATE = "ACCESS_DEVICE_UPDATE";
        public static final String DELETE = "ACCESS_DEVICE_DELETE";
        public static final String CONTROL = "ACCESS_DEVICE_CONTROL";
        public static final String CONFIG = "ACCESS_DEVICE_CONFIG";
        public static final String MONITOR = "ACCESS_DEVICE_MONITOR";
    }

    /**
     * 考勤管理资源码
     */
    public static final class Attendance {
        public static final String PUNCH_IN = "ATTENDANCE_PUNCH_IN";
        public static final String PUNCH_OUT = "ATTENDANCE_PUNCH_OUT";
        public static final String RECORD_VIEW = "ATTENDANCE_RECORD_VIEW";
        public static final String RECORD_EXPORT = "ATTENDANCE_RECORD_EXPORT";
        public static final String SCHEDULE_MANAGE = "ATTENDANCE_SCHEDULE_MANAGE";
        public static final String RULE_CONFIG = "ATTENDANCE_RULE_CONFIG";
        public static final String STATISTICS_VIEW = "ATTENDANCE_STATISTICS_VIEW";
    }

    /**
     * 消费管理资源码
     */
    public static final class Consume {
        public static final String ACCOUNT_MANAGE = "CONSUME_ACCOUNT_MANAGE";
        public static final String RECORD_VIEW = "CONSUME_RECORD_VIEW";
        public static final String RECORD_EXPORT = "CONSUME_RECORD_EXPORT";
        public static final String TERMINAL_MANAGE = "CONSUME_TERMINAL_MANAGE";
        public static final String SETTLEMENT_MANAGE = "CONSUME_SETTLEMENT_MANAGE";
        public static final String RECHARGE_MANAGE = "CONSUME_RECHARGE_MANAGE";
    }

    /**
     * 区域管理资源码
     */
    public static final class Area {
        public static final String VIEW = "AREA_VIEW";
        public static final String ADD = "AREA_ADD";
        public static final String UPDATE = "AREA_UPDATE";
        public static final String DELETE = "AREA_DELETE";
        public static final String MANAGE = "AREA_MANAGE";
        public static final String CONFIG = "AREA_CONFIG";
    }

    /**
     * 人员管理资源码
     */
    public static final class Person {
        public static final String VIEW = "PERSON_VIEW";
        public static final String ADD = "PERSON_ADD";
        public static final String UPDATE = "PERSON_UPDATE";
        public static final String DELETE = "PERSON_DELETE";
        public static final String IMPORT = "PERSON_IMPORT";
        public static final String EXPORT = "PERSON_EXPORT";
        public static final String BIOMETRIC_MANAGE = "PERSON_BIOMETRIC_MANAGE";
    }

    /**
     * 权限管理资源码
     */
    public static final class Permission {
        public static final String ROLE_MANAGE = "PERMISSION_ROLE_MANAGE";
        public static final String USER_ROLE_ASSIGN = "PERMISSION_USER_ROLE_ASSIGN";
        public static final String DATA_SCOPE_CONFIG = "PERMISSION_DATA_SCOPE_CONFIG";
        public static final String RESOURCE_CONFIG = "PERMISSION_RESOURCE_CONFIG";
    }

    /**
     * 系统管理资源码
     */
    public static final class System {
        public static final String USER_MANAGE = "SYSTEM_USER_MANAGE";
        public static final String DEPT_MANAGE = "SYSTEM_DEPT_MANAGE";
        public static final String DICTIONARY_MANAGE = "SYSTEM_DICTIONARY_MANAGE";
        public static final String CONFIG_MANAGE = "SYSTEM_CONFIG_MANAGE";
        public static final String LOG_VIEW = "SYSTEM_LOG_VIEW";
        public static final String MONITOR_VIEW = "SYSTEM_MONITOR_VIEW";
    }

    /**
     * 私有构造函数，防止实例化
     */
    private ResourceCodeConst() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}