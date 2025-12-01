package net.lab1024.sa.base.common.constant;

/**
 * Redis缓存键常量定义
 * 统一管理所有Redis缓存键，避免键冲突和重复定义
 *
 * @author IOE-DREAM Team
 * @date 2025-11-18
 */
public class RedisKeyConst {

    // ==================== 系统基础缓存 ====================

    /**
     * 用户信息缓存
     * 格式: user:info:{userId}
     */
    public static final String USER_INFO = "user:info:%s";

    /**
     * 用户权限缓存
     * 格式: user:permission:{userId}
     */
    public static final String USER_PERMISSION = "user:permission:%s";

    /**
     * 用户角色缓存
     * 格式: user:role:{userId}
     */
    public static final String USER_ROLE = "user:role:%s";

    // ==================== 消费模块缓存 ====================

    /**
     * 消费账户信息缓存
     * 格式: consume:account:{accountId}
     */
    public static final String CONSUME_ACCOUNT = "consume:account:%s";

    /**
     * 消费记录缓存
     * 格式: consume:record:{recordId}
     */
    public static final String CONSUME_RECORD = "consume:record:%s";

    /**
     * 用户消费账户映射
     * 格式: consume:user:account:{userId}
     */
    public static final String CONSUME_USER_ACCOUNT = "consume:user:account:%s";

    /**
     * 消费限额缓存
     * 格式: consume:limit:{userId}:{type}
     */
    public static final String CONSUME_LIMIT = "consume:limit:%s:%s";

    // ==================== 门禁系统缓存 ====================

    /**
     * 门禁设备缓存
     * 格式: access:device:{deviceId}
     */
    public static final String ACCESS_DEVICE = "access:device:%s";

    /**
     * 门禁记录缓存
     * 格式: access:record:{recordId}
     */
    public static final String ACCESS_RECORD = "access:record:%s";

    /**
     * 用户门禁权限缓存
     * 格式: access:permission:{userId}
     */
    public static final String ACCESS_PERMISSION = "access:permission:%s";

    // ==================== 考勤系统缓存 ====================

    /**
     * 考勤记录缓存
     * 格式: attendance:record:{recordId}
     */
    public static final String ATTENDANCE_RECORD = "attendance:record:%s";

    /**
     * 考勤规则缓存
     * 格式: attendance:rule:{ruleId}
     */
    public static final String ATTENDANCE_RULE = "attendance:rule:%s";

    /**
     * 考勤统计缓存
     * 格式: attendance:stats:{userId}:{date}
     */
    public static final String ATTENDANCE_STATS = "attendance:stats:%s:%s";

    // ==================== 视频监控缓存 ====================

    /**
     * 视频设备缓存
     * 格式: video:device:{deviceId}
     */
    public static final String VIDEO_DEVICE = "video:device:%s";

    /**
     * 视频流缓存
     * 格式: video:stream:{streamId}
     */
    public static final String VIDEO_STREAM = "video:stream:%s";

    // ==================== 分布式锁 ====================

    /**
     * 分布式锁键
     * 格式: lock:{business}:{key}
     */
    public static final String DISTRIBUTED_LOCK = "lock:%s:%s";

    /**
     * 消费操作锁
     * 格式: lock:consume:{userId}
     */
    public static final String CONSUME_LOCK = "lock:consume:%s";

    /**
     * 充值操作锁
     * 格式: lock:recharge:{userId}
     */
    public static final String RECHARGE_LOCK = "lock:recharge:%s";

    // ==================== 缓存过期时间（秒） ====================

    /**
     * 短期缓存过期时间：5分钟
     */
    public static final int SHORT_EXPIRE = 300;

    /**
     * 中期缓存过期时间：30分钟
     */
    public static final int MEDIUM_EXPIRE = 1800;

    /**
     * 长期缓存过期时间：2小时
     */
    public static final int LONG_EXPIRE = 7200;

    /**
     * 永久缓存过期时间：30天
     */
    public static final int PERMANENT_EXPIRE = 2592000;

    /**
     * 分布式锁过期时间：30秒
     */
    public static final int LOCK_EXPIRE = 30;

    // ==================== 私有构造函数 ====================

    private RedisKeyConst() {
        // 工具类，禁止实例化
    }
}