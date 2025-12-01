package net.lab1024.sa.base.common.constant;

/**
 * 缓存键常量
 * 严格遵循repowiki规范：
 * - 统一的缓存键命名规范
 * - 模块化的缓存键管理
 * - 支持访客管理、设备管理、业务模块等
 */
public class CacheKeyConst {

    // 系统基础缓存
    public static final String USER_CACHE = "user:";
    public static final String MENU_CACHE = "menu:";
    public static final String DEPT_CACHE = "dept:";
    public static final String ROLE_CACHE = "role:";
    public static final String PERMISSION_CACHE = "permission:";

    // 访客管理缓存
    public static final String VISITOR_QR_CODE = "visitor:qr:";
    public static final String VISITOR_QR_REVERSE = "visitor:qr_reverse:";
    public static final String VISITOR_RESERVATION = "visitor:reservation:";
    public static final String VISITOR_STATISTICS = "visitor:statistics:";
    public static final String VISITOR_BLACKLIST = "visitor:blacklist:";
    public static final String VISITOR_COUNT = "visitor:count:";

    // 设备管理缓存
    public static final String DEVICE_CACHE = "device:";
    public static final String DEVICE_STATUS = "device:status:";
    public static final String DEVICE_CONFIG = "device:config:";
    public static final String DEVICE_LOCATION = "device:location:";

    // 考勤管理缓存
    public static final String ATTENDANCE_RULE = "attendance:rule:";
    public static final String ATTENDANCE_RECORD = "attendance:record:";
    public static final String ATTENDANCE_SCHEDULE = "attendance:schedule:";
    public static final String ATTENDANCE_LOCATION = "attendance:location:";

    // 消费管理缓存
    public static final String CONSUME_CONFIG = "consume:config:";
    public static final String CONSUME_RULE = "consume:rule:";
    public static final String CONSUME_RECORD = "consume:record:";
    public static final String CONSUME_BALANCE = "consume:balance:";
    public static final String CONSUME_DEVICE = "consume:device:";

    // 门禁管理缓存
    public static final String ACCESS_RECORD = "access:record:";
    public static final String ACCESS_DEVICE = "access:device:";
    public static final String ACCESS_PERMISSION = "access:permission:";
    public static final String ACCESS_LOG = "access:log:";

    // 通知公告缓存
    public static final String NOTICE_CACHE = "notice:";
    public static final String NOTICE_READ = "notice:read:";
    public static final String NOTICE_UNREAD = "notice:unread:";

    // 数据字典缓存
    public static final String DICT_CACHE = "dict:";
    public static final String DICT_TYPE = "dict:type:";

    // 文件服务缓存
    public static final String FILE_CACHE = "file:";
    public static final String FILE_UPLOAD = "file:upload:";
    public static final String FILE_DOWNLOAD = "file:download:";

    // 操作日志缓存
    public static final String OPERATION_LOG = "operation:log:";
    public static final String LOGIN_LOG = "login:log:";

    // 统计报表缓存
    public static final String REPORT_CACHE = "report:";
    public static final String STATISTICS_CACHE = "statistics:";
    public static final String DASHBOARD_CACHE = "dashboard:";

    // 系统配置缓存
    public static final String SYSTEM_CONFIG = "system:config:";
    public static final String SYSTEM_PARAM = "system:param:";
    public static final String SYSTEM_CACHE = "system:cache:";
}